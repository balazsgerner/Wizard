package application.query.musicbrainz;

import application.query.Query;

public class MusicbrainzIsrcQuery extends MusicbrainzQuery {

  public MusicbrainzIsrcQuery(Query query) {
    super(query);
  }

  @Override
  protected String createSearchStr() {
    String isrc = params.get("isrc").toString();
    return "isrc:" + isrc;
  }
}
