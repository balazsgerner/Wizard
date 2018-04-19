package application.query;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import model.MusicFile;

public class SpotifyQuery extends Query {

  private static final int LIMIT = 10;

  private static final int OFFSET = 0;

  public static final String CODE = "SPOTIFY";

  private SpotifyApi spotifyApi;

  private ClientCredentialsRequest clientCredentialsRequest;

  private Properties prop;

  @Override
  protected void init() {
    prop = new Properties();
    try {
      prop.load(getClass().getResourceAsStream("/resources/properties/spotify.properties"));

      String clientId = prop.getProperty("client_id");
      String clientSecret = prop.getProperty("client_secret");
      spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();
      clientCredentialsRequest = spotifyApi.clientCredentials().build();

      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    } catch (IOException | SpotifyWebApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void performQuery(MusicFile mf) {
    super.performQuery(mf);

    String band = musicFile.getBand();
    String title = musicFile.getTitle();
    String searchString = "";
    if (!StringUtils.isEmpty(band)) {
      searchString += "artist:" + band;
    }

    if (!StringUtils.isEmpty(title)) {
      searchString += " + title:" + title;
    }

    System.out.println(searchString);

    SearchTracksRequest trackRequest = spotifyApi.searchTracks(searchString).offset(OFFSET).limit(LIMIT).build();
    Paging<Track> paging;
    try {
      paging = trackRequest.execute();
      for (Track track : paging.getItems()) {
        System.out.format("%-35s%-50s%-5s\n", "id: " + track.getId(), "name: " + track.getName(), "album: " + track.getAlbum().getName());
      }
    } catch (SpotifyWebApiException | IOException e) {
      e.printStackTrace();
    }
  }

}
