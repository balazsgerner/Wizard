package application.query.musicbrainz;

public class MusicbrainzIsrcQuery extends MusicbrainzQuery {

  public static final String CODE = "MUSICBRAINZISRC";

  @Override
  protected String createSearchStr() {
    String isrc = params.get("isrc").toString();
    return "isrc:" + isrc;
  }
}
