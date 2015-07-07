import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.BackEndServerInfo;
import model.ServerAddressAndPort;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//Manage all data stored in discovery.
public class DataManager {
	private static Logger logger = LogManager.getLogger(DataManager.class);
	private static List<ServerAddressAndPort> frontEndAp = new LinkedList<ServerAddressAndPort>();
	private static List<BackEndServerInfo> secondaryAp = new LinkedList<BackEndServerInfo>();
	private static ServerAddressAndPort primaryAp = null;
	private static ServerAddressAndPort loadBalance = null;
	//Map<host:port, id>
	private static Map<String, Integer> idAllocator = new HashMap<String, Integer>();
	private static final ReadWriteLock rwLock = new ReadWriteLock();
	
	//Assign id to each server.
		public static Integer assignId(String host, int port) {
			String key = host + ":" + port;
			logger.debug("Get id for key: " + key);
			rwLock.writeUnlock(); 
			if (!idAllocator.containsKey(key)) {
				idAllocator.put(key, idAllocator.size());
			}
			Integer id = idAllocator.get(key);
			rwLock.writeUnlock();
			logger.debug("Id is: " + id);
			return id;
		}
		
	public static void addFrontEndAp(ServerAddressAndPort fAp) {
		rwLock.writeLock();
		frontEndAp.add(fAp);
		logger.info("Current Front ends: " + toStringF(frontEndAp));
		rwLock.writeUnlock();
	}
	public static void addBackEndAp(BackEndServerInfo bAp) {
		rwLock.writeLock();
		secondaryAp.add(bAp);
		logger.info("Current secondary back ends: " + toString(secondaryAp));
		rwLock.writeUnlock();
	}
	public static void removeBackEndAp(ServerAddressAndPort bAp) {
		rwLock.writeLock();
		secondaryAp.remove(bAp);
		logger.info("Current secondary back ends: " + toString(secondaryAp));
		rwLock.writeUnlock();
	}
	private static String toString(List<BackEndServerInfo> list) {
		String r = "";
		for (BackEndServerInfo sp : list) {
			r += sp + "\n";
		}
		return r;
	}
	
	private static String toStringF(List<ServerAddressAndPort> list) {
    String r = "";
    for (ServerAddressAndPort sp : list) {
      r += sp + "\n";
    }
    return r;
  }

	public static void removeFrontEndAp(ServerAddressAndPort fAp) {
		rwLock.writeLock();
		frontEndAp.remove(fAp);
		logger.info("Current Front ends: " + toStringF(frontEndAp));
		rwLock.writeUnlock();
	}
	
	public static List<ServerAddressAndPort> getFrontEndAp() {
		rwLock.readLock();
		List<ServerAddressAndPort> result = frontEndAp;
		rwLock.readUnlock();
		return result;
	}
	public static void setFrontEndAp(List<ServerAddressAndPort> frontEndAp) {
		rwLock.writeLock();
		DataManager.frontEndAp = frontEndAp;
		rwLock.writeUnlock();
	}
	
	public static List<BackEndServerInfo> getSecondaryAp() {
		rwLock.readLock();
		List<BackEndServerInfo> result = secondaryAp;
		rwLock.readUnlock();
		return result;
	}
	public static void setSecondaryAp(List<BackEndServerInfo> secondaryAp) {
		rwLock.writeLock();
		DataManager.secondaryAp = secondaryAp;
		rwLock.writeUnlock();
	}
	public static ServerAddressAndPort getPrimaryAp() {
		rwLock.readLock();
		ServerAddressAndPort result = primaryAp;
		rwLock.readUnlock();
		return result;
	}
	public static void setPrimaryAp(ServerAddressAndPort primaryAp) {
		rwLock.writeLock();
		DataManager.primaryAp = primaryAp;
		rwLock.writeUnlock();
	}
	public static ServerAddressAndPort getLoadBalance() {
		rwLock.readLock();
		ServerAddressAndPort result = loadBalance;
		rwLock.readUnlock();
		return result;
	}
	public static void setLoadBalance(ServerAddressAndPort loadBalance) {
		rwLock.writeLock();
		DataManager.loadBalance = loadBalance;
		rwLock.writeUnlock();
	}
	
	
	
}
