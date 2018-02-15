package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import model.MusicFile;

public class MainWindowController implements Initializable {

  @FXML
  private BorderPane root;

  @FXML
  private MenuItem openMenuItem;

  @FXML
  private TableView<MusicFile> musicDetails;

  @FXML
  private Label trackNumber;

  @FXML
  private ProgressIndicator scanProgressIndicator;

  private List<String> supportedExtensions = Arrays.asList("mp3", "m4a", "flac");

  private ObservableList<MusicFile> model = FXCollections.observableArrayList();

  @FXML
  private void openLibrary() throws IOException {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Locate music library folder");
    File directory = chooser.showDialog(root.getScene().getWindow());
    if (directory != null) {
      model.clear();
      trackNumber.setText("");
      scanProgressIndicator.setVisible(true);

      Task<Void> scanTask = new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          scanLibrary(directory);
          return null;
        }

        @Override
        protected void succeeded() {
          scanProgressIndicator.setVisible(false);
          trackNumber.setText(model.size() + " tracks found");
        }
      };

      scanProgressIndicator.progressProperty().bind(scanTask.progressProperty());
      Thread thread = new Thread(scanTask);
      thread.setDaemon(true);
      thread.start();
    }
  }

  private void scanLibrary(File library) throws IOException {
    for (File file : library.listFiles()) {
      if (file.isDirectory()) {
        scanLibrary(file);
      } else {
        String ext = FilenameUtils.getExtension(file.getName());
        if (supportedExtensions.contains(ext)) {
          model.add(new MusicFile(file));
        }
      }
    }
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
    musicDetails.setItems(model);

    extensionColumn.setVisible(false);
    pathColumn.setVisible(false);
    musicDetails.getColumns().addAll(extensionColumn, pathColumn, bandColumn, titleColumn, albumColumn);
    musicDetails.getSortOrder().add(bandColumn);
  }

}
