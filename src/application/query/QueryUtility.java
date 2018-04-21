package application.query;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import application.MainWindowController.QueryTask;
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
  }

  public Map<String, Map<String, Object>> performQuery(MusicFile musicFile, Query query) {
    Query queryMethod = null;
    try {
      queryMethod = getQueryMethodInstance(query);
      queryMethod.performQuery(musicFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return queryMethod.getResults();
  }

  private Query getQueryMethodInstance(Query query)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Query queryMethod = queryMethodsByCode.get(query.getCode());
    if (queryMethod == null) {
      Class<? extends Query> queryMethodClass = Class.forName(query.getClassName()).asSubclass(Query.class);
      queryMethod = queryMethodClass.getDeclaredConstructor(Query.class).newInstance(query);
      queryMethodsByCode.put(queryMethod.getCode(), queryMethod);
    } else {
      queryMethod.setParams(query.getParams());
    }
    return queryMethod;
  }

  public void performManyQueries(List<MusicFile> musicFiles, Query query, QueryTask queryTask) {
    queryTask.updateProgress(0, 100);
    int listSize = musicFiles.size();
    for (int i = 0; i < listSize; i++) {
      MusicFile musicFile = musicFiles.get(i);
      Map<String, Map<String, Object>> res = performQuery(musicFile, query);
      musicFile.setQueryResults(res);
      musicFile.setLastQueryName(query.getName());
      queryTask.updateProgress(i, listSize);
    }

    queryTask.updateProgress(1, 1);
  }

}
