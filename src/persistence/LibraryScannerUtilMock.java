package persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import model.MusicFile;

public class LibraryScannerUtilMock {

  public static final String LIBRARY_PATH = "C:\\Users\\Gerner\\Music";

  protected List<String> supportedExtensions = Arrays.asList("mp3", "m4a", "flac");

  protected List<MusicFile> model = new ArrayList<MusicFile>();

  public List<MusicFile> getModel() {
    return model;
  }

  public void scanLibrary(File library) throws IOException {
    for (File file : library.listFiles()) {
      if (file.isDirectory()) {
        scanLibrary(file);
      } else {
        String ext = FilenameUtils.getExtension(file.getName());
        if (supportedExtensions.contains(ext)) {
          model.add(new MusicFile(file));
        }
      }
    }
  }

}
