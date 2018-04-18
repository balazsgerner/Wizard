package application;

import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    try {

      Parent root = FXMLLoader.load(getClass().getResource("/view/main.fxml"));
      Scene scene = new Scene(root, 800, 600);
      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

      Properties prop = new Properties();
      prop.load(getClass().getResourceAsStream("/resources/properties/application.properties"));
      
      primaryStage.setTitle(prop.getProperty("application.title"));
      primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/icon.png")));
      primaryStage.setScene(scene);
      primaryStage.setMaximized(true);
      primaryStage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

}
