package application.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import javafx.scene.image.Image;

public class ImageLoader {

  protected static ImageLoader instance = null;

  protected Properties prop;

  protected static Logger log = Logger.getLogger(ImageLoader.class);

  protected ImageLoader() {
    prop = new Properties();
    try {
      prop.load(getClass().getResourceAsStream("/resources/properties/images.properties"));
    } catch (IOException e) {
      log.fatal(e);
    }
  }

  public static ImageLoader getInstance() {
    if (instance == null) {
      instance = new ImageLoader();
    }
    return instance;
  }

  public Image loadImage(String imageKey) {
    InputStream url = getClass().getResourceAsStream(prop.getProperty(imageKey));
    return new Image(url);
  }

}
