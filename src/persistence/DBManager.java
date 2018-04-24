package persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import model.MusicFile;

public class DBManager {

  private static Logger log = Logger.getLogger(DBManager.class);

  private static DBManager instance = null;

  private Database musicLibraryDB;

  protected DBManager() {
    init();
  }

  public static DBManager getInstance() {
    if (instance == null) {
      instance = new DBManager();
    }
    return instance;
  }

  private void init() {
    JavaContext ctx = new JavaContext();
    Manager manager;
    try {
      manager = new Manager(ctx, Manager.DEFAULT_OPTIONS);
      musicLibraryDB = manager.getDatabase("musiclibrary_db");
    } catch (IOException | CouchbaseLiteException e) {
      log.fatal("Error while initializing DBManager!", e);
    }
  }

  public void saveMusicFile(MusicFile mf) throws CouchbaseLiteException {
    Document document = musicLibraryDB.getDocument(mf.getPath());
    Map<String, Object> allQueryResults = mf.getAllQueryResults();
    allQueryResults.put("latest_query", mf.getLastQueryCode());
    try {
      document.createRevision();
      // database content if exists
      Map<String, Object> properties = document.getProperties();
      
      Map<String, Object> newValues;
      if (properties != null) {
        newValues = new HashMap<>(properties);
      } else {
        newValues = new HashMap<>();
      }
      newValues.putAll(allQueryResults);

      // persist data
      document.putProperties(newValues);
      mf.setDirty(false);
    } catch (CouchbaseLiteException e) {
      log.error("Error while writing musicFile to database!\n" + mf.toString(), e);
      throw e;
    }
  }

  public void saveMusicFiles(List<MusicFile> musicFiles) throws CouchbaseLiteException {
    List<MusicFile> dirtyFiles = musicFiles.stream().filter(mf -> mf.getDirty()).collect(Collectors.toList());
    for (MusicFile mf : dirtyFiles) {
      saveMusicFile(mf);
    }

    // allDocumentsQuery();
  }

  public void loadMusicFiles(List<MusicFile> musicFiles) {
    musicFiles.forEach(mf -> {
      Document document = musicLibraryDB.getExistingDocument(mf.getPath());
      if (document != null) {
        Map<String, Object> results = new HashMap<>(document.getProperties());
        results.remove("_rev");
        results.remove("_id");
        mf.setAllQueryResults(results);
      }
    });

  }

  private void allDocumentsQuery() {
    Query query = musicLibraryDB.createAllDocumentsQuery();
    QueryEnumerator result;
    try {
      result = query.run();

      for (Iterator<QueryRow> it = result; it.hasNext();) {
        QueryRow row = it.next();
        System.out.println(row.getDocument().getProperties());
      }

      System.out.println("Number of music files in database: " + result.getCount());
    } catch (CouchbaseLiteException e) {
      log.error(e);
    }
  }

}
