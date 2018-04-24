package application.query;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import model.MusicFile;

@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public class Query {

  protected static Logger log = Logger.getLogger(Query.class);
  
  @XmlElement
  protected String name;

  @XmlElement
  protected String code;

  @XmlElement
  protected boolean parameterized;

  @XmlElement(name = "many")
  protected boolean performManyQuery;

  @XmlElement(name = "class")
  protected String className;

  @XmlTransient
  protected QueryResult result;

  @XmlTransient
  protected MusicFile musicFile;

  @XmlTransient
  protected Map<String, Object> params;

  public Query() throws ConnectException {
    this.result = new QueryResult();
    this.params = new HashMap<>();
    init();
  }

  public Query(Query original) throws ConnectException {
    this.name = original.name;
    this.code = original.code;
    this.parameterized = original.parameterized;
    this.className = original.className;
    this.params = original.params;
    init();
  }

  protected void init() throws ConnectException {
    // to be overridden
  }

  public void performQuery(MusicFile mf) throws ConnectException {
    this.musicFile = mf;
    this.result = new QueryResult();
    String searchString = createSearchStr();
    fillResultsMap(searchString);
    mf.setQueryResult(code, result);
    mf.setLastQueryCode(code);
  }

  protected String createSearchStr() {
    // to be overridden
    return null;
  }

  protected void fillResultsMap(String searchString) throws ConnectException {
    // to be overridden
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setParam(String paramName, Object paramValue) {
    params.put(paramName, paramValue);
  }

  @Override
  public String toString() {
    return "[name=" + name + ", code=" + code + "]";
  }

  public QueryResult getResults() {
    return result;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public boolean isParametrized() {
    return parameterized;
  }

  public void setParametrized(boolean parameterized) {
    this.parameterized = parameterized;
  }

  public boolean isPerformManyQuery() {
    return performManyQuery;
  }

  public void setPerformManyQuery(boolean performManyQuery) {
    this.performManyQuery = performManyQuery;
  }

}
