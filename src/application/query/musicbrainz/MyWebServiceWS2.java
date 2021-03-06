package application.query.musicbrainz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.musicbrainz.webservice.DefaultWebServiceWs2;
import org.musicbrainz.webservice.WebServiceException;
import org.musicbrainz.wsxml.MbXMLException;
import org.musicbrainz.wsxml.element.Metadata;

public abstract class MyWebServiceWS2 extends DefaultWebServiceWs2 {

  private static Logger log = Logger.getLogger(MyWebServiceWS2.class);

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
      log.error("Error while initializing web service!", e);
    }
  }

  @Override
  public Metadata get(String entity, String id, List<String> includeParams, Map<String, String> filterParams)
      throws WebServiceException, MbXMLException {
    String url = this.makeURL(entity, id, includeParams, filterParams);
    Optional<Metadata> results = Optional.ofNullable(doGet(url));
    return results.orElseThrow(() -> new WebServiceException("Cannot connect to web service, now aborting!"));
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
