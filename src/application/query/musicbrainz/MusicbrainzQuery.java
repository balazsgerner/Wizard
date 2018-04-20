package application.query.musicbrainz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.musicbrainz.controller.Recording;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.RecordingWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.RecordingResultWs2;

import application.query.Query;
import model.MusicFile;

public class MusicbrainzQuery extends Query {

  private static final long LIMIT = 5L;

  private static final int RELEASE_LIMIT = 10;

  private Recording recording;

  @Override
  protected void init() {
    recording = new Recording();
    recording.setQueryWs(new MyHttpWSImpl());
    recording.getSearchFilter().setLimit(LIMIT);
  }

  @Override
  public void performQuery(MusicFile mf) {
    super.performQuery(mf);

    String title = formatStr(mf.getTitle());
    String artist = formatStr(mf.getBand());
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

    recording.search(searchStr);
    List<RecordingResultWs2> resultList = recording.getFirstSearchResultPage();
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

      results.put(recordingId, attributes);
    }
  }

  private String formatStr(String rawStr) {
    if (StringUtils.isEmpty(rawStr)) {
      return StringUtils.EMPTY;
    }
    String result = rawStr;
    result = result.replace("!", "");
    result = result.replace("/", " ");
    result = result.replace(' ', '+');
    return result;
  }

}
