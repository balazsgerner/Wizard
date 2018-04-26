package model;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import application.query.QueryResult;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MusicFile {

  private static Logger log = Logger.getLogger(MusicFile.class);

  private AudioFile file;

  private Tag tag;

  private Map<String, Object> queryResultMap;

  private Map<String, String> assignedIds;

  private String lastQueryCode;

  private BooleanProperty dirty = new SimpleBooleanProperty(false);

  public MusicFile(File file) {
    try {
      this.file = AudioFileIO.read(file);
      this.tag = this.file.getTag();
    } catch (Exception e) {
      log.error("Error while loading audioFile!", e);
    }
  }

  public String getPath() {
    return file.getFile().getAbsolutePath();
  }

  public String getExtension() {
    return FilenameUtils.getExtension(file.getFile().getName());
  }

  public String getBand() {
    return tag.getFirst(FieldKey.ARTIST);
  }

  public void setBand(String band) {
    // this.band = band;
  }

  public String getAlbum() {
    return tag.getFirst(FieldKey.ALBUM);
  }

  public void setAlbum(String album) {
    // this.album = album;
  }

  public String getTitle() {
    return tag.getFirst(FieldKey.TITLE);
  }

  public void setTitle(String title) {
    // this.title = title;
  }

  public byte[] getArtwork() {
    Artwork artWork = tag.getFirstArtwork();
    return artWork == null ? null : artWork.getBinaryData();
  }

  public String getGenre() {
    return tag.getFirst(FieldKey.GENRE);
  }

  public String getYear() {
    String rawYear = tag.getFirst(FieldKey.YEAR);
    if (isMp4() && (rawYear != null && !rawYear.isEmpty())) {
      return rawYear.substring(0, 4);
    }
    return rawYear;
  }

  private boolean isMp4() {
    return "m4a".equals(getExtension());
  }

  public Integer getTrack() {
    return Integer.valueOf(tag.getFirst(FieldKey.TRACK));
  }

  public String getTrackLength() {
    int trackLength = file.getAudioHeader().getTrackLength();
    return String.format("%02d:%02d", trackLength / 60, trackLength % 60);
  }

  public String getBitRate() {
    return Long.toString(file.getAudioHeader().getBitRateAsNumber()) + " kbps";
  }

  public String getSampleRate() {
    return file.getAudioHeader().getSampleRate() + " Hz";
  }

  @Override
  public String toString() {
    return "[artist: " + getBand() + ", album: " + getAlbum() + ", title: " + getTitle() + "]";
  }

  public Map<String, Object> getAttibuteMap() {
    Map<String, Object> attributeMap = new HashMap<>();
    List<Method> listOfMethods = Arrays.asList(getClass().getDeclaredMethods());
    Stream<Method> listOfGetters = listOfMethods.stream().filter(p -> {
      String name = p.getName();
      return name.startsWith("get") && !(name.equals("getAttibuteMap") || name.equals("getArtwork"));
    });

    listOfGetters.filter(p -> {
      String mName = p.getName().toLowerCase();
      return !(mName.contains("query") || mName.contains("dirty"));
    }).forEach(g -> {

      String attributeName = g.getName().split("get")[1].toLowerCase();
      attributeName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
      try {
        Object attributeValue = g.invoke(this);
        attributeMap.put(attributeName, attributeValue);
      } catch (Exception e) {
        log.error("Error while creating attributeMap!", e);
      }
    });
    return attributeMap;
  }

  public Map<String, Object> getAllQueryResults() {
    return queryResultMap;
  }

  @SuppressWarnings("unchecked")
  public void setAllQueryResults(Map<String, Object> queryResults) {
    this.lastQueryCode = (String) queryResults.remove("latest_query");
    this.assignedIds = (Map<String, String>) queryResults.remove("assigned_ids");
    if (queryResultMap != null) {
      queryResultMap.clear();
    }
    queryResults.forEach((code, queryResult) -> {
      HashMap<String, Map<String, Object>> resMap = new HashMap<>();
      resMap.put(code, queryResults);
      setQueryResult(code, new QueryResult(resMap));
    });
    this.queryResultMap = queryResults;
    setDirty(false);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getQueryResult(String queryCode) {
    return (Map<String, Object>) queryResultMap.get(queryCode);
  }

  public void setQueryResult(String queryCode, QueryResult result) {
    if (queryResultMap == null) {
      queryResultMap = new HashMap<>();
    }
    queryResultMap.put(queryCode, result);
    dirty.set(true);
  }

  public String getLastQueryCode() {
    return lastQueryCode;
  }

  public void setLastQueryCode(String lastQueryCode) {
    this.lastQueryCode = lastQueryCode;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getLatestQueryResult() {
    return lastQueryCode == null ? null : (Map<String, Object>) queryResultMap.get(lastQueryCode);
  }

  public void setDirty(boolean dirty) {
    this.dirty.set(dirty);
  }

  public Boolean getDirty() {
    return dirty.get();
  }

  public BooleanProperty dirtyProperty() {
    return dirty;
  }

  public Map<String, String> getAssignedIds() {
    return assignedIds;
  }

  public void setAssignedIds(Map<String, String> assignedIds) {
    this.assignedIds = assignedIds;
  }

  public void assignId(String queryMethod, String id) {
    if (assignedIds == null) {
      assignedIds = new HashMap<>();
    }
    assignedIds.put(queryMethod, id);
  }

}
