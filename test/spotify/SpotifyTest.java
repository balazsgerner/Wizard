package spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;

import model.MusicFile;
import persistence.LibraryScannerUtilMock;

public class SpotifyTest extends LibraryScannerUtilMock {

  private SpotifyApi spotifyApi;

  private ClientCredentialsRequest clientCredentialsRequest;

  private Properties prop;

  public static void main(String[] args) {
    disableWarning();
    SpotifyTest st = new SpotifyTest();
    try {
      st.querySpotify();
    } catch (IOException | SpotifyWebApiException e) {
      e.printStackTrace();
    }
  }

  public static void disableWarning() {
    Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    AudioFile.logger.setLevel(Level.OFF);
    System.err.close();
    System.setErr(System.out);
  }

  private void initClient() throws IOException, SpotifyWebApiException {
    prop = new Properties();
    prop.load(getClass().getResourceAsStream("/resources/properties/spotify.properties"));

    String clientId = prop.getProperty("client_id");
    String clientSecret = prop.getProperty("client_secret");
    spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();
    clientCredentialsRequest = spotifyApi.clientCredentials().build();

    ClientCredentials clientCredentials = clientCredentialsRequest.execute();
    spotifyApi.setAccessToken(clientCredentials.getAccessToken());
  }

  private void querySpotify() throws IOException, SpotifyWebApiException {
    initClient();
    scanLibrary(new File(LibraryScannerUtilMock.LIBRARY_PATH));

    try (PrintWriter pw = new PrintWriter(new File(prop.getProperty("spotify_output_file")))) {
      int num = 0;
      for (MusicFile mf : model) {
        String band = mf.getBand();
        String title = mf.getTitle();
        pw.println(StringUtils.repeat('-', 100));
        String searchStr = "artist:" + band + " + title:" + title;
        String currentFileStr = "#" + (num++) + " " + (!StringUtils.isEmpty(band) ? searchStr : "[empty]");
        pw.println(currentFileStr);
        System.out.println(currentFileStr);
        pw.println(StringUtils.repeat('-', 100));

        if (StringUtils.isEmpty(band)) {
          continue;
        }

        SearchTracksRequest trackRequest = spotifyApi.searchTracks(searchStr).offset(0).limit(10).build();
        Paging<Track> paging = trackRequest.execute();
        for (Track track : paging.getItems()) {
          pw.format("%-35s%-50s%-5s\n", "id: " + track.getId(), "name: " + track.getName(), "album: " + track.getAlbum().getName());
        }
        pw.println(StringUtils.repeat('-', 100));
        pw.println(System.lineSeparator());
      }
    }
  }

}
