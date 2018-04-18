package model;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

public class MusicFile {

  private AudioFile file;

  private Tag tag;

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

}
