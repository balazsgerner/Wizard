package application.query;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import model.MusicFile;

@XmlRootElement(name = "querymethods")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryUtility {

  private static QueryUtility instance = null;

  @XmlElement(name = "query", type = Query.class)
  private List<Query> queryMethods;

  public static QueryUtility getInstance() {
    if (instance == null) {
      instance = new QueryUtility();
    }
    return instance;
  }

  public List<Query> getQueryMethods() {
    return queryMethods;
  }

  public void setQueryMethods(List<Query> queryMethods) {
    this.queryMethods = queryMethods;
  }

  public Object performQuery(MusicFile musicFile, String code) {
    try {
      switch (code) {
      case AcoustidQuery.CODE:
        AcoustidQuery acoustidQuery = new AcoustidQuery();
        acoustidQuery.init(musicFile);
        acoustidQuery.performQuery();
        break;
      default:
        break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}
