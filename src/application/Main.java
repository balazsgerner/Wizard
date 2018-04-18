package application;

import java.util.Properties;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;

import application.query.QueryUtility;
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

      initQueryMethods();

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

  private void initQueryMethods() throws JAXBException {
    QueryUtility instance = QueryUtility.getInstance();
    JAXBContext jaxbContext = JAXBContext.newInstance(QueryUtility.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    QueryUtility qm = (QueryUtility) jaxbUnmarshaller.unmarshal(getClass().getResource("/resources/xml/query_methods.xml"));
    instance.setQueryMethods(qm.getQueryMethods());

  }

  public static void disableWarning() {
    Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    AudioFile.logger.setLevel(Level.OFF);
    System.err.close();
    System.setErr(System.out);
  }

  public static void main(String[] args) {
    disableWarning();
    launch(args);
  }

}
