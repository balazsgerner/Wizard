package application;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.MusicFile;

public class TrackDetailsController implements Initializable {

  @FXML
  private Parent root;

  @FXML
  private Button backBtn;

  @FXML
  private Label lblPath;

  @FXML
  private Label lblArtist;

  @FXML
  private Label lblTitle;

  @FXML
  private Label lblAlbum;

  @FXML
  private Label lblYear;

  @FXML
  private Label lblGenre;

  @FXML
  private ImageView imgAlbum;

  private MusicFile musicFile;

  private Parent callerWindowRoot;

  public TrackDetailsController(Parent callerWindowRoot, MusicFile musicFile) {
    this.callerWindowRoot = callerWindowRoot;
    this.musicFile = musicFile;
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    setTrackData();
  }

  private void setTrackData() {
    String path = validateData(musicFile.getPath());
    lblPath.setText(path);
    lblPath.setTooltip(new Tooltip(path));
    String band = validateData(musicFile.getBand());
    lblArtist.setText(band);
    lblArtist.setTooltip(new Tooltip(band));
    String title = validateData(musicFile.getTitle());
    lblTitle.setText(title);
    lblTitle.setTooltip(new Tooltip(title));
    String album = validateData(musicFile.getAlbum());
    lblAlbum.setText(album);
    lblAlbum.setTooltip(new Tooltip(album));
    String year = validateData(musicFile.getYear());
    lblYear.setText(year);
    lblYear.setTooltip(new Tooltip(year));
    String genre = validateData(musicFile.getGenre());
    lblGenre.setText(genre);
    lblGenre.setTooltip(new Tooltip(genre));
    imgAlbum.setImage(new Image(new ByteArrayInputStream(musicFile.getArtwork())));
  }

  private String validateData(String rawData) {
    return rawData == null || rawData.isEmpty() ? "- (Unknown)" : rawData;
  }

  @FXML
  private void backToMainView() {
    root.getScene().setRoot(callerWindowRoot);
  }

}
