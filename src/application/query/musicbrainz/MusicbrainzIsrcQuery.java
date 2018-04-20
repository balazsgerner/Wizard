package application.query.musicbrainz;

public class MusicbrainzIsrcQuery extends MusicbrainzQuery {

  @Override
  protected String createSearchStr() {
    String isrc = params.get("isrc").toString();
    return "isrc:" + isrc;
  }
}
