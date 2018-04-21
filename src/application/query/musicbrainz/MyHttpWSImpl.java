package application.query.musicbrainz;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
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
import org.musicbrainz.webservice.AuthorizationException;
import org.musicbrainz.webservice.RequestException;
import org.musicbrainz.webservice.ResourceNotFoundException;
import org.musicbrainz.webservice.WebServiceException;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.musicbrainz.wsxml.MbXMLException;
import org.musicbrainz.wsxml.element.Metadata;

public class MyHttpWSImpl extends MyWebServiceWS2 {

  public MyHttpWSImpl() {
    super();
    userAgent = createUserAgent();
    this.httpClient = new DefaultHttpClient();
  }

  /**
   * A logger
   */
  private Log log = LogFactory.getLog(HttpClientWebServiceWs2.class);

  /**
   * A {@link HttpClient} instance
   */
  private DefaultHttpClient httpClient;

  /**
   * User agent string sent to music brainz.
   */
  private String userAgent;

  /**
   * Creates a httpClient with default properties and a custom user agent string.
   * 
   * @param applicationName custom application name used in user agent string
   * @param applicationVersion custom application version used in user agent string
   * @param applicationContact contact URL or author email used in user agent string
   */
  public MyHttpWSImpl(String applicationName, String applicationVersion, String applicationContact) {
    userAgent = createUserAgent(applicationName, applicationVersion, applicationContact);
    this.httpClient = new DefaultHttpClient();
  }

  /**
   * Use this constructor to inject a configured {@link DefaultHttpClient}.
   * 
   * @param httpClient A configured {@link DefaultHttpClient}.
   */
  public MyHttpWSImpl(DefaultHttpClient httpClient) {
    this.httpClient = httpClient;
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

    // retry 3 times, do not retry if we got a response, because we
    // may only query the web service once a second

    HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

        if (executionCount >= 3) {
          // Do not retry if over max retry count
          return false;
        }
        if (exception instanceof NoHttpResponseException) {
          // Retry if the server dropped connection on us
          return true;
        }
        if (exception instanceof SSLHandshakeException) {
          // Do not retry on SSL handshake exception
          return false;
        }
        HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
          // Retry if the request is considered idempotent
          return true;
        }
        return false;
      }
    };

    httpClient.setHttpRequestRetryHandler(myRetryHandler);
  }

  @Override
  protected Metadata doGet(String url) throws WebServiceException, MbXMLException {
    setConnectionParam();
    setRetryHandler();// inside the call

    // retry with new calls if the error is 503 Service unavaillable.
    boolean repeat = true;
    int trial = 0;
    int maxtrial = 5;

    while (repeat) {

      trial++;
      HttpGet method = new HttpGet(url);
      Metadata md = executeMethod(method);
      if (md == null && trial > maxtrial) {
        String em = "ABORTED: web service returned an error " + maxtrial + " time consecutively";
        log.error(em);
        throw new WebServiceException(em);
      } else if (md != null) {
        return md;
      }

    } // end wile
    return null;
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

  private Metadata executeMethod(HttpUriRequest method) throws MbXMLException, WebServiceException {

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
      default: {
        String em = "Fatal web service error: " + statusCode;
        log.error(em);
        throw new WebServiceException(em);
      }

      }
    } catch (IOException e) {
      log.error("Fatal transport error: " + e.getMessage());
      throw new WebServiceException(e.getMessage(), e);
    } finally {
      EntityUtils.consumeQuietly(response.getEntity());
    }
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

}
