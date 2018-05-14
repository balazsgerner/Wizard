
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import application.query.QueryUtility;
import application.query.acoustid.AcoustidQuery;
import application.query.spotify.SpotifyQuery;
import model.MusicFile;
import persistence.DBManager;

public class QueryTest {

  private static QueryUtility qUtility;

  private static final String path = "/resource/Moderator - Words Remain.flac";

  private static MusicFile mf;

  @BeforeClass
  public static void initAll() throws Exception {
    disableWarning();
    Properties prop = new Properties();
    prop.load(QueryTest.class.getResourceAsStream("/resources/properties/application.properties"));
    qUtility = QueryUtility.getInstance();
    JAXBContext jaxbContext = JAXBContext.newInstance(QueryUtility.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    QueryUtility qm = (QueryUtility) jaxbUnmarshaller.unmarshal(QueryTest.class.getResource(prop.getProperty("querymethods.url")));
    qUtility.setQueryMethods(qm.getQueryMethods());
  }

  @Before
  public void createMusicFile() throws Exception {
    mf = new MusicFile(new File(QueryTest.class.getResource(path).toURI()));
  }

  private static void disableWarning() {
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
    AudioFile.logger.setLevel(Level.OFF);
  }

  /**
   * Ha offline állapotban vagyunk, vagy nem lehet csatlakozni egy web szolgáltatáshoz, akkor a lekérdezésnek meg kell
   * szakadnia egy ConnectException-nel
   * 
   * @throws ConnectException
   */
  @Test(expected = ConnectException.class)
  public void testNoConnection() throws ConnectException {
    assumeFalse(netIsAvailable());
    qUtility.performQuery(mf, qUtility.getQueryMethodByCode(AcoustidQuery.CODE));
  }

  /**
   * Ha online állapotban vagyunk, és sikeresen lefut a keresés, akkor a lekérdezés lefutása után azok eredménye bele
   * kell, hogy íródjon a zenefájlba.
   * 
   * @throws ConnectException
   */
  @Test
  public void testSuccessfulQuery() throws ConnectException {
    assumeTrue(netIsAvailable());
    assumeTrue(mf.getAllQueryResults() == null);

    qUtility.performQuery(mf, qUtility.getQueryMethodByCode(SpotifyQuery.CODE));
    assertFalse(mf.getAllQueryResults().isEmpty());
  }

  /**
   * Adatbázisba írja a zenéhez tartozó lekérdezések eredményét, majd visszaolvassa egy újonnan létrehozott fájlba. A
   * folyamat végén nem lehet üres az új fájl eredménylistája.
   * 
   * @throws Exception
   */
  @Test
  public void testWriteResultsToDatabase() throws Exception {
    qUtility.performQuery(mf, qUtility.getQueryMethodByCode(SpotifyQuery.CODE));
    DBManager dbManager = DBManager.getInstance(true);
    dbManager.saveMusicFile(mf);

    MusicFile anotherFile = new MusicFile(new File(mf.getPath()));
    dbManager.loadMusicFiles(Arrays.asList(anotherFile));
    assertFalse(anotherFile.getAllQueryResults().isEmpty());
  }

  private boolean netIsAvailable() {
    try {
      final URL url = new URL("http://www.google.com");
      URLConnection conn = url.openConnection();
      conn.connect();
      conn.getInputStream().close();
      return true;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      return false;
    }
  }

}
