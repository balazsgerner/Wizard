package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;

import org.bson.Document;

public class MongoClientMain {

  public static void main(String[] args) {

    // mongodemo
    try (MongoClient mongoClient = new MongoClient()) {
      MongoDatabase db = mongoClient.getDatabase("mydb");
      MongoCollection<Document> collection = db.getCollection("test");
      Document document = new Document("name", "GB_DB").append("type", "database").append("count", 1).append("versions",
          Arrays.asList("3.0", "3.1", "3.2"));
      collection.insertOne(document);
    }

  }

}
