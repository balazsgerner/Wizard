package musicbrainz;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.musicbrainz.controller.Artist;
import org.musicbrainz.model.entity.ArtistWs2;
import org.musicbrainz.model.searchresult.ArtistResultWs2;

import model.MusicFile;
import persistence.LibraryScannerUtilMock;

public class MusicBrainzWSDemo {

  private LibraryScannerUtilMock libraryScanner = new LibraryScannerUtilMock();

  public static void main(String[] args) {
    MusicBrainzWSDemo instance = new MusicBrainzWSDemo();
    try {
      instance.queryMB();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void queryMB() throws IOException {
    libraryScanner.scanLibrary(new File(LibraryScannerUtilMock.LIBRARY_PATH));
    List<MusicFile> model = libraryScanner.getModel();
    Set<String> artistNames = new TreeSet<>();
    model.forEach(e -> artistNames.add(e.getBand()));

    Artist artist = new Artist();
    artist.setQueryWs(new MyHttpWSImpl());
    artist.getSearchFilter().setLimit(5L);
    artist.getSearchFilter().setMinScore(85L);

    for (String band : artistNames) {
      System.out.println("++++++++++++++++++++++++++++++++++++++");
      System.out.println(band);
      band = band.replace(' ', '+');
      artist.search(band.contains("!") ? getQuotedString(band) : band);
      List<ArtistResultWs2> resultList = artist.getFirstSearchResultPage();
      Iterator<ArtistResultWs2> iter = resultList.iterator();
      while (iter.hasNext()) {
        ArtistResultWs2 at = iter.next();
        ArtistWs2 artistEntity = at.getArtist();
        System.out.println("\tid: " + artistEntity.getId() + " " + artistEntity.getName() + " " + artistEntity.getCountry());
      }
      System.out.println("++++++++++++++++++++++++++++++++++++++\n");
    }
  }

  private String getQuotedString(String string) {
    return "\"" + string + "\"";
  }

}
