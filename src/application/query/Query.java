package application.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import model.MusicFile;

@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public class Query {

  @XmlTransient
  protected boolean initialized = false;

  protected String name;

  protected String code;

  @XmlTransient
  protected MusicFile musicFile;

  public void init(MusicFile musicFile) {
    // to be overridden
    this.musicFile = musicFile;
    this.initialized = true;
  }

  public void performQuery() throws Exception {
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

  @Override
  public String toString() {
    return "[name=" + name + ", code=" + code + "]";
  }

}
