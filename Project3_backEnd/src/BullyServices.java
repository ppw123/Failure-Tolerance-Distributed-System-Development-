import java.util.LinkedList;
import java.util.List;

import model.BackEndServerInfo;
import model.EmptyMessageData;
import model.IdMessageData;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import model.ServerChanges.ServerType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BullyServices {
	private static Logger logger = LogManager.getLogger(HttpServer.class);
	public void bully() {
		logger.info("Start bully...");
		List<BackEndServerInfo> properServers = new LinkedList<BackEndServerInfo>();
		Integer id = BackendMetaData.getId();
		logger.info("Current server id: " + id);
		Message m = new Message();
		m.setMethod(Method.BULLY);
		m.setData(new EmptyMessageData());
		for (BackEndServerInfo be : BackendMetaData.getSecondaryServerList()) {
			if (be.getId() > id) {
				properServers.add(be);
			}
		}
		for (BackEndServerInfo bei : properServers) {
			try {
				String s = MessageSender.sentMessage(m, bei.getSp().getHost(), bei.getSp().getPort());
				if (s.equals("S")) {
					logger.info("Responsed from " + bei);
					logger.info("End bully for this server.");
					return;
				}
			} catch (Exception e) {
				
			}
		}
		logger.info("Win bully, set current server as primary.");
		setPrimary();
	}
	
	private void setPrimary() {
		Message m = new Message();
		m.setMethod(Method.SET_PRIMARY);
		ServerChanges sc = new ServerChanges();
		sc.setId(BackendMetaData.getId());
		sc.setServerType(ServerType.BE);
		sc.setServerAddressAndPort(new ServerAddressAndPort(HttpServer.getHost(), HttpServer.getPort()));
		m.setData(sc);
		BackendMetaData.setPrimary(true);
		BackendMetaData.setPrimary(new ServerAddressAndPort(HttpServer.getHost(), HttpServer.getPort()));
		BackendMetaData.removeSecondaryMap(HttpServer.getHost(), HttpServer.getPort());
		logger.info("Send set primary to all backend list.");
		for (BackEndServerInfo be : BackendMetaData.getSecondaryServerList()) {
			MessageSender.sentMessage(m, be.getSp().getHost(), be.getSp().getPort());
		}
		logger.info("Send set primary to discovery.");
		MessageSender.sentMessage(m, HttpServer.getDiscoveryHost(), HttpServer.getDiscoveryPort());
		syncVersion();
	}
	
	private void syncVersion() {
		Integer v = Storage.getVersion();
		Message m = new Message();
		m.setMethod(Method.GET_VERSION);
		m.setData(new EmptyMessageData());
		logger.info("Will Sync version of all backends.");
		//Get the MIN version in back-end system.
		for (BackEndServerInfo be : BackendMetaData.getSecondaryServerList()) {
			String ver = MessageSender.sentMessage(m, be.getSp().getHost(), be.getSp().getPort());
			if (Integer.parseInt(ver) < v) {
				v = Integer.parseInt(ver);
			}
		}
		logger.info("Get the min version: (" + v + ")");
		Message m1 = new Message();
		m1.setMethod(Method.SET_VERSION);
		m1.setData(new IdMessageData(v));
		//set min version for itself
		 
		Storage.syncVersion(v);
		//set min version to all secondary
		for (BackEndServerInfo be : BackendMetaData.getSecondaryServerList()) {
			MessageSender.sentMessage(m1, be.getSp().getHost(), be.getSp().getPort());
		}
	}
	
	
}
