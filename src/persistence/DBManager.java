package persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import model.MusicFile;

public class DBManager {

  private static Logger log = Logger.getLogger(DBManager.class);

  private static DBManager instance = null;

  private boolean isTest = false;
  
  private Database musicLibraryDB;

  protected DBManager() {
    init();
  }

  public DBManager(boolean isTest) {
    this.isTest = isTest;
    init();
  }

  public static DBManager getInstance() {
    instance = Optional.ofNullable(instance).orElse(new DBManager());
    return instance;
  }

  public static DBManager getInstance(boolean isTest) {
    instance = Optional.ofNullable(instance).orElse(new DBManager(isTest));
    return instance;
  }

  private void init() {
    JavaContext ctx = new JavaContext();
    Manager manager;
    try {
      manager = new Manager(ctx, Manager.DEFAULT_OPTIONS);
      musicLibraryDB = manager.getDatabase(isTest ? "test_db" : "musiclibrary_db");
    } catch (IOException | CouchbaseLiteException e) {
      log.fatal("Error while initializing DBManager!", e);
    }
  }

  /**
   * Save a single musicfile to database. All query results and assigned ids will be persisted.
   * 
   * @param mf
   * @throws CouchbaseLiteException
   */
  public void saveMusicFile(MusicFile mf) throws CouchbaseLiteException {
    try {
      Document document = musicLibraryDB.getDocument(mf.getPath());
      Map<String, Object> allQueryResults = mf.getAllQueryResults();
      allQueryResults.put("latest_query", mf.getLastQueryCode());
      allQueryResults.put("assigned_ids", mf.getAssignedIds());
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

  /**
   * Save multiple musicFiles to database.
   * 
   * @param musicFiles
   * @throws CouchbaseLiteException
   */
  public void saveMusicFiles(List<MusicFile> musicFiles) throws CouchbaseLiteException {
    List<MusicFile> dirtyFiles = musicFiles.stream().filter(mf -> mf.getDirty()).collect(Collectors.toList());
    for (MusicFile mf : dirtyFiles) {
      saveMusicFile(mf);
    }
  }

  /**
   * Load data for musicFiles if it exists in database.
   * 
   * @param musicFiles
   */
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

}
