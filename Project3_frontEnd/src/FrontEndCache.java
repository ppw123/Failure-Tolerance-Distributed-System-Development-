import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

public class FrontEndCache {
  private static Map<String, String> cache = new HashMap<String, String>();
  private static Logger logger = LogManager.getLogger(HttpServer.class);
  private static final ReadWriteLock rwLock = new ReadWriteLock();
  
  // s is the response string which come from back end.
  public static void saveToCache(String s) {
    rwLock.writeLock();
    logger.info("Save {} to cache.", s);
    JSONParser jp = new JSONParser();
    try {
      JSONObject jb = (JSONObject) jp.parse(s);
      cache.put(jb.get("q").toString(), s);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    rwLock.writeUnlock();
  }

  public static String searchInCache(String key) {
    rwLock.readLock();
    if (cache.containsKey(key)) {
      rwLock.readUnlock();
      return cache.get(key);
    }
    rwLock.readUnlock();
    return null;
  }

  public static Integer getVersionNum(String key) {
    rwLock.readLock();
    logger.info("Get version num for key {}.", key);
    String value = searchInCache(key);
    if (value == null) {
      rwLock.readUnlock();
      return 0;
    } else {
      JSONParser jp = new JSONParser();
      try {
        JSONObject jb = (JSONObject) jp.parse(value);
        int versionNum = Integer.parseInt(jb.get("v").toString());
        logger.info("Version num for key {} is {}.", key, versionNum);
        rwLock.readUnlock();
        return versionNum;
      } catch (ParseException e) {
        e.printStackTrace();
        rwLock.readUnlock();
        return 0;
      }
    }
  }
}
