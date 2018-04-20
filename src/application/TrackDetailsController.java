package application;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import application.query.Query;
import application.query.QueryUtility;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
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
  private Label lblQueryResults;

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

  @FXML
  private ListView<String> listQueryResults;

  @FXML
  private TableView<Map.Entry<String, Object>> tblResults;

  @FXML
  private TableColumn<Map.Entry<String, Object>, String> resValueColumn;

  @FXML
  private TableColumn<Map.Entry<String, Object>, String> resAttributeColumn;

  @FXML
  private TableView<Map.Entry<String, Object>> tblOriginal;

  @FXML
  private TableColumn<Map.Entry<String, Object>, String> orgValueColumn;

  @FXML
  private TableColumn<Map.Entry<String, Object>, String> orgAttributeColumn;

  private Parent callerWindowRoot;

  private TrackDetailsParamBean paramBean;

  private Map<String, Map<String, Object>> results;

  private Image originalImg;

  public TrackDetailsController(Parent callerWindowRoot, TrackDetailsParamBean parambean) {
    this.callerWindowRoot = callerWindowRoot;
    this.paramBean = parambean;
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    setTrackData();
    initQueryMethods();
    initTableColumns();
    initTblOriginal();
    initTblResults();
  }

  private void initTblOriginal() {
    Map<String, Object> attibuteMap = paramBean.musicFile.getAttibuteMap();
    tblOriginal.setItems(FXCollections.observableArrayList(attibuteMap.entrySet()));
  }

  private void initTblResults() {
    listQueryResults.getSelectionModel().selectedItemProperty().addListener(e -> refreshValuesInTable());
    tblResults.getSelectionModel().selectedItemProperty().addListener(e -> refreshImageIfNeeded());
  }

  private void refreshImageIfNeeded() {
    Entry<String, Object> selectedItem = tblResults.getSelectionModel().getSelectedItem();
    if (selectedItem != null && selectedItem.getKey().contains("image")) {
      if (originalImg == null) {
        originalImg = imgAlbum.getImage();
      }
      String imgUrl = (String) selectedItem.getValue();
      imgAlbum.setImage(new Image(imgUrl));
    } else if (originalImg != null && !imgAlbum.getImage().equals(originalImg)) {
      imgAlbum.setImage(originalImg);
    }
  }

  private void initTableColumns() {
    Callback<TableColumn.CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>> mapKeyColumnFactory = createMapKeyColumnFactory();
    Callback<TableColumn.CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>> mapValueColumnFactory = createMapValueColumnFactory();
    resAttributeColumn.setCellValueFactory(mapKeyColumnFactory);
    resValueColumn.setCellValueFactory(mapValueColumnFactory);
    resValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    orgAttributeColumn.setCellValueFactory(mapKeyColumnFactory);
    orgValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    orgValueColumn.setCellValueFactory(mapValueColumnFactory);

  }

  private Callback<CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>> createMapValueColumnFactory() {
    return new Callback<TableColumn.CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>>() {

      @Override
      public ObservableValue<String> call(CellDataFeatures<Entry<String, Object>, String> param) {
        Object value = param.getValue().getValue();
        if (value == null) {
          return new SimpleStringProperty(StringUtils.EMPTY);
        }
        return new SimpleStringProperty(value.toString());
      }
    };
  }

  private Callback<CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>> createMapKeyColumnFactory() {
    return new Callback<TableColumn.CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>>() {

      @Override
      public ObservableValue<String> call(CellDataFeatures<Entry<String, Object>, String> param) {
        String key = param.getValue().getKey();
        return new SimpleStringProperty(key.substring(0, 1).toUpperCase() + key.substring(1));
      }
    };
  }

  private void initQueryMethods() {
    QueryUtility queryUtility = QueryUtility.getInstance();
    queryUtility.getQueryMethods().forEach(q -> {
      MenuItem menuItem = new MenuItem(q.getName());
      btnPerformQuery.getItems().add(menuItem);
      menuItem.setOnAction(e -> {
        Task<Void> queryTask = new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            performQuery(queryUtility, q);
            return null;
          }

          @Override
          protected void succeeded() {
            Set<String> keySet = results.keySet();
            listQueryResults.setItems(FXCollections.observableArrayList(keySet));
            boolean isempty = keySet.isEmpty();
            if (!isempty) {
              listQueryResults.getSelectionModel().select(0);
            } else {
              Label placeHolder = new Label("No results found for track!");
              placeHolder.getStyleClass().add("placeHolder");
              listQueryResults.setPlaceholder(placeHolder);
            }
            lblQueryResults.setText(q.getName() + " query results");
          }
        };

        Thread thread = new Thread(queryTask);
        thread.setDaemon(true);
        thread.start();

      });
    });
  }

  private void performQuery(QueryUtility queryUtility, Query q) {
    results = queryUtility.performQuery(paramBean.musicFile, q);
  }

  private void refreshValuesInTable() {
    String selectedId = listQueryResults.getSelectionModel().getSelectedItem();
    if (selectedId != null) {
      tblResults.setItems(FXCollections.observableArrayList(results.get(selectedId).entrySet()));
      tblResults.getSortOrder().add(resAttributeColumn);
    } else {
      tblResults.setItems(null);
    }
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
