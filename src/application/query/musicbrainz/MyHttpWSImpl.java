package application.query.musicbrainz;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.musicbrainz.webservice.AuthorizationException;
import org.musicbrainz.webservice.RequestException;
import org.musicbrainz.webservice.ResourceNotFoundException;
import org.musicbrainz.webservice.WebServiceException;
import org.musicbrainz.wsxml.MbXMLException;
import org.musicbrainz.wsxml.element.Metadata;

public class MyHttpWSImpl extends MyWebServiceWS2 {

  private Logger log = Logger.getLogger(MyHttpWSImpl.class);

  private DefaultHttpClient httpClient;

  private String userAgent;

  private boolean hasConnectionProblem = false;

  public MyHttpWSImpl() {
    super();
    userAgent = createUserAgent();
    this.httpClient = new DefaultHttpClient();
    log.setLevel(Level.ERROR);
    Logger.getLogger(org.apache.http.impl.conn.BasicClientConnectionManager.class).setLevel(Level.OFF);
    Logger.getLogger(org.apache.http.impl.client.DefaultHttpClient.class).setLevel(Level.OFF);
    Logger.getLogger(org.apache.http.impl.conn.DefaultClientConnection.class).setLevel(Level.OFF);
  }

  private void setConnectionParam() {
    HttpParams connectionParams = httpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(connectionParams, 60000);
    HttpConnectionParams.setSoTimeout(connectionParams, 60000);
    connectionParams.setParameter("http.useragent", userAgent);
    connectionParams.setParameter("http.protocol.content-charset", "UTF-8");
    if (getUsername() != null && !getUsername().isEmpty()) {
      UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getUsername(), getPassword());
      AuthScope authScope = new AuthScope(getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
      httpClient.getCredentialsProvider().setCredentials(authScope, creds);
    }
  }

  private void setRetryHandler() {

    // retry 3 times
    HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

        if (executionCount >= 3) {
          return false;
        }
        if (exception instanceof NoHttpResponseException) {
          return true;
        }
        if (exception instanceof SSLHandshakeException) {
          return false;
        }
        HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
          return true;
        }
        return false;
      }
    };

    httpClient.setHttpRequestRetryHandler(myRetryHandler);
  }

  @Override
  protected Metadata doGet(String url) throws MbXMLException {
    setConnectionParam();
    setRetryHandler();
    HttpGet method = new HttpGet(url);
    Metadata md = null;
    try {
      hasConnectionProblem = false;
      md = executeMethod(method);
    } catch (WebServiceException | IOException e) {
      hasConnectionProblem = true;
    }
    return md;
  }

  @Override
  protected Metadata doPost(String url, Metadata md) throws WebServiceException, MbXMLException {
    return null;
  }

  @Override
  protected Metadata doPut(String url) throws WebServiceException, MbXMLException {
    return null;
  }

  @Override
  protected Metadata doDelete(String url) throws WebServiceException, MbXMLException {
    return null;
  }

  private Metadata executeMethod(HttpUriRequest method) throws MbXMLException, WebServiceException, IOException {

    HttpParams params = new BasicHttpParams();
    HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
    paramsBean.setUserAgent(userAgent);
    method.setParams(params);

    // Try using compression
    method.setHeader("Accept-Encoding", "gzip");

    HttpResponse response = null;

    try {
      // Execute the method.
      log.debug("Hitting url: " + method.getURI().toString());
      response = this.httpClient.execute(method);

      lastHitTime = System.currentTimeMillis();

      int statusCode = response.getStatusLine().getStatusCode();

      switch (statusCode) {
      case HttpStatus.SC_SERVICE_UNAVAILABLE: {
        // Maybe the server is too busy, let's try again.
        log.error("Service unavaillable " + statusCode);
        method.abort();
        lastHitTime = System.currentTimeMillis();
        wait(1);
        return null;
      }
      case HttpStatus.SC_BAD_GATEWAY: {
        // Maybe the server is too busy, let's try again.
        log.error("Bad Gateway " + statusCode);
        method.abort();
        lastHitTime = System.currentTimeMillis();
        wait(1);
        return null;
      }
      case HttpStatus.SC_OK:
        InputStream instream = response.getEntity().getContent();
        // Check if content is compressed
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
          instream = new GZIPInputStream(instream);
        }
        Metadata mtd = getParser().parse(instream);
        // Closing the input stream will trigger connection release
        try {
          instream.close();
        } catch (Exception ignore) {
        }
        lastHitTime = System.currentTimeMillis();
        return mtd;

      case HttpStatus.SC_NOT_FOUND:
        throw new ResourceNotFoundException("Not found " + statusCode);

      case HttpStatus.SC_BAD_REQUEST:
        throw new RequestException("Bad Request " + statusCode);

      case HttpStatus.SC_FORBIDDEN:
        throw new AuthorizationException("Forbidden " + statusCode);

      case HttpStatus.SC_UNAUTHORIZED:
        throw new AuthorizationException("Unauthorized " + statusCode);
      case HttpStatus.SC_INTERNAL_SERVER_ERROR: {
        throw new AuthorizationException("Internal server error " + statusCode);
      }

      }
    } finally {
      if (response != null) {
        EntityUtils.consumeQuietly(response.getEntity());
      }
    }
    return null;
  }

  private static long lastHitTime = 0;

  private static void wait(int seconds) {
    long t1;
    if (lastHitTime > 0) {
      do {
        t1 = System.currentTimeMillis();
      } while (t1 - lastHitTime < seconds * 1000);
    }
  }

  public boolean hasConnectionProblem() {
    return hasConnectionProblem;
  }
  
}
