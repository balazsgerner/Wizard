package application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import application.query.Query;
import application.query.QueryUtility;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import model.MusicFile;

public class MainWindowController implements Initializable {

  @FXML
  private Parent root;

  @FXML
  private MenuItem openMenuItem;

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
  private Button btnOpenLibrary;

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
      txtFilter.setText("");
      txtFilter.setDisable(true);

      Service<Void> openLibraryService = new Service<Void>() {

        @Override
        protected Task<Void> createTask() {
          return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
              scanLibrary(directory);
              return null;
            }

            @Override
            protected void succeeded() {
              scanProgressIndicator.setVisible(false);
              txtFilter.setDisable(false);
              trackNumber.setText(model.size() + " tracks found");
            }
          };
        }
      };

      openLibraryService.start();
      scanProgressIndicator.progressProperty().bind(openLibraryService.progressProperty());
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
  private void clearFilterText() {
    txtFilter.setText("");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    openMenuItem.setAccelerator(new KeyCharacterCombination("O", KeyCombination.CONTROL_DOWN));

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

    initBtnQueryAll();
    filteredModel = new FilteredList<>(model);
    musicDetails.setItems(filteredModel);

    musicDetails.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    extensionColumn.setVisible(false);
    pathColumn.setVisible(false);
    musicDetails.getColumns().addAll(extensionColumn, pathColumn, bandColumn, titleColumn, albumColumn, yearColumn, genreColumn);
    musicDetails.getColumns().forEach(c -> c.setSortable(true));

    btnTrackDetails.setOnAction(e -> loadTrackDetailView(musicDetails.getSelectionModel().getSelectedItem()));
    musicDetails.setRowFactory(createRowFactory());

    musicDetails.getSelectionModel().selectedItemProperty().addListener(e -> enableTrackDetailsButton());
    txtFilter.textProperty().addListener(e -> filterTableModel());
    initProgressIndicators();
    initBtnOpenLibrary();
  }

  private void initBtnOpenLibrary() {
    InputStream url = getClass().getResourceAsStream("/resources/images/folder.png");
    btnOpenLibrary.setGraphic(new ImageView(new Image(url, 20, 20, true, true)));
    btnOpenLibrary.setOnAction(e -> openLibrary());
  }

  private void initProgressIndicators() {
    pnlScanIndicator.managedProperty().bind(pnlScanIndicator.visibleProperty());
    scanProgressIndicator.managedProperty().bind(scanProgressIndicator.visibleProperty());
    pnlProgressIndicator.managedProperty().bind(pnlProgressIndicator.visibleProperty());
    queryProgressBar.managedProperty().bind(queryProgressBar.visibleProperty());
  }

  private void initBtnQueryAll() {
    ListChangeListener<MusicFile> modelEmptyListener = new ListChangeListener<MusicFile>() {

      @Override
      public void onChanged(Change<? extends MusicFile> c) {
        boolean modelEmpty = model.isEmpty();
        boolean disabled = btnQueryAll.isDisabled();
        if (modelEmpty && !disabled) {
          btnQueryAll.setDisable(true);
        } else if (!modelEmpty && disabled) {
          btnQueryAll.setDisable(false);
        }
      }
    };
    model.addListener(modelEmptyListener);
    QueryUtility queryUtility = QueryUtility.getInstance();
    queryUtility.getQueryMethods().stream().filter(q -> q.isPerformManyQuery()).forEach(q -> {
      MenuItem menuItem = new MenuItem(q.getName());
      btnQueryAll.getItems().add(menuItem);
      menuItem.setOnAction(e -> {
        pnlScanIndicator.setVisible(false);
        pnlProgressIndicator.setVisible(true);
        queryProgressBar.setVisible(true);
        Service<Void> queryService = new Service<>() {

          @Override
          protected Task<Void> createTask() {
            Task<Void> queryTask = new QueryTask(q);
            queryProgressBar.progressProperty().bind(queryTask.progressProperty());
            return queryTask;
          }
        };
        lblQueryName.setText(q.getName() + " query");
        queryService.start();
      });

    });

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

  private void enableTrackDetailsButton() {
    boolean empty = musicDetails.getSelectionModel().isEmpty();
    if (empty) {
      btnTrackDetails.setDisable(true);
    } else if (btnTrackDetails.isDisabled()) {
      btnTrackDetails.setDisable(false);
    }
  }

  private void filterTableModel() {
    filteredModel.setPredicate(p -> {
      String searchStr = txtFilter.getText().toLowerCase();
      if (StringUtils.isEmpty(searchStr)) {
        btnClearFilter.setDisable(true);
        return true;
      }
      btnClearFilter.setDisable(false);

      String band = p.getBand().toLowerCase();
      String album = p.getAlbum().toLowerCase();
      String title = p.getTitle().toLowerCase();
      String genre = p.getGenre().toLowerCase();
      String year = p.getYear().toLowerCase();
      if (band.contains(searchStr) || album.contains(searchStr) || title.contains(searchStr) || genre.contains(searchStr)
          || year.contains(searchStr)) {
        return true;
      }

      return false;
    });
  }

  private void loadTrackDetailView(MusicFile selectedFile) {
    try {
      FXMLLoader trackDetailsLoader = new FXMLLoader(getClass().getResource("/view/track_details.fxml"));
      TrackDetailsParamBean paramBean = new TrackDetailsParamBean();
      paramBean.musicFile = selectedFile;
      trackDetailsLoader.setController(new TrackDetailsController(root, paramBean));
      Parent trackDetailsRoot = trackDetailsLoader.load();
      root.getScene().setRoot(trackDetailsRoot);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  public class QueryTask extends Task<Void> {

    private Query query;

    public QueryTask(Query q) {
      this.query = q;
    }

    @Override
    protected Void call() throws Exception {
      QueryUtility.getInstance().performManyQueries(model, query, this);
      return null;
    }

    @Override
    public void updateProgress(long workDone, long max) {
      super.updateProgress(workDone, max);
      Platform.runLater(() -> {
        int progress = (int) (queryProgressBar.getProgress() * 100);
        lblQueryStatus.setText(String.format("%3s%s", progress, "%"));
      });
    }

    @Override
    protected void succeeded() {
      super.succeeded();
      lblQueryStatus.setText("100% finished");
      queryProgressBar.setVisible(false);
    }

  }

}
