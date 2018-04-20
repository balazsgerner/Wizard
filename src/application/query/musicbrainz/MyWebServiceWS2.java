package application.query.musicbrainz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.musicbrainz.webservice.DefaultWebServiceWs2;

public abstract class MyWebServiceWS2 extends DefaultWebServiceWs2 {

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

  @Override
  protected String makeURL(String entity, String id, List<String> includeParams, Map<String, String> filterParams) {
    StringBuilder url = new StringBuilder();
    Map<String, String> urlParams = new HashMap<String, String>();

    if (filterParams != null) {
      urlParams.putAll(filterParams);
    }

    url.append(protocol).append("://").append(host);
    if (port != null) {
      url.append(":").append(port);
    }
    url.append(PATHPREFIX).append("/").append(WS_VERSION).append("/").append(entity).append("/").append(id);

    url.append("?");
    url.append("query=").append(urlParams.get("query"));
    urlParams.remove("query");
    urlParams.remove("offset");
    Iterator<Entry<String, String>> it = urlParams.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String> e = it.next();
      url.append("&").append(e.getKey()).append("=").append(e.getValue());
    }

    return url.toString();
  }

  @Override
  public String getHost() {
    return host;
  }
}
