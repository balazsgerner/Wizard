package application.query;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class QueryAdapter extends XmlAdapter<Query[], Map<String, Object>> {

  @Override
  public Map<String, Object> unmarshal(Query[] queryMethods) throws Exception {
    Map<String, Object> queryMethodMap = new HashMap<String, Object>();
    for (Query query : queryMethods) {
      queryMethodMap.put(query.getCode(), query);
    }
    return queryMethodMap;
  }

  @Override
  public Query[] marshal(Map<String, Object> v) throws Exception {
    Query[] queryMethods = new Query[v.size()];
    int i = 0;
    for (Map.Entry<String, Object> entry : v.entrySet()) {
      queryMethods[i++] = new Query(entry.getKey(), entry.getValue().toString());
    }
    return queryMethods;
  }

}
