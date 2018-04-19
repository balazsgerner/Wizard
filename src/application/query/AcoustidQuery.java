package application.query;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import model.MusicFile;

public class AcoustidQuery extends Query {

  private ProcessBuilder pb;

  public static final String CODE = "ACOUSTID";

  @Override
  protected void init() {
    URL url = getClass().getResource("/resources/binaries/fpcalc.exe");
    pb = new ProcessBuilder(url.getPath(), musicFile.getPath());
  }

  @Override
  public void performQuery(MusicFile mf) {
    super.performQuery(mf);
    try {
      Process p = pb.start();
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = null;
      line = br.readLine();
      String duration = line.split("DURATION=")[1];
      line = br.readLine();
      String fingerPrint = line.split("FINGERPRINT=")[1];
      buildURL(duration, fingerPrint);
    } catch (IOException | UnirestException e) {
      e.printStackTrace();
    }
  }

  private void buildURL(String duration, String fingerPrint) throws IOException, UnirestException {
    Properties prop = new Properties();
    prop.load(getClass().getResourceAsStream("/resources/properties/acoustid.properties"));
    HttpResponse<JsonNode> response = Unirest.get(prop.getProperty("acoustid_url")).header("accept", "application/json")
        .queryString("client", prop.getProperty("acoustid_client_key")).queryString("duration", duration).queryString("fingerprint", fingerPrint)
        .queryString("meta", prop.getProperty("acoustid_meta")).asJson();

    results = new HashMap<String, Map<String, Object>>();
    JSONObject jsonObject = new JSONObject(response.getBody().toString());
    JSONArray resultsArray = jsonObject.getJSONArray("results");

    for (int i = 0; i < resultsArray.length(); i++) {
      JSONObject track = resultsArray.getJSONObject(i);
      try {
        JSONArray recordings = track.getJSONArray("recordings");
        for (int j = 0; j < recordings.length(); j++) {

          Map<String, Object> recordingData = new HashMap<>();
          JSONObject recording = recordings.getJSONObject(j);

          String recordingId = recording.getString("id");
          recordingData.put("recordingid", recordingId);

          JSONArray artists = recording.getJSONArray("artists");
          List<String> artistNames = new ArrayList<>();
          for (int k = 0; k < artists.length(); k++) {
            JSONObject artist = artists.getJSONObject(k);
            artistNames.add(artist.getString("name"));
          }
          recordingData.put("Artist", String.join(", ", artistNames));

          try {
            int drtn = recording.getInt("duration");
            String drtnStr = String.format("%02d:%02d", drtn / 60, drtn % 60);
            recordingData.put("duration", drtnStr);
          } catch (JSONException e) {
          } finally {
            try {
              recordingData.put("recording title", recording.getString("title"));
            } finally {
              results.put(recordingId, recordingData);
            }
          }
        }

      } catch (JSONException e) {
      }
    }

  }

}
