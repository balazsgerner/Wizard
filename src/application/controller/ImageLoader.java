package application.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

  /**
   * Loads an image with original size.
   * 
   * @param imageKey - image identifier
   * @return
   */
  public ImageView loadImage(String imageKey) {
    InputStream url = getClass().getResourceAsStream(prop.getProperty(imageKey));
    return new ImageView(new Image(url));
  }

  /**
   * Loads an image, then resizes it to fit the given height.
   * 
   * @param imageKey - image identifier
   * @param height - height to fit
   * @return
   */
  public ImageView loadImage(String imageKey, int height) {
    ImageView iv = loadImage(imageKey);
    iv.setPreserveRatio(true);
    iv.setFitHeight(height);
    return iv;
  }

}
