package application.query;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import application.query.Query;

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

}
