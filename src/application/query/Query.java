package application.query;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import model.MusicFile;

@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public class Query {

  @XmlElement
  protected String name;

  @XmlElement
  protected String code;

  @XmlElement
  protected boolean parameterized;

  @XmlElement(name = "class")
  protected String className;

  @XmlTransient
  protected Map<String, Map<String, Object>> results;

  @XmlTransient
  protected MusicFile musicFile;

  @XmlTransient
  protected Map<String, Object> params;

  public Query() {
    params = new HashMap<>();
  }

  protected void init() {
    // to be overridden
  }

  public void performQuery(MusicFile mf) {
    this.musicFile = mf;
    this.results = new HashMap<String, Map<String, Object>>();
    init();
    String searchString = createSearchStr();
    fillResultsMap(searchString);
  }

  protected String createSearchStr() {
    // to be overridden
    return null;
  }

  protected void fillResultsMap(String searchString) {
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

  public Map<String, Map<String, Object>> getResults() {
    return results;
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

}
