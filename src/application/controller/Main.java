package application.controller;

import java.util.Properties;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;

import application.query.QueryUtility;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  private static Logger log = Logger.getLogger(Application.class);

  private Properties prop;

  @Override
  public void start(Stage primaryStage) {
    try {
      prop = new Properties();
      prop.load(getClass().getResourceAsStream("/resources/properties/application.properties"));

      disableWarning();
      initQueryMethods();

      Parent root = FXMLLoader.load(getClass().getResource(prop.getProperty("mainwindow.view.url")));
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource(prop.getProperty("styles.url")).toExternalForm());

      primaryStage.setTitle(prop.getProperty("application.title"));
      primaryStage.getIcons().add(ImageLoader.getInstance().loadImage("icon"));
      primaryStage.setScene(scene);
      primaryStage.setMaximized(true);
      primaryStage.show();
    } catch (Exception e) {
      log.fatal("Fatal error while starting application!", e);
    }
  }

  private void initQueryMethods() {
    try {
      QueryUtility instance = QueryUtility.getInstance();
      JAXBContext jaxbContext;
      jaxbContext = JAXBContext.newInstance(QueryUtility.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      QueryUtility qm = (QueryUtility) jaxbUnmarshaller.unmarshal(getClass().getResource(prop.getProperty("querymethods.url")));
      instance.setQueryMethods(qm.getQueryMethods());
    } catch (JAXBException e) {
      log.fatal("Fatal error while loading querymethods.xml!", e);
    }

  }

  private void disableWarning() {
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
    AudioFile.logger.setLevel(Level.OFF);
  }

  public static void main(String[] args) {
    launch(args);
  }

}
