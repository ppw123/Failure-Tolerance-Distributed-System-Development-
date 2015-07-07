import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.BackEndServerInfo;
import model.ServerAddressAndPort;


public class BackendMetaData {
	private static boolean isPrimary;
	private static int id;
	private static ServerAddressAndPort primary;
	private static Map<Integer, ServerAddressAndPort> activeSecondaryMap = new HashMap<Integer, ServerAddressAndPort>();
	private static final ReadWriteLock rwLock = new ReadWriteLock();
	private static Logger logger = LogManager.getLogger(HttpServer.class);
	public static Boolean addSecondaryMap(String host, int port, Integer id){
		rwLock.writeLock();
		ServerAddressAndPort s = new ServerAddressAndPort(host, port);
		if(activeSecondaryMap.containsKey(id)){
			rwLock.writeUnlock();
			return false;	
		} else {
			activeSecondaryMap.put(id, s);
			logger.info("Current secondary list: " + toString(activeSecondaryMap));
			rwLock.writeUnlock();
			return true;
		}
	}
	
	private static String toString(Map<Integer, ServerAddressAndPort> a) {
		String s = "";
		for (Integer i : a.keySet()) {
			s += "id: " + i + a.get(i) + "\n";
		}
		return s;
	}
	public static ServerAddressAndPort getPrimary() {
		return primary;
	}

	public static void setPrimary(ServerAddressAndPort primary) {
		rwLock.writeLock();
		BackendMetaData.primary = primary;
		rwLock.writeUnlock();
	}

	public static Boolean removeSecondaryMap(String host, int port){
		rwLock.writeLock();
		ServerAddressAndPort s = new ServerAddressAndPort(host, port);
		if(activeSecondaryMap.containsValue(s)){
			activeSecondaryMap.values().remove(s);
			rwLock.writeUnlock();
			return true;
		} else {
			rwLock.writeUnlock();
			return false;
		}
	}

	public static  boolean isPrimary() {
		return isPrimary;
	}

	public static void setPrimary(boolean isp) {
		rwLock.writeLock();
		isPrimary = isp;
		rwLock.writeUnlock();
	}

	public static int getId() {
		return id;
	}

	public static void setId(int id) {
		rwLock.writeLock();
		BackendMetaData.id = id;
		rwLock.writeUnlock();
	}

	public static Map<Integer, ServerAddressAndPort> getActiveSecondaryMap() {
		rwLock.readLock();
		Map<Integer, ServerAddressAndPort> result = activeSecondaryMap;
		rwLock.readUnlock();
		return result;
	}

	public static void setActiveSecondaryMap(
			Map<Integer, ServerAddressAndPort> activeSecondaryMap) {
		rwLock.writeLock();
		BackendMetaData.activeSecondaryMap = activeSecondaryMap;
		rwLock.writeUnlock();
	}
	public static List<BackEndServerInfo> getSecondaryServerList() {
		List<BackEndServerInfo> result = new LinkedList<BackEndServerInfo>();
		rwLock.readLock();
		for (Integer id : activeSecondaryMap.keySet()) {
			result.add(new BackEndServerInfo(id, activeSecondaryMap.get(id).getHost(), activeSecondaryMap.get(id).getPort()));
		}
		rwLock.readUnlock();
		return result;
	}

}
