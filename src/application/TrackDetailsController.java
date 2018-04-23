package application;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import application.query.Query;
import application.query.QueryUtility;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
  private MenuButton btnLookukIsrc;

  @FXML
  private TextField txtIsrc;

  @FXML
  private MenuButton btnPerformQuery;

  @FXML
  private Label lblQueryResults;

  @FXML
  private Label lblQueryName;

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

  @FXML
  private ProgressIndicator progressIndicator;

  @FXML
  private Label lblQueryStatus;

  @FXML
  private Label lblQName;

  private Parent callerWindowRoot;

  private TrackDetailsParamBean paramBean;

  private Map<String, Map<String, Object>> results;

  private Image originalImg;

  private QueryService queryService;

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
    initTxtIsrc();
    initStatusBarComponents();
  }

  private void initStatusBarComponents() {
    lblQName.managedProperty().bind(lblQName.visibleProperty());
    lblQueryStatus.managedProperty().bind(lblQueryStatus.visibleProperty());
    progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
  }

  private void initTxtIsrc() {
    txtIsrc.textProperty().addListener(e -> disableButtonsIfEmpty());
  }

  private void disableButtonsIfEmpty() {
    boolean enabled;
    if (StringUtils.isEmpty(txtIsrc.getText())) {
      enabled = false;
    } else {
      enabled = true;
    }
    btnLookukIsrc.setDisable(!enabled);
  }

  private void initTblOriginal() {
    Map<String, Object> attibuteMap = paramBean.musicFile.getAttibuteMap();
    tblOriginal.setItems(FXCollections.observableArrayList(attibuteMap.entrySet()));
  }

  private void initTblResults() {
    MusicFile selectedFile = paramBean.musicFile;
    Map<String, Map<String, Object>> queryResults = selectedFile.getQueryResults();
    if (queryResults != null) {
      String lastQueryName = selectedFile.getLastQueryName();
      results = queryResults;
      Platform.runLater(() -> refreshUIAfterQuery(lastQueryName));
    }

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
    List<Query> queryMethods = queryUtility.getQueryMethods();
    queryMethods.stream().filter(q -> !q.isParametrized()).forEach(query -> {
      createMenuItemFromQuery(btnPerformQuery, query);
    });

    queryMethods.stream().filter(q -> q.isParametrized()).forEach(query -> {
      createMenuItemFromQuery(btnLookukIsrc, query);
    });
  }

  private void createMenuItemFromQuery(MenuButton button, Query query) {
    MenuItem menuItem = new MenuItem(query.getName());
    button.getItems().add(menuItem);
    menuItem.setOnAction(createQueryBackgroundTask(query));
  }

  private EventHandler<ActionEvent> createQueryBackgroundTask(Query query) {
    return e -> {
      runQueryInBackground(query);
    };
  }

  private QueryService getQueryService(Query query) {
    if (queryService == null) {
      queryService = new QueryService(query);
    } else {
      queryService.setQuery(query);
    }
    return queryService;
  }

  private void runQueryInBackground(Query query) {
    QueryService service = getQueryService(query);
    service.restart();

  }

  private void refreshUIAfterQuery(String queryName) {
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

    lblQueryName.setText(queryName + " ");
    lblQueryResults.setText(lblQueryResults.getText().toLowerCase());
  }

  private void performQuery(Query query) throws ConnectException {
    if (query.isParametrized()) {
      query.setParam("isrc", txtIsrc.getText());
    }
    results = QueryUtility.getInstance().performQuery(paramBean.musicFile, query);
  }

  private void refreshValuesInTable() {
    String selectedId = listQueryResults.getSelectionModel().getSelectedItem();
    if (selectedId != null) {
      btnAssignId.setDisable(false);
      Map<String, Object> attributeMap = results.get(selectedId);
      if (attributeMap.containsKey("isrc")) {
        String isrc = (String) attributeMap.get("isrc");
        txtIsrc.setText(isrc);
      } else {
        txtIsrc.setText(StringUtils.EMPTY);
      }
      tblResults.setItems(FXCollections.observableArrayList(attributeMap.entrySet()));
      tblResults.getSortOrder().add(resAttributeColumn);
    } else {
      txtIsrc.setText(StringUtils.EMPTY);
      btnAssignId.setDisable(true);
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

  private class QueryService extends Service<Void> {

    private Query query;

    public QueryService(Query query) {
      this.query = query;
      progressIndicator.progressProperty().bind(progressProperty());
    }

    @Override
    protected Task<Void> createTask() {
      return new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          initUI();
          performQuery(query);
          return null;
        }
      };
    }

    @Override
    protected void failed() {
      showError();
    }

    private void showError() {
      Platform.runLater(() -> {
        lblQueryStatus.getStyleClass().setAll("error");
        lblQueryStatus.setText("Cannot connect to web service!");
        progressIndicator.setVisible(false);
      });
    }

    private void initUI() {
      Platform.runLater(() -> {
        lblQName.setText(query.getName() + " query");
        lblQueryStatus.setText("");
        progressIndicator.setVisible(true);
      });
    }

    @Override
    protected void succeeded() {
      Platform.runLater(() -> {
        progressIndicator.setVisible(false);
        lblQueryStatus.getStyleClass().setAll("success");
        lblQueryStatus.setText("Finished!");
        refreshUIAfterQuery(query.getName());
      });
    }

    public void setQuery(Query query) {
      this.query = query;
    }
  }

}
