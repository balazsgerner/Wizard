package model;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

public class MusicFile {

  private AudioFile file;

  private Tag tag;

  private Map<String, Map<String, Object>> queryResults;

  private String lastQueryName;

  public MusicFile(File file) {
    try {
      this.file = AudioFileIO.read(file);
      this.tag = this.file.getTag();
    } catch (Exception e) {
      e.printStackTrace();
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

    listOfGetters.forEach(g -> {
      String attributeName = g.getName().split("get")[1].toLowerCase();
      attributeName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
      try {
        Object attributeValue = g.invoke(this);
        attributeMap.put(attributeName, attributeValue);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    return attributeMap;
  }

  public Map<String, Map<String, Object>> getQueryResults() {
    return queryResults;
  }

  public void setQueryResults(Map<String, Map<String, Object>> queryResults) {
    this.queryResults = queryResults;
  }

  public String getLastQueryName() {
    return lastQueryName;
  }

  public void setLastQueryName(String lastQueryName) {
    this.lastQueryName = lastQueryName;
  }

}
