package application.query.acoustid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import application.query.Query;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

public class AcoustidQuery extends Query {

  private ProcessBuilder pb;

  public static final String CODE = "ACOUSTID";

  private String duration;

  private String fingerPrint;

  private Properties prop;

  private File tmpFile;

  private HttpClient httpClient;

  public AcoustidQuery(Query query) throws ConnectException {
    super(query);
  }

  @Override
  protected void init() throws ConnectException {
    try {
      httpClient = HttpClient.newHttpClient();
      prop = new Properties();
      prop.load(getClass().getResourceAsStream("/resources/properties/acoustid.properties"));
      createTempFileForFpCalc();
    } catch (IOException | URISyntaxException e) {
      log.error("Error while initializing acoustid query!", e);
    }
  }

  /**
   * Creates a temp copy of fpcalc.exe resource. Needed when run inside jar files. The created file will be deleted on
   * exit.
   * 
   * @throws URISyntaxException
   * @throws IOException
   */
  private void createTempFileForFpCalc() throws URISyntaxException, IOException {
    InputStream is = getClass().getResourceAsStream(prop.getProperty("fpcalc.location"));
    tmpFile = File.createTempFile(prop.getProperty("fpcalc.name"), "." + prop.getProperty("fpcalc.extension"));
    tmpFile.deleteOnExit();
    IOUtils.copy(is, FileUtils.openOutputStream(tmpFile));
  }

  @Override
  protected String createSearchStr() {
    try {
      Process p = createProcess();
      readResults(p);
    } catch (Exception e) {
      log.error("Error while reading results!", e);
    }

    return null;
  }

  private void readResults(Process p) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = null;
    line = br.readLine();
    duration = line.split("DURATION=")[1];
    line = br.readLine();
    fingerPrint = line.split("FINGERPRINT=")[1];
  }

  private Process createProcess() throws IOException, InterruptedException {
    pb = new ProcessBuilder(tmpFile.getAbsolutePath(), musicFile.getPath());
    Process p = pb.start();
    p.waitFor();
    return p;
  }

  @Override
  protected void fillResultsMap(String searchString) throws ConnectException {
    try {
      HttpRequest request = HttpRequest.newBuilder(new URI("http", "api.acoustid.org", "/v2/lookup", buildQueryStr(), null)).build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandler.asString());
      
      JSONObject jsonObject = new JSONObject(response.body());
      Optional<JSONArray> optResultsArray = Optional.ofNullable(jsonObject.optJSONArray("results"));
      if(!optResultsArray.isPresent()) {
        log.error("Result array is empty, server response code: " + response.statusCode());
        return;
      }
      
      JSONArray resultArray = optResultsArray.get();
      for (int i = 0; i < resultArray.length(); i++) {
        Optional<JSONObject> optTrack = Optional.ofNullable(resultArray.optJSONObject(i));
        optTrack.ifPresent(track -> {
          Optional<JSONArray> optRecordings = Optional.of(track.optJSONArray("recordings"));
          optRecordings.ifPresent(recordings -> {
            for (int j = 0; j < recordings.length(); j++) {
              JSONObject recording = recordings.getJSONObject(j);
              String recordingId = recording.getString("id");
              var recordingData = createRecordingDataMap(recording, recordingId);
              result.put(recordingId, recordingData);
            }
          });
        });
      }
    } catch (Exception e1) {
      throw new ConnectException(e1.getMessage());
    }
  }

  private Map<String, Object> createRecordingDataMap(JSONObject recording, String recordingId) {
    var recordingData = new HashMap<String, Object>();
    recordingData.put("recordingid", recordingId);
    Optional<JSONArray> optArtists = Optional.of(recording.optJSONArray("artists"));
    optArtists.ifPresent(artists -> recordingData.put("Artist", artists.join(",")));
    int duration = recording.optInt("duration", 0);
    recordingData.put("duration", String.format("%02d:%02d", duration / 60, duration % 60));
    recordingData.put("recording title", recording.optString("title", ""));
    return recordingData;
  }

  private String buildQueryStr() {
    var paramMap = Map.of("client", prop.getProperty("acoustid_client_key"), "duration", duration, "fingerprint", fingerPrint, "meta",
        prop.getProperty("acoustid_meta"));
    List<String> paramList = paramMap.entrySet().stream().map(entry -> String.join("=", entry.getKey(), entry.getValue())).collect(Collectors.toList());
    return String.join("&", paramList);
  }

}
