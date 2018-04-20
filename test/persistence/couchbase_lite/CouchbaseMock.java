package persistence.couchbase_lite;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.MusicFile;
import persistence.LibraryScannerUtilMock;

/**
 * Couchbase persistence demo
 * 
 * @author: Gerner
 */
public class CouchbaseMock extends LibraryScannerUtilMock {

  public static void main(String[] args) {
    try {
      // create context, get database
      CouchbaseMock instance = new CouchbaseMock();
      JavaContext ctx = new JavaContext();
      Manager manager = new Manager(ctx, Manager.DEFAULT_OPTIONS);
      Database musicLibraryDB = manager.getDatabase("musiclibrary_db");

      // scan library, generate data
      instance.scanLibrary(new File(LIBRARY_PATH));
      Document doc = musicLibraryDB.createDocument();
      Map<String, Object> properties = new HashMap<>();
      for (MusicFile mf : instance.getModel()) {
        properties.put(mf.getPath(), mf.toString());
      }
      // persist data
      doc.putProperties(properties);

      // query result
      Query query = musicLibraryDB.createAllDocumentsQuery();
      QueryEnumerator result = query.run();

      // display library content
      for (Iterator<QueryRow> it = result; it.hasNext();) {
        QueryRow row = it.next();
        System.out.println(row.getDocument().getProperties());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
