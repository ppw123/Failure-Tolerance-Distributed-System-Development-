import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;  
import org.apache.logging.log4j.Logger; 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Storage {

  private static Logger logger = LogManager.getLogger(HttpServer.class);
	private static Map<String, List<Integer>> hashtags = new HashMap<String, List<Integer>>();
	private static List<String> tweets = new LinkedList<String>();
	private static Integer version = 1;
	private static final ReadWriteLock rwLock = new ReadWriteLock();
	private static List<String> lastTags = new LinkedList<String>();
	public static void addTweet(String tweet, List<String> hashtag) {
		rwLock.writeLock();
		logger.info("Lock read and write and start to add a new tweet.");
		Integer currentIndex = 0;
		if (tweets.add(tweet)) {
			currentIndex = tweets.size() - 1;
			logger.info("Add a new tweet with index: {}.", currentIndex);
		}
		for (String ht : hashtag) {
			lastTags = new LinkedList<String>();
			lastTags.add(ht);
			if (hashtags.containsKey(ht)) {
				hashtags.get(ht).add(currentIndex);
			} else {
				List<Integer> tweetsIndexList = new LinkedList<Integer>();
				tweetsIndexList.add(currentIndex);
				hashtags.put(ht, tweetsIndexList);
			}
		}
		version++;
		logger.info("New version: {}." , version);
		logger.info("Unlock read and write and finish adding a new tweet.");
		rwLock.writeUnlock();
	}

	@SuppressWarnings("unchecked")
	public static String readTweet(String key, Integer v) {
		rwLock.readLock();
		if (version.equals(v)) {
			rwLock.readUnlock();
			return null;
		} else {
			JSONArray jTweets = new JSONArray();
			if (hashtags.containsKey(key)) {
				for (Integer index : hashtags.get(key)) {
					jTweets.add(tweets.get(index));
				}
			}
			JSONObject result = new JSONObject();
			result.put("q", key);
			result.put("v", version);
			result.put("tweets", jTweets);
			rwLock.readUnlock();
			return result.toJSONString();
		}
	}
	
	public static List<String> getAllTweets() {
		rwLock.readLock();
		List<String> result = tweets;
		rwLock.readUnlock();
		return result;
	}
	
	public static Integer getVersion() {
		rwLock.readLock();
		Integer result = version;
		rwLock.readUnlock();
		return result;
	}

	public static void syncVersion(Integer v) {
		if (v < version) {
			logger.info("current version is larger than version synced, will roll back for this back end..");
			tweets.remove(tweets.size() - 1);
			for (String tag : lastTags) {
				hashtags.get(tag).remove(hashtags.get(tag).size() - 1);
			}
		} else {
			logger.info("version synced equals current version, donot need any operation.");
		}
		
	}

}
