package application;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
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

  @FXML
  private TableView<MusicFile> albumDetails;

  private Parent callerWindowRoot;

  private TrackDetailsParamBean paramBean;

  public TrackDetailsController(Parent callerWindowRoot, TrackDetailsParamBean parambean) {
    this.callerWindowRoot = callerWindowRoot;
    this.paramBean = parambean;
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    initTableView();
    setTrackData();
  }

  @SuppressWarnings("unchecked")
  private void initTableView() {
    TableColumn<MusicFile, String> trackColumn = new TableColumn<>("Track");
    trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));
    TableColumn<MusicFile, String> bandColumn = new TableColumn<>("Artist");
    bandColumn.setCellValueFactory(new PropertyValueFactory<>("band"));
    TableColumn<MusicFile, String> titleColumn = new TableColumn<>("Title");
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    TableColumn<MusicFile, String> albumColumn = new TableColumn<>("Album");
    albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
    TableColumn<MusicFile, String> genreColumn = new TableColumn<>("Genre");
    genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
    TableColumn<MusicFile, String> yearColumn = new TableColumn<>("Year");
    yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
    albumDetails.setItems(FXCollections.observableArrayList(paramBean.tracksFromSameAlbum));
    albumDetails.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    albumDetails.getColumns().addAll(trackColumn, bandColumn, titleColumn, albumColumn, yearColumn, genreColumn);
    albumDetails.getSortOrder().add(trackColumn);
    albumDetails.getSelectionModel().select(paramBean.musicFile);
    albumDetails.getSelectionModel().selectedItemProperty().addListener(e -> setCurrentTrackData());
  }

  private void setTrackData() {
    setCurrentTrackData();
    setAlbumData();
  }

  private void setAlbumData() {
    MusicFile selectedItem = albumDetails.getSelectionModel().getSelectedItem();
    String album = validateData(selectedItem.getAlbum());
    lblAlbum.setText(album);
    lblAlbum.setTooltip(new Tooltip(album));
    String year = validateData(selectedItem.getYear());
    lblYear.setText(year);
    lblYear.setTooltip(new Tooltip(year));
    String genre = validateData(selectedItem.getGenre());
    lblGenre.setText(genre);
    lblGenre.setTooltip(new Tooltip(genre));
    imgAlbum.setImage(new Image(new ByteArrayInputStream(selectedItem.getArtwork())));
  }

  private void setCurrentTrackData() {
    MusicFile selectedItem = albumDetails.getSelectionModel().getSelectedItem();
    String path = validateData(selectedItem.getPath());
    lblPath.setText(path);
    lblPath.setTooltip(new Tooltip(path));
    String band = validateData(selectedItem.getBand());
    lblArtist.setText(band);
    lblArtist.setTooltip(new Tooltip(band));
    String title = validateData(selectedItem.getTitle());
    lblTitle.setText(title);
    lblTitle.setTooltip(new Tooltip(title));
  }

  private String validateData(String rawData) {
    return rawData == null || rawData.isEmpty() ? "- (Unknown)" : rawData;
  }

  @FXML
  private void backToMainView() {
    root.getScene().setRoot(callerWindowRoot);
  }

}
