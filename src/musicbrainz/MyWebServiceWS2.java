package musicbrainz;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.musicbrainz.webservice.DefaultWebServiceWs2;

public abstract class MyWebServiceWS2 extends DefaultWebServiceWs2 {

  private Log log = LogFactory.getLog(MyWebServiceWS2.class);

  private String host;

  private String protocol;

  private String port;

  private Properties prop = new Properties();

  public MyWebServiceWS2() {
    try {
      prop.load(getClass().getResourceAsStream("/resources/properties/musicbrainz.properties"));
      host = prop.getProperty("server_host");
      port = prop.getProperty("server_port");
      protocol = prop.getProperty("server_protocol");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Constructs a URL that can be used to query the web service. The url is made up of the protocol, host, port,
   * version, type, path and parameters.
   * 
   * @param entity The entity (i.e. type, e.g. 'artist') the request is targeting
   * @param id The id of the entity
   * @param includeParams A list containing values for the 'inc' parameter (can be null)
   * @param filterParams Additional parameters depending on the entity (can be null)
   * 
   * @return An URL as String
   */
  @Override
  protected String makeURL(String entity, String id, List<String> includeParams, Map<String, String> filterParams) {
    StringBuilder url = new StringBuilder();
    Map<String, String> urlParams = new HashMap<String, String>();

    // Type is not requested/allowed anymore in ws2.
    // urlParams.put("type", this.type);

    // append filter params

    if (filterParams != null)
      urlParams.putAll(filterParams);

    // append protocol, host and port
    url.append(this.protocol).append("://").append(this.getHost());
    if (this.port != null)
      url.append(":").append(this.port);

    // append path
    url.append(PATHPREFIX).append("/").append(WS_VERSION).append("/").append(entity).append("/").append(id);

    // Handle COLLECTION sintax exception.

    if (entity.equals(COLLECTION) && !id.isEmpty()) {
      url.append("/" + RELEASES_INC);
    }

    // build space separated include param
    if (includeParams != null) {
      urlParams.put("inc", StringUtils.join(includeParams, "+"));
    }

    // append params
    url.append("?");
    Iterator<Entry<String, String>> it = urlParams.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String> e = it.next();
      try {
        url.append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue(), URL_ENCODING)).append("&");
      } catch (UnsupportedEncodingException ex) {
        log.error("Internal Error: Could not encode url parameter " + e.getKey(), ex);
      }
    }

    return url.substring(0, url.length() - 1);
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public void setHost(String host) {
    this.host = host;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

}
