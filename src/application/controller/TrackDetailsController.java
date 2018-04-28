package application.controller;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import application.query.Query;
import application.query.QueryUtility;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SingleSelectionModel;
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
  private ComboBox<String> cmbQueryResultName;

  @FXML
  private Label lblQName;

  private Parent callerWindowRoot;

  private Map<String, Object> results;

  private Image originalImg;

  private QueryService queryService;

  private MusicFile musicFile;

  private Map<String, String> assignedIds;

  private ObservableList<String> listModel;

  public TrackDetailsController(Parent callerWindowRoot, MusicFile musicFile) {
    this.callerWindowRoot = callerWindowRoot;
    this.musicFile = musicFile;
    this.assignedIds = musicFile.getAssignedIds();
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    setTrackData();
    initAssignedIdListener();
    initSelectionListeners();
    initCmbQueryResultName();
    initQueryMethods();
    initTableColumns();
    initTblOriginal();
    initTxtIsrc();
    initStatusBarComponents();
  }

  private void initAssignedIdListener() {
    listModel = FXCollections.observableArrayList();
    listQueryResults.setItems(listModel);
    listQueryResults.setCellFactory(param -> new CustomCell());
  }

  private void initSelectionListeners() {
    // cmbQueryResultName
    SingleSelectionModel<String> selectionModel = cmbQueryResultName.getSelectionModel();
    selectionModel.selectedItemProperty().addListener(e -> {
      String qName = selectionModel.getSelectedItem();
      if (qName != null) {
        String qCode = QueryUtility.getInstance().getQueryCodeByName(qName);
        results = musicFile.getQueryResult(qCode);
      } else {
        results = new HashMap<>();
      }
      refreshUIAfterQuery(null);
    });

    // listQueryResults
    listQueryResults.getSelectionModel().selectedItemProperty().addListener(e -> refreshValuesInTable());

    // tblResults
    tblResults.getSelectionModel().selectedItemProperty().addListener(e -> refreshImageIfNeeded());

  }

  private void initCmbQueryResultName() {
    boolean empty = loadQueryResultNames();
    selectLatestQuery(empty);
  }

  private void selectLatestQuery(boolean empty) {
    Platform.runLater(() -> {
      if (!empty) {
        String lastQueryCode = musicFile.getLastQueryCode();
        String latestQueryName = QueryUtility.getInstance().getQueryNameByCode(lastQueryCode);
        cmbQueryResultName.getSelectionModel().select(latestQueryName);
      }
    });
  }

  /**
   * Újratölti a combobox modelljét
   * 
   * @return - üres-e az új modell
   */
  private boolean loadQueryResultNames() {
    QueryUtility qUtility = QueryUtility.getInstance();
    Map<String, Object> allQueryResults = musicFile.getAllQueryResults();

    if (allQueryResults == null) {
      cmbQueryResultName.setDisable(true);
      return true;
    }

    ObservableList<String> items = cmbQueryResultName.getItems();
    items.clear();
    allQueryResults.keySet().forEach(q -> {
      items.add(qUtility.getQueryNameByCode(q));
    });

    cmbQueryResultName.setDisable(false);
    return false;

  }

  private void initStatusBarComponents() {
    lblQName.managedProperty().bind(lblQName.visibleProperty());
    lblQueryStatus.managedProperty().bind(lblQueryStatus.visibleProperty());
    progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
  }

  private void initTxtIsrc() {
    btnLookukIsrc.disableProperty().bind(Bindings.isEmpty(txtIsrc.textProperty()));
  }

  private void initTblOriginal() {
    Map<String, Object> attibuteMap = musicFile.getAttributeMap();
    tblOriginal.setItems(FXCollections.observableArrayList(attibuteMap.entrySet()));
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
    return param -> {
      Entry<String, Object> mapEntry = param.getValue();
      Object value = mapEntry.getValue();
      String text = value == null ? StringUtils.EMPTY : value.toString();
      return new SimpleStringProperty(text);
    };
  }

  private Callback<CellDataFeatures<Entry<String, Object>, String>, ObservableValue<String>> createMapKeyColumnFactory() {
    return param -> {
      String key = param.getValue().getKey();
      return new SimpleStringProperty(key.substring(0, 1).toUpperCase() + key.substring(1));
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

  private void refreshUIAfterQuery(String select) {
    Platform.runLater(() -> {
      Set<String> keySet = results.keySet();
      listModel.setAll(keySet);

      if (listModel.isEmpty()) {
        Label placeHolder = new Label("No results found for track!");
        placeHolder.getStyleClass().add("placeHolder");
        listQueryResults.setPlaceholder(placeHolder);
      }

      MultipleSelectionModel<String> selectionModel = listQueryResults.getSelectionModel();
      if (select == null) {
        selectionModel.selectFirst();
      } else {
        selectionModel.select(select);
      }
    });
  }

  private void performQuery(Query query) throws ConnectException {
    if (query.isParametrized()) {
      query.setParam("isrc", txtIsrc.getText());
    }
    QueryUtility.getInstance().performQuery(musicFile, query);
  }

  private void refreshValuesInTable() {
    Platform.runLater(() -> {
      String selectedId = listQueryResults.getSelectionModel().getSelectedItem();

      if (selectedId != null) {
        boolean enabled = true;
        if (assignedIds != null && assignedIds.containsValue(selectedId)) {
          enabled = false;
        } else {
          enabled = true;
        }
        btnAssignId.setDisable(!enabled);

        @SuppressWarnings("unchecked")
        Map<String, Object> attributeMap = (Map<String, Object>) results.get(selectedId);
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
        tblResults.setItems(null);
        btnAssignId.setDisable(true);
      }
    });
  }

  private void setTrackData() {
    setCurrentTrackData();
    setAlbumData();
  }

  private void setAlbumData() {
    MusicFile selectedItem = musicFile;
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
    MusicFile selectedItem = musicFile;
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
    return rawData == null || rawData.isEmpty() ? "(Unknown)" : rawData;
  }

  @FXML
  private void assignId() {
    String qName = cmbQueryResultName.getSelectionModel().getSelectedItem();
    QueryUtility qUtility = QueryUtility.getInstance();
    String qCode = qUtility.getQueryCodeByName(qName);
    String selectedId = listQueryResults.getSelectionModel().getSelectedItem();
    musicFile.assignId(qCode, selectedId);
    this.assignedIds = musicFile.getAssignedIds();
    refreshUIAfterQuery(selectedId);
  }

  @FXML
  private void backToMainView() {
    root.getScene().setRoot(callerWindowRoot);
  }

  private final class CustomCell extends ListCell<String> {

    @Override
    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);

      if (empty || item == null) {
        setText(null);
      } else {
        ObservableList<String> styleClass = getStyleClass();
        if (assignedIds != null && assignedIds.containsValue(item)) {
          styleClass.add("bold");
        } else {
          styleClass.remove("bold");
        }
        setText(item);
      }
    }

  }

  private class QueryService extends ScanService {

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
    protected void succeeded() {
      results = musicFile.getLatestQueryResult();
      super.succeeded();
    }

    @Override
    protected void initUI() {
      Platform.runLater(() -> {
        lblQName.setText(query.getName() + " query");
        lblQueryStatus.setText("");
        progressIndicator.setVisible(true);
      });
    }

    public void setQuery(Query query) {
      this.query = query;
    }

    @Override
    protected void showSuccess(String message) {
      Platform.runLater(() -> {
        progressIndicator.setVisible(false);
        lblQueryStatus.getStyleClass().setAll("success");
        lblQueryStatus.setText(message);
        initCmbQueryResultName();
      });

    }

    @Override
    protected void showError(String message) {
      Platform.runLater(() -> {
        lblQueryStatus.getStyleClass().setAll("error");
        lblQueryStatus.setText(message);
        progressIndicator.setVisible(false);
      });
    }

    @Override
    protected void hideIndicators() {
      progressIndicator.setVisible(false);
    }

    @Override
    protected String getSuccessMessage() {
      return "Finished!";
    }

    @Override
    protected String getErrorMessage() {
      return "Cannot connect to web service!";
    }
  }

}
