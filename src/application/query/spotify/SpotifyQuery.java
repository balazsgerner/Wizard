package application.query.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadGatewayException;
import com.wrapper.spotify.exceptions.detailed.NotFoundException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import application.controller.Main;
import application.query.Query;
import application.query.QueryUtility;

public class SpotifyQuery extends Query {

  private static final int LIMIT = 10;

  private static final int OFFSET = 0;

  private static final int DEFAULT_RETRY_AFTER = 3;

  public static final String CODE = "SPOTIFY";

  private SpotifyApi spotifyApi;

  private ClientCredentialsRequest clientCredentialsRequest;

  private Properties prop;

  private int imgNum;

  private int errorNum = 0;

  public SpotifyQuery(Query query) throws ConnectException {
    super(query);
  }

  @Override
  protected void init() throws ConnectException {
    prop = new Properties();
    try {
      try {
        prop.load(Main.class.getResourceAsStream("/resources/properties/spotify.properties"));
      } catch (IOException e) {
        log.error("Error while loading properties!", e);
      }

      String clientId = prop.getProperty("client_id");
      String clientSecret = prop.getProperty("client_secret");
      spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();
      clientCredentialsRequest = spotifyApi.clientCredentials().build();

      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    } catch (SpotifyWebApiException | IOException e) {
      throw new ConnectException("Cannot connect to web service!");
    }
  }

  @Override
  protected void fillResultsMap(String searchString) throws ConnectException {
    SearchTracksRequest trackRequest = spotifyApi.searchTracks(searchString).offset(OFFSET).limit(LIMIT).build();
    try {
      Paging<Track> paging = trackRequest.execute();
      for (Track track : paging.getItems()) {
        Map<String, Object> attributes = new HashMap<>();

        ArtistSimplified[] artists = track.getArtists();
        String artistsStr = getListOfArtists(artists);

        attributes.put("artists", artistsStr);
        attributes.put("album", track.getAlbum().getName());
        ArtistSimplified[] albumArtists = track.getAlbum().getArtists();
        String albumArtistsStr = getListOfArtists(albumArtists);
        attributes.put("album artist", albumArtistsStr);

        List<Image> images = Arrays.asList(track.getAlbum().getImages());
        imgNum = 0;
        images.forEach(e -> {
          attributes.put("image #" + (imgNum++), e.getUrl());
        });

        attributes.put("album type", track.getAlbum().getType());
        List<CountryCode> availableMarkets = Arrays.asList(track.getAvailableMarkets());
        List<String> countryCodes = new ArrayList<>();
        availableMarkets.forEach(a -> countryCodes.add(a.getAlpha2()));
        String countryCodesStr = String.join(", ", countryCodes);

        attributes.put("available markets", countryCodesStr);
        attributes.put("disc number", track.getDiscNumber());

        Integer durationMs = track.getDurationMs();
        int drtn = durationMs / 1000;
        String drtnStr = String.format("%02d:%02d", drtn / 60, drtn % 60);
        attributes.put("duration", drtnStr);

        attributes.put("explicit", track.getIsExplicit());
        attributes.put("isrc", track.getExternalIds().getExternalIds().get("isrc"));
        String trackId = track.getId();
        attributes.put("track id", trackId);
        attributes.put("title", track.getName());
        attributes.put("track number", track.getTrackNumber());
        result.put(trackId, attributes);
      }
    } catch (TooManyRequestsException e) {
      int retryAfter = e.getRetryAfter();
      QueryUtility.log.error("Too many requests was sent to the server! Retry After: " + retryAfter, e);
      retry(retryAfter, searchString);
    } catch (BadGatewayException e) {
      QueryUtility.log.error("Bad Gateway!", e);
      retry(0, searchString);
    } catch (NotFoundException e) {
      log.warn("#" + (errorNum++) + " Track not found!\n"
          + String.format("%17s%s%n%17s%s%n%17s%s%n%17s%s", "", "Search string: ", "", searchString, "", "MusicFile: ", "", musicFile.toString()));
    } catch (SpotifyWebApiException | IOException e) {
      throw new ConnectException(e.getMessage());
    }

  }

  /**
   * Retry calling the web service. Is needed when e.g. TooManyRequestException or BadGateway exception occurs.
   * 
   * @param waitSec - How many seconds until next try
   * @param searchString - search params
   * @throws ConnectException
   */
  private void retry(Integer waitSec, String searchString) throws ConnectException {
    try {
      Thread.sleep((waitSec != null ? waitSec : DEFAULT_RETRY_AFTER) * 1000);
      fillResultsMap(searchString);
    } catch (InterruptedException e1) {
    }
  }

  @Override
  protected String createSearchStr() {
    String band = musicFile.getBand();
    String title = removeFeaturing(musicFile.getTitle());
    String searchString = "";

    if (!StringUtils.isEmpty(band)) {
      searchString += "artist:" + band;
    }

    if (!StringUtils.isEmpty(title)) {
      searchString += " title:" + title;
    }
    return searchString;
  }

  private String removeFeaturing(String title) {
    return title.replaceAll("\\((feat\\.|Feat\\.|featuring|Featuring).*?\\)", "").trim();
  }

  private String getListOfArtists(ArtistSimplified[] artists) {
    List<ArtistSimplified> artistsList = Arrays.asList(artists);
    List<String> artistNames = new ArrayList<>();
    artistsList.forEach(e -> artistNames.add(e.getName()));
    String artistsStr = String.join(", ", artistNames);
    return artistsStr;
  }

}
