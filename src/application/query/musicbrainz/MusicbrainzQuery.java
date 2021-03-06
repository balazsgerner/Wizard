package application.query.musicbrainz;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.musicbrainz.controller.Recording;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.RecordingWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.RecordingResultWs2;

import application.query.Query;

public class MusicbrainzQuery extends Query {

  private static final long LIMIT = 5L;

  private static final int RELEASE_LIMIT = 10;

  private Recording recording;

  public MusicbrainzQuery(Query query) throws ConnectException {
    super(query);
  }

  @Override
  protected void init() {
    Logger.getLogger("org.musicbrainz.wsxml.impl.JDOMParserWs2").setLevel(Level.ERROR);
    recording = new Recording();
    recording.setQueryWs(new MyHttpWSImpl());
    recording.getSearchFilter().setLimit(LIMIT);
  }

  @Override
  protected void fillResultsMap(String searchStr) throws ConnectException {
    recording.search(searchStr);

    hideWSErrors();
    List<RecordingResultWs2> resultList = recording.getFirstSearchResultPage();
    resetErrors();

    MyHttpWSImpl queryWs = (MyHttpWSImpl) recording.getQueryWs();
    if (queryWs.hasConnectionProblem()) {
      throw new ConnectException("Cannot connect to web service");
    }

    Iterator<RecordingResultWs2> iterResults = resultList.iterator();
    while (iterResults.hasNext()) {
      Map<String, Object> attributes = new HashMap<>();
      RecordingResultWs2 rec = iterResults.next();
      RecordingWs2 recordingEntity = rec.getRecording();
      attributes.put("artist credit", recordingEntity.getArtistCreditString());
      attributes.put("diambiguation", recordingEntity.getDisambiguation());
      attributes.put("isrc", recordingEntity.getIsrcString());
      attributes.put("title", recordingEntity.getTitle());
      attributes.put("duration", recordingEntity.getDuration());
      String recordingId = recordingEntity.getId();
      attributes.put("id", recordingId);
      attributes.put("rating", recordingEntity.getRating().getAverageRating());
      List<TagWs2> tags = recordingEntity.getTags();
      List<String> tagStr = new ArrayList<>();
      for (TagWs2 tag : tags) {
        tagStr.add(tag.getName());
      }
      attributes.put("tags", String.join(", ", tagStr));
      List<ReleaseWs2> releases = recordingEntity.getReleases();
      for (int i = 0; i < RELEASE_LIMIT && i < releases.size(); i++) {
        ReleaseWs2 rel = releases.get(i);
        String releaseStr = "release #" + (i + 1);
        attributes.put(releaseStr, rel.getUniqueTitle());
        attributes.put(releaseStr + " country", rel.getCountryId());
      }

      result.put(recordingId, attributes);
    }
  }

  private void resetErrors() {
    System.setErr(System.err);
  }

  private void hideWSErrors() {
    System.err.close();
  }

  protected String formatStr(String rawStr) {
    if (StringUtils.isEmpty(rawStr)) {
      return StringUtils.EMPTY;
    }
    String result = rawStr;
    result = StringUtils.replace(result, "!", "");
    result = StringUtils.replace(result, "/", " ");
    result = StringUtils.replace(result, " ", "+");
    result = StringUtils.replace(result, "&", "and");
    result = StringUtils.replace(result, "[", "(");
    result = StringUtils.replace(result, "]", ")");
    return result;
  }

  @Override
  protected String createSearchStr() {
    String title = formatStr(musicFile.getTitle());
    String artist = formatStr(musicFile.getBand());
    String searchStr = "";
    boolean artistEmpty = StringUtils.isEmpty(artist);
    String artistStr = null;
    if (!artistEmpty) {
      artistStr = "artist:" + artist;
    }

    boolean titleEmpty = StringUtils.isEmpty(title);
    String titleStr = null;
    if (!titleEmpty) {
      titleStr = "recording:" + title;
    }

    if (!artistEmpty && !titleEmpty) {
      searchStr = String.join("+and+", artistStr, titleStr);
    } else if (artistEmpty) {
      searchStr = titleStr;
    } else if (titleEmpty) {
      searchStr = artistStr;
    } else {
      throw new IllegalArgumentException("Search string cannot be null!");
    }
    return searchStr;
  }

}
