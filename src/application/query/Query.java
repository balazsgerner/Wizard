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

  protected String name;

  protected String code;

  @XmlElement(name = "class")
  protected String className;

  @XmlTransient
  protected Map<String, Map<String, Object>> results;

  @XmlTransient
  protected MusicFile musicFile;

  protected void init() {
    // to be overridden
  }

  public void performQuery(MusicFile mf) {
    // to be overridden
    this.musicFile = mf;
    init();
    results = new HashMap<String, Map<String, Object>>();
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

}
