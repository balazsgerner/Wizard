package persistence.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.File;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import model.MusicFile;
import persistence.LibraryScannerUtilMock;

public class MongoClientMain extends LibraryScannerUtilMock {

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

}
