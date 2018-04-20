package application.query.spotify;

public class SpotifyIsrcQuery extends SpotifyQuery {

  @Override
  protected String createSearchStr() {
    String isrc = params.get("isrc").toString();
    return "isrc:" + isrc;
  }

}
