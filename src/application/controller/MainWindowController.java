package application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import application.query.Query;
import application.query.QueryUtility;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import model.MusicFile;
import persistence.DBManager;

public class MainWindowController implements Initializable {

  private static Logger log = Logger.getLogger(MainWindowController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<MusicFile> musicDetails;

  @FXML
  private Label trackNumber;

  @FXML
  private Button btnClearFilter;

  @FXML
  private TextField txtFilter;

  @FXML
  private Button btnTrackDetails;

  @FXML
  private Button btnSaveResults;

  @FXML
  private Button btnScanFolder;

  @FXML
  private Button btnLoadResults;

  @FXML
  private Button btnCancelQuery;

  @FXML
  private MenuButton btnQueryAll;

  @FXML
  private ProgressIndicator scanProgressIndicator;

  @FXML
  private ProgressBar queryProgressBar;

  @FXML
  private Label lblQueryStatus;

  @FXML
  private Label lblQueryName;

  @FXML
  private HBox pnlScanIndicator;

  @FXML
  private HBox pnlProgressIndicator;

  private List<String> supportedExtensions = Arrays.asList("mp3", "m4a", "flac");

  private ObservableList<MusicFile> model = FXCollections.observableArrayList();

  private FilteredList<MusicFile> filteredModel;

  private OpenLibraryService openLibraryService;

  private QueryService queryService;

  private DBService saveService;

  private DBService loadService;

  @FXML
  private void openLibrary() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Locate music library folder");
    File directory = chooser.showDialog(root.getScene().getWindow());
    if (directory != null) {
      model.clear();
      trackNumber.setText("");
      pnlProgressIndicator.setVisible(false);
      pnlScanIndicator.setVisible(true);
      scanProgressIndicator.setVisible(true);
      clearFilterText();
      txtFilter.setDisable(true);

      openLibraryService = getOpenLibraryService(directory);
      openLibraryService.restart();
    }
  }

  private OpenLibraryService getOpenLibraryService(File directory) {
    if (openLibraryService == null) {
      return new OpenLibraryService(directory);
    } else {
      openLibraryService.setDirectory(directory);
      return openLibraryService;
    }
  }

  private void scanLibrary(File library) {
    Arrays.asList(library.listFiles()).forEach(file -> {
      if (file.isDirectory()) {
        scanLibrary(file);
      } else {
        String ext = FilenameUtils.getExtension(file.getName());
        if (supportedExtensions.contains(ext)) {
          model.add(new MusicFile(file));
        }
      }
    });

  }

  @FXML
  private void saveAllResultsToDatabase() {
    getSaveService().restart();
  }

  @FXML
  private void loadResults() {
    getLoadService().restart();
  }

  private DBService getSaveService() {
    if (saveService == null) {
      return new SaveService();
    } else {
      return saveService;
    }
  }

  private DBService getLoadService() {
    if (loadService == null) {
      return new LoadService();
    } else {
      return loadService;
    }
  }

  @FXML
  private void clearFilterText() {
    txtFilter.setText("");
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    initTxtFilter();
    initTableView();
    initProgressIndicators();
    initBtnOpenLibrary();
    initBtnSaveResults();
    initBtnLoadResults();
    initBtnQueryAll();
    initBtnTrackDetails();
    initBtnClearFilter();
  }

  private void initBtnClearFilter() {
    btnClearFilter.disableProperty().bind(Bindings.isEmpty(txtFilter.textProperty()));
  }

  private void initBtnLoadResults() {
    btnLoadResults.setGraphic(ImageLoader.getInstance().loadImage("database.load", 24));
    btnLoadResults.disableProperty().bind(Bindings.size(model).isEqualTo(0));
  }

  private void initTxtFilter() {
    txtFilter.textProperty().addListener(e -> filterTableModel());
  }

  @SuppressWarnings("unchecked")
  private void initTableView() {
    TableColumn<MusicFile, String> extensionColumn = new TableColumn<>("Extension");
    extensionColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
    TableColumn<MusicFile, String> pathColumn = new TableColumn<>("Path");
    pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
    TableColumn<MusicFile, String> bandColumn = new TableColumn<>("Band");
    bandColumn.setCellValueFactory(new PropertyValueFactory<>("band"));
    TableColumn<MusicFile, String> titleColumn = new TableColumn<>("Title");
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    TableColumn<MusicFile, String> albumColumn = new TableColumn<>("Album");
    albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
    TableColumn<MusicFile, String> genreColumn = new TableColumn<>("Genre");
    genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
    TableColumn<MusicFile, String> yearColumn = new TableColumn<>("Year");
    yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
    yearColumn.setStyle("-fx-alignment: CENTER");
    yearColumn.setMinWidth(45);
    yearColumn.setPrefWidth(45);
    yearColumn.setMaxWidth(65);
    TableColumn<MusicFile, Boolean> dirtyColumn = new TableColumn<>("Dirty");
    dirtyColumn.setCellValueFactory(new PropertyValueFactory<>("dirty"));
    dirtyColumn.setStyle("-fx-alignment: CENTER");
    dirtyColumn.setCellFactory(param -> new TableCellFactory());
    dirtyColumn.setPrefWidth(45);
    dirtyColumn.setResizable(false);
    filteredModel = new FilteredList<>(model);
    musicDetails.setItems(filteredModel);
    musicDetails.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    extensionColumn.setVisible(false);
    pathColumn.setVisible(false);
    musicDetails.getColumns().addAll(dirtyColumn, extensionColumn, pathColumn, bandColumn, titleColumn, albumColumn, yearColumn, genreColumn);
    musicDetails.setRowFactory(createRowFactory());
  }

  private void initBtnTrackDetails() {
    btnTrackDetails.setGraphic(ImageLoader.getInstance().loadImage("details", 24));
    btnTrackDetails.setOnAction(e -> loadTrackDetailView(musicDetails.getSelectionModel().getSelectedItem()));
    btnTrackDetails.disableProperty().bind(Bindings.isNull(musicDetails.getSelectionModel().selectedItemProperty()));
  }

  private void initBtnSaveResults() {
    btnSaveResults.setGraphic(ImageLoader.getInstance().loadImage("database", 24));
    btnSaveResults.disableProperty().bind(Bindings.size(model).isEqualTo(0));
  }

  private void initBtnOpenLibrary() {
    btnScanFolder.setGraphic(ImageLoader.getInstance().loadImage("folder", 24));
    btnScanFolder.setOnAction(e -> openLibrary());
  }

  private void initProgressIndicators() {
    trackNumber.managedProperty().bind(trackNumber.visibleProperty());
    pnlScanIndicator.managedProperty().bind(pnlScanIndicator.visibleProperty());
    scanProgressIndicator.managedProperty().bind(scanProgressIndicator.visibleProperty());
    pnlProgressIndicator.managedProperty().bind(pnlProgressIndicator.visibleProperty());
    queryProgressBar.managedProperty().bind(queryProgressBar.visibleProperty());
    btnCancelQuery.managedProperty().bind(btnCancelQuery.visibleProperty());
    btnCancelQuery.setGraphic(ImageLoader.getInstance().loadImage("cancel"));
  }

  @FXML
  private void cancelQuery() {
    queryService.cancel();
  }

  private void initBtnQueryAll() {
    btnQueryAll.setGraphic(ImageLoader.getInstance().loadImage("query", 24));
    QueryUtility queryUtility = QueryUtility.getInstance();
    queryUtility.getQueryMethods().stream().filter(q -> q.isPerformManyQuery()).forEach(query -> {
      MenuItem menuItem = new MenuItem(query.getName());
      btnQueryAll.getItems().add(menuItem);
      menuItem.setOnAction(e -> {
        queryService = getQueryService(query);
        queryService.restart();
      });
    });
    btnQueryAll.disableProperty().bind(Bindings.size(model).isEqualTo(0));
  }

  private QueryService getQueryService(Query query) {
    if (queryService == null) {
      return new QueryService(query);
    } else {
      queryService.setQuery(query);
      return queryService;
    }
  }

  private Callback<TableView<MusicFile>, TableRow<MusicFile>> createRowFactory() {
    return tv -> {
      TableRow<MusicFile> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          MusicFile selectedFile = row.getItem();
          loadTrackDetailView(selectedFile);
        }
      });

      return row;
    };
  }

  private void filterTableModel() {
    filteredModel.setPredicate(mf -> {
      String searchStr = txtFilter.getText();
      if (StringUtils.isEmpty(searchStr)) {
        return true;
      }

      String band = mf.getBand();
      String album = mf.getAlbum();
      String title = mf.getTitle();
      String genre = mf.getGenre();
      String year = mf.getYear();
      List<String> searchFields = Arrays.asList(band, album, title, genre, year);
      return searchFields.stream().anyMatch(f -> StringUtils.containsIgnoreCase(f, searchStr));
    });
  }

  private void loadTrackDetailView(MusicFile selectedFile) {
    try {
      FXMLLoader trackDetailsLoader = new FXMLLoader(getClass().getResource("/view/track_details.fxml"));
      trackDetailsLoader.setController(new TrackDetailsController(root, selectedFile));
      Parent trackDetailsRoot = trackDetailsLoader.load();
      pnlProgressIndicator.setVisible(false);
      root.getScene().setRoot(trackDetailsRoot);
    } catch (IOException e) {
      log.error("Error opening track details view!", e);
    }
  }

  private final class TableCellFactory extends TableCell<MusicFile, Boolean> {

    private CheckBox cb;

    private TableCellFactory() {
      cb = new CheckBox();
      cb.selectedProperty().bind(itemProperty());
      this.setGraphic(cb);
      this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      this.setFocusTraversable(false);
      this.setEditable(false);
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
      super.updateItem(item, empty);

      if (empty) {
        setGraphic(null);
      } else {
        setGraphic(cb);
      }
    }
  }

  private class SaveService extends DBService {

    @Override
    protected Task<Void> createTask() {
      return new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          DBManager.getInstance().saveMusicFiles(model);
          return null;
        }
      };
    }

    @Override
    protected String getSuccessMessage() {
      return "Save operation completed!";
    }

    @Override
    protected String getErrorMessage() {
      return "Error while saving music files to database!";
    }

    @Override
    protected String getTaskStartedMsg() {
      return "Saving";
    }

  }

  public final class QueryService extends ScanService {

    private Query query;

    private QueryService(Query query) {
      this.query = query;
    }

    @Override
    protected Task<Void> createTask() {
      QueryTask queryTask = new QueryTask();
      return queryTask;
    }

    public Query getQuery() {
      return query;
    }

    public void setQuery(Query query) {
      this.query = query;
    }

    @Override
    protected void initUI() {
      queryProgressBar.progressProperty().bind(this.progressProperty());
      pnlScanIndicator.setVisible(false);
      pnlProgressIndicator.setVisible(true);
      queryProgressBar.setVisible(true);
      btnCancelQuery.setVisible(true);
      lblQueryName.setVisible(true);
      lblQueryStatus.setVisible(true);
      lblQueryName.setText(query.getName() + " query");
      lblQueryStatus.setText("");
      lblQueryStatus.getStyleClass().setAll("default");
    }

    public class QueryTask extends Task<Void> {

      @Override
      protected Void call() throws Exception {
        QueryUtility.getInstance().performManyQueries(model, getQuery(), this);
        if (isCancelled()) {
          cancelled();
        }
        return null;
      }

      @Override
      protected void cancelled() {
        Platform.runLater(() -> {
          lblQueryStatus.getStyleClass().setAll("info");
          lblQueryStatus.setText("Cancelled!");
          hideIndicators();
        });

      }

      @Override
      public void updateProgress(long workDone, long max) {
        super.updateProgress(workDone, max);
        Platform.runLater(() -> {
          int progress = (int) (queryProgressBar.getProgress() * 100);
          lblQueryStatus.setText(progress + "%");
        });
      }

    }

    @Override
    protected void showSuccess(String message) {
      showSuccessMsg(message);
    }

    @Override
    protected void showError(String message) {
      showErrorMsg(message);
    }

    @Override
    protected void hideIndicators() {
      queryProgressBar.setVisible(false);
      btnCancelQuery.setVisible(false);
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

  private void showSuccessMsg(String message) {
    Platform.runLater(() -> {
      lblQueryStatus.getStyleClass().setAll("success");
      lblQueryStatus.setText(message);
    });
  }

  private void showErrorMsg(String message) {
    Platform.runLater(() -> {
      lblQueryStatus.getStyleClass().setAll("error");
      lblQueryStatus.setText(message);
    });
  }

  private class OpenLibraryService extends ScanService {

    private File directory;

    private OpenLibraryService(File directory) {
      this.directory = directory;
    }

    public void setDirectory(File directory) {
      this.directory = directory;
    }

    @Override
    protected Task<Void> createTask() {
      return new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          scanLibrary(directory);
          return null;
        }

      };
    }

    @Override
    protected void initUI() {
      Platform.runLater(() -> {
        trackNumber.getStyleClass().setAll("bold");
        scanProgressIndicator.progressProperty().bind(progressProperty());
      });
    }

    @Override
    protected void showSuccess(String message) {
      Platform.runLater(() -> {
        trackNumber.setText(message);
        filterTableModel();
      });
    }

    @Override
    protected void showError(String message) {
      trackNumber.setText(message);
    }

    @Override
    protected void hideIndicators() {
      scanProgressIndicator.setVisible(false);
      txtFilter.setDisable(false);
    }

    @Override
    protected String getSuccessMessage() {
      return model.size() + " tracks found";
    }

    @Override
    protected String getErrorMessage() {
      return "Error while scanning library!";
    }
  }

  private abstract class DBService extends ScanService {

    protected abstract Task<Void> createTask();

    protected abstract String getTaskStartedMsg();

    protected abstract String getSuccessMessage();

    protected abstract String getErrorMessage();

    @Override
    protected void initUI() {
      Platform.runLater(() -> {
        scanProgressIndicator.progressProperty().bind(progressProperty());

        pnlProgressIndicator.setVisible(true);
        lblQueryName.setVisible(false);
        queryProgressBar.setVisible(false);
        btnCancelQuery.setVisible(false);
        lblQueryStatus.getStyleClass().setAll("bold");
        lblQueryStatus.setText(getTaskStartedMsg());
        lblQueryStatus.setVisible(true);

        pnlScanIndicator.setVisible(true);
        trackNumber.setVisible(false);
        scanProgressIndicator.setVisible(true);
        lblQueryStatus.setPadding(new Insets(0, 5, 0, 0));
      });

    }

    @Override
    protected void showSuccess(String message) {
      showSuccessMsg(message);
    }

    @Override
    protected void showError(String message) {
      showErrorMsg(message);
    }

    @Override
    protected void hideIndicators() {
      lblQueryStatus.setPadding(new Insets(0));
      scanProgressIndicator.setVisible(false);
    }

  }

  private class LoadService extends DBService {

    @Override
    protected Task<Void> createTask() {
      return new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          DBManager.getInstance().loadMusicFiles(model);
          return null;
        }
      };
    }

    @Override
    protected String getSuccessMessage() {
      return "Load operation completed!";
    }

    @Override
    protected String getErrorMessage() {
      return "Error while loading music files to database!";
    }

    @Override
    protected String getTaskStartedMsg() {
      return "Loading...";
    }

  }

}
