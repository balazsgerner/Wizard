package application.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import model.MusicFile;

public class AcoustidQuery extends Query {

  private ProcessBuilder pb;
  
  public static final String CODE = "ACOUSTID";

  @Override
  public void init(MusicFile mf) {
    super.init(mf);
    URL url = getClass().getResource("/resources/binaries/fpcalc.exe");
    pb = new ProcessBuilder(url.getPath(), musicFile.getPath());
  }

  @Override
  public void performQuery() throws Exception {
    if (!initialized) {
      return;
    }

    Process p = pb.start();
    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = null;
    line = br.readLine();
    String duration = line.split("DURATION=")[1];
    System.out.println(line);
    line = br.readLine();
    String fingerPrint = line.split("FINGERPRINT=")[1];
    System.out.println(line);
    buildURL(duration, fingerPrint);
  }

  private void buildURL(String duration, String fingerPrint) throws IOException, UnirestException {
    Properties prop = new Properties();
    prop.load(getClass().getResourceAsStream("/resources/properties/acoustid.properties"));
    HttpRequest queryString = Unirest.get(prop.getProperty("acoustid_url")).header("accept", "application/json")
        .queryString("client", prop.getProperty("acoustid_client_key")).queryString("duration", duration).queryString("fingerprint", fingerPrint)
        .queryString("meta", prop.getProperty("acoustid_meta"));
    queryString.asJsonAsync(new Callback<JsonNode>() {

      @Override
      public void completed(HttpResponse<JsonNode> response) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(response.getBody().toString());
        String prettyJsonString = gson.toJson(je);
        System.out.println(prettyJsonString);
      }

      @Override
      public void failed(UnirestException e) {
      }

      @Override
      public void cancelled() {
      }
    });

  }

}