package application.query.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import application.Main;
import application.query.Query;
import model.MusicFile;

public class SpotifyQuery extends Query {

  private static final int LIMIT = 10;

  private static final int OFFSET = 0;

  public static final String CODE = "SPOTIFY";

  private SpotifyApi spotifyApi;

  private ClientCredentialsRequest clientCredentialsRequest;

  private Properties prop;

  private int imgNum;

  public SpotifyQuery(Query query) {
    super(query);
  }

  @Override
  protected void init() {
    prop = new Properties();
    try {
      prop.load(Main.class.getResourceAsStream("/resources/properties/spotify.properties"));

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
  public void performQuery(MusicFile mf) throws ConnectException {
    Logger.getRootLogger().setLevel(Level.OFF);
    super.performQuery(mf);
    Logger.getRootLogger().setLevel(Level.DEBUG);
  }

  @Override
  protected void fillResultsMap(String searchString) {
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
        results.put(trackId, attributes);
      }
    } catch (SpotifyWebApiException | IOException e) {
      e.printStackTrace();
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
