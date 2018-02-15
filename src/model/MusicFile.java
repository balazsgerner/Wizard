package model;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

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

}
