package application.query;

import java.util.List;
import java.util.Map;

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

  /**
   * Instantiates a queryMethod class and performs a query with the selected musicFile.
   * 
   * @param musicFile - the musicfile to be queried
   * @param q - query object
   * @return
   */
  public Map<String, Map<String, Object>> performQuery(MusicFile musicFile, Query q) {
    Query queryMethod = null;
    try {
      Class<? extends Query> queryMethodClass = Class.forName(q.getClassName()).asSubclass(Query.class);
      queryMethod = queryMethodClass.getDeclaredConstructor().newInstance();
      queryMethod.performQuery(musicFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return queryMethod.getResults();
  }

}
