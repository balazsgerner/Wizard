package application.query;

import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import application.controller.MainWindowController.QueryService.QueryTask;
import model.MusicFile;

@XmlRootElement(name = "querymethods")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryUtility {

  public static Logger log = Logger.getLogger(QueryUtility.class);

  private static QueryUtility instance = null;

  @XmlElement(name = "query", type = Query.class)
  private List<Query> queryMethods;

  @XmlTransient
  private Map<String, Query> queryMethodsByCode = new HashMap<String, Query>();

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
    queryMethods.forEach(q -> {
      Query queryMethod = null;
      try {
        queryMethod = getQueryMethodInstance(q);
      } catch (Exception e) {
        log.error("Error while initializing query methods", e);
      }
      queryMethodsByCode.put(q.getCode(), queryMethod);
    });
  }

  public String getQueryNameByCode(String code) {
    return queryMethodsByCode.get(code).getName();
  }

  public Query getQueryMethodByCode(String code) {
    return queryMethodsByCode.get(code);
  }

  public String getQueryCodeByName(String name) {
    for (Query q : queryMethods) {
      if (q.getName().equals(name)) {
        return q.getCode();
      }
    }
    return null;
  }

  public void performQuery(MusicFile musicFile, Query query) throws ConnectException {
    try {
      Query queryMethod = queryMethodsByCode.get(query.getCode());
      queryMethod.performQuery(musicFile);
    } catch (ConnectException e) {
      log.error("Cannot connect to web service!", e);
      throw e;
    }
  }

  private Query getQueryMethodInstance(Query query) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
      NoSuchMethodException, IllegalArgumentException, InvocationTargetException, SecurityException {
    Query queryMethod = queryMethodsByCode.get(query.getCode());
    Class<? extends Query> queryMethodClass = Class.forName(query.getClassName()).asSubclass(Query.class);
    queryMethod = queryMethodClass.getDeclaredConstructor(Query.class).newInstance(query);
    queryMethodsByCode.put(queryMethod.getCode(), queryMethod);
    return queryMethod;
  }

  public void performManyQueries(List<MusicFile> musicFiles, Query query, QueryTask queryTask) throws ConnectException {
    queryTask.updateProgress(0, 100);
    int listSize = musicFiles.size();
    for (int i = 0; i < listSize; i++) {
      if (queryTask.isCancelled()) {
        return;
      }
      MusicFile musicFile = musicFiles.get(i);
      performQuery(musicFile, query);
      queryTask.updateProgress(i, listSize);
    }

    queryTask.updateProgress(1, 1);
  }

}
