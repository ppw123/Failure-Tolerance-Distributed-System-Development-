import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import model.ServerChanges.ServerType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//This is not the real load service, just do front end random assignment for client.
public class LoadBalance {
	private static Logger logger = LogManager.getLogger(LoadBalance.class);
	private static List<ServerAddressAndPort> frontEndServerList = new LinkedList<ServerAddressAndPort>();
	private static final ReadWriteLock rwLock = new ReadWriteLock();
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		if (args.length > 2) {
			int dPort = Integer.parseInt(args[0]);
			String discoveryHost = args[1];
		    int discoveryPort = Integer.parseInt(args[2]);
			logger.info("Starting loadBalance server with port: {}.", dPort);
			// notice discovery
		      Message m = new Message();
		      m.setMethod(Method.ADD);
		      ServerChanges sc = new ServerChanges();
		      sc.setId(-1);
		      sc.setServerType(ServerType.LB);
		      InetAddress addr = InetAddress.getLocalHost();
		      String name = addr.getHostName().toString();
		      sc.setServerAddressAndPort(new ServerAddressAndPort(name, dPort));
		      m.setData(sc);
		      try {
			      String result = MessageSender.sentMessage(m, discoveryHost, discoveryPort);
			      
			      if (result.equals("S")) {
			    	  ServerSocket serversock = new ServerSocket(dPort);
						logger.info("LoadBalance server is ready...");
						ExecutorService executor = Executors.newFixedThreadPool(80);
						while (true) {
							Socket socket = serversock.accept();
							executor.execute(new Service(socket));
						}
			      }
		      } catch (Exception e) {
		    	  logger.info("Please run discovery first...");
		      }
			
		} else {
		  logger.error("Usage: java LoadBalance <port> <discoveryHost> <discoveryPort>");
		}
	}
	
	public static void addFrontEndServer(ServerAddressAndPort serverAddressAndPort) {
		rwLock.writeLock();
		frontEndServerList.add(serverAddressAndPort);
		rwLock.writeUnlock();
	}
	
	public static void removeFrontEndServer(ServerAddressAndPort sp) {
		rwLock.writeLock();
		frontEndServerList.remove(sp);
		rwLock.writeUnlock();
	}
	
    public static List<ServerAddressAndPort> getFrontEndServers() {
    	rwLock.readLock();
    	List<ServerAddressAndPort> result = frontEndServerList;
    	rwLock.readUnlock();
		return result;
	}
}
