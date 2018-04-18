package application;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import application.query.QueryUtility;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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
  private Button btnAssignId;

  @FXML
  private Button btnSelectId;

  @FXML
  private MenuButton btnPerformQuery;

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
  private Label lblTrackLength;

  @FXML
  private Label lblBitRate;

  @FXML
  private Label lblSampleRate;

  @FXML
  private ImageView imgAlbum;

  private Parent callerWindowRoot;

  private TrackDetailsParamBean paramBean;

  public TrackDetailsController(Parent callerWindowRoot, TrackDetailsParamBean parambean) {
    this.callerWindowRoot = callerWindowRoot;
    this.paramBean = parambean;
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    setTrackData();
    initQueryMethods();
    
    btnPerformQuery.setOnAction(e -> performQuery());
  }

  private void performQuery() {
  }

  private void initQueryMethods() {
    QueryUtility queryUtility = QueryUtility.getInstance();
    queryUtility.getQueryMethods().forEach(q -> {
      MenuItem menuItem = new MenuItem(q.getName());
      menuItem.setOnAction(e -> queryUtility.performQuery(paramBean.musicFile, q.getCode()));
      btnPerformQuery.getItems().add(menuItem);
    });
  }

  private void setTrackData() {
    setCurrentTrackData();
    setAlbumData();
  }

  private void setAlbumData() {
    MusicFile selectedItem = paramBean.musicFile;
    String album = validateData(selectedItem.getAlbum());
    lblAlbum.setText(album);
    lblAlbum.setTooltip(new Tooltip(album));
    String year = validateData(selectedItem.getYear());
    lblYear.setText(year);
    lblYear.setTooltip(new Tooltip(year));
    String genre = validateData(selectedItem.getGenre());
    lblGenre.setText(genre);
    lblGenre.setTooltip(new Tooltip(genre));
    byte[] artwork = selectedItem.getArtwork();
    if (artwork != null) {
      imgAlbum.setImage(new Image(new ByteArrayInputStream(artwork)));
    }
  }

  private void setCurrentTrackData() {
    MusicFile selectedItem = paramBean.musicFile;
    String path = validateData(selectedItem.getPath());
    lblPath.setText(path);
    lblPath.setTooltip(new Tooltip(path));
    String band = validateData(selectedItem.getBand());
    lblArtist.setText(band);
    lblArtist.setTooltip(new Tooltip(band));
    String title = validateData(selectedItem.getTitle());
    lblTitle.setText(title);
    lblTitle.setTooltip(new Tooltip(title));
    String genre = selectedItem.getGenre();
    lblGenre.setText(genre);
    lblGenre.setTooltip(new Tooltip(genre));
    String trackLength = selectedItem.getTrackLength();
    lblTrackLength.setText(trackLength);
    String bitRate = selectedItem.getBitRate();
    lblBitRate.setText(bitRate);
    String sampleRate = selectedItem.getSampleRate();
    lblSampleRate.setText(sampleRate);
  }

  private String validateData(String rawData) {
    return rawData == null || rawData.isEmpty() ? "- (Unknown)" : rawData;
  }

  @FXML
  private void backToMainView() {
    root.getScene().setRoot(callerWindowRoot);
  }

}
