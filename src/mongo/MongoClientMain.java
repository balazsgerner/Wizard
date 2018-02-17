package mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import model.MusicFile;

public class MongoClientMain {

  private static final String LIBRARY_PATH = "C:\\Users\\Gerner\\Music";

  private List<String> supportedExtensions = Arrays.asList("mp3", "m4a", "flac");

  private List<MusicFile> model = new ArrayList<MusicFile>();

  // mongodemo
  public static void main(String[] args) {

    MongoClientMain instance = new MongoClientMain();

    CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    try (MongoClient mongoClient = new MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build())) {

      MongoDatabase db = mongoClient.getDatabase("musiclibrary_db");
      MongoCollection<MusicFile> collection = db.getCollection("my_music_collection", MusicFile.class);

      instance.scanLibrary(new File(LIBRARY_PATH));
      collection.insertMany(instance.getModel());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private List<MusicFile> getModel() {
    return model;
  }

  public void scanLibrary(File library) throws IOException {
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

}
