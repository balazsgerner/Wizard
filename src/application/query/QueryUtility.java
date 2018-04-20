package application.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import model.MusicFile;

@XmlRootElement(name = "querymethods")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryUtility {

  private static QueryUtility instance = null;

  @XmlElement(name = "query", type = Query.class)
  private List<Query> queryMethods;

  @XmlTransient
  private Map<String, Query> queryMethodsByCode;

  protected QueryUtility() {
    queryMethodsByCode = new HashMap<String, Query>();
  }

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
    queryMethods.forEach(q -> queryMethodsByCode.put(q.getCode(), q));
  }

  public Query getQueryByCode(String code) {
    return queryMethodsByCode.get(code);
  }

  public Map<String, Map<String, Object>> performQuery(MusicFile musicFile, Query query) {
    Query queryMethod = null;
    try {
      Class<? extends Query> queryMethodClass = Class.forName(query.getClassName()).asSubclass(Query.class);
      queryMethod = queryMethodClass.getDeclaredConstructor().newInstance();
      queryMethod.setParams(query.getParams());
      queryMethod.performQuery(musicFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return queryMethod.getResults();
  }
}
