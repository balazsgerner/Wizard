package musicbrainz;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.musicbrainz.controller.Artist;
import org.musicbrainz.model.entity.ArtistWs2;
import org.musicbrainz.model.searchresult.ArtistResultWs2;

import model.MusicFile;
import persistence.LibraryScannerUtilMock;

public class MusicBrainzWSDemo {

  private LibraryScannerUtilMock libraryScanner = new LibraryScannerUtilMock();

  private PrintWriter pw;

  private static MusicBrainzWSDemo instance;

  public static void main(String[] args) {
    Logger.getRootLogger().setLevel(Level.OFF);
    instance = new MusicBrainzWSDemo();
    try {
      instance.queryArtists();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void queryArtists() throws IOException {
    Properties prop = new Properties();
    prop.load(getClass().getResourceAsStream("/resources/properties/musicbrainz.properties"));
    libraryScanner.scanLibrary(new File(LibraryScannerUtilMock.LIBRARY_PATH));

    List<MusicFile> model = libraryScanner.getModel();
    Set<String> artistNames = new TreeSet<>();
    model.forEach(e -> {
      String bandName = e.getBand().trim();
      if (!StringUtils.isEmpty(bandName)) {
        artistNames.add(bandName);
      }
    });

    try (PrintWriter pw = new PrintWriter(new File(prop.getProperty("artist_output_file")))) {
      this.pw = pw;

      Artist artist = new Artist();
      artist.setQueryWs(new MyHttpWSImpl());
      artist.getSearchFilter().setLimit(5L);

      String newLine = System.lineSeparator();
      for (String band : artistNames) {
        String formattedString = band.replace(' ', '+');
        String searchString = formattedString.replaceAll("!", "");

        pw.println(StringUtils.repeat("-", 100));
        pw.println(searchString);
        System.out.print("query: " + searchString);
        artist.search(searchString);
        List<ArtistResultWs2> resultList = artist.getFirstSearchResultPage();
        pw.println(StringUtils.repeat("-", 100));

        Iterator<ArtistResultWs2> iter = resultList.iterator();
        while (iter.hasNext()) {
          ArtistResultWs2 at = iter.next();
          ArtistWs2 artistEntity = at.getArtist();
          pw.format("%-50s%-35s%-5s\n", "id: " + artistEntity.getId(), "name: " + artistEntity.getName(), "country: " + artistEntity.getCountry());
        }
        pw.println(StringUtils.repeat("-", 100));
        pw.println(newLine);
      }
    }
  }

  private String formatString(String band) {
    String formattedString = band.replace(' ', '+');
    String searchString = formattedString.replaceAll("!", "");
    return searchString;
  }

  public PrintWriter getPw() {
    return pw;
  }

  public static MusicBrainzWSDemo getInstance() {
    return instance;
  }
}
