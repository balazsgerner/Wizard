package musicbrainz;

import java.io.File;
import java.io.IOException;
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

import application.query.musicbrainz.MyHttpWSImpl;
import model.MusicFile;
import persistence.LibraryScannerUtilMock;

public class MusicBrainzWSDemo {

  private LibraryScannerUtilMock libraryScanner = new LibraryScannerUtilMock();

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
    System.out.println(model.size() + " songs found!");
    Set<String> artistNames = new TreeSet<>();
    model.forEach(e -> {
      String bandName = e.getBand().trim();
      if (!StringUtils.isEmpty(bandName)) {
        artistNames.add(bandName);
      }
    });

    Artist artist = new Artist();
    artist.setQueryWs(new MyHttpWSImpl());
    artist.getSearchFilter().setLimit(5L);

    String newLine = System.lineSeparator();
    for (String band : artistNames) {
      String formattedString = band.replace(' ', '+');
      String searchString = formattedString.replaceAll("!", "");

      System.out.println(StringUtils.repeat("-", 100));
      System.out.println(searchString);
      artist.search(searchString);
      List<ArtistResultWs2> resultList = artist.getFirstSearchResultPage();
      System.out.println(StringUtils.repeat("-", 100));

      Iterator<ArtistResultWs2> iter = resultList.iterator();
      while (iter.hasNext()) {
        ArtistResultWs2 at = iter.next();
        ArtistWs2 artistEntity = at.getArtist();
        System.out.format("%-50s%-35s%-5s\n", "id: " + artistEntity.getId(), "name: " + artistEntity.getName(),
            "country: " + artistEntity.getCountry());
      }
      System.out.println(StringUtils.repeat("-", 100));
      System.out.println(newLine);
    }
  }

}
