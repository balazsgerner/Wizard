package application.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QueryResult {

  protected Map<String, Map<String, Object>> results;

  public QueryResult() {
    this.results = new HashMap<>();
  }

  public QueryResult(Map<String, Map<String, Object>> results) {
    this.results = results;
  }

  public Map<String, Map<String, Object>> getResultMap() {
    return results;
  }

  public void setResults(Map<String, Map<String, Object>> results) {
    this.results = results;
  }

  public void put(String key, Map<String, Object> value) {
    results.put(key, value);
  }

  public int size() {
    return results.size();
  }

  public Map<String, Object> get(Object key) {
    return results.get(key);
  }

  public Set<String> keySet() {
    return results.keySet();
  }

}
