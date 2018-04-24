package application.query;

import java.util.HashMap;
import java.util.Map;

public class QueryResult extends HashMap<String, Map<String, Object>> {

  public QueryResult() {
    super();
  }

  public QueryResult(HashMap<String, Map<String, Object>> map) {
    super(map);
  }

}
