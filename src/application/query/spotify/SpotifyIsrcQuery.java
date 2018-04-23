package application.query.spotify;

import java.net.ConnectException;

import application.query.Query;

public class SpotifyIsrcQuery extends SpotifyQuery {

  public SpotifyIsrcQuery(Query query) throws ConnectException {
    super(query);
  }

  @Override
  protected String createSearchStr() {
    String isrc = params.get("isrc").toString();
    return "isrc:" + isrc;
  }

}
