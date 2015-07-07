import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.BackEndServerInfo;
import model.BackendServersList;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import model.ServerChanges.ServerType;
import myHttp.HTTPRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class HttpServer {

  private static Logger logger = LogManager.getLogger(HTTPRequest.class);
  private static ServerAddressAndPort primary;
  private static List<BackEndServerInfo> secondaryList = new LinkedList<BackEndServerInfo>();
  private static final ReadWriteLock rwLock = new ReadWriteLock();
  
  @SuppressWarnings("resource")
  public static void main(String[] args) throws Exception {
    if (args.length > 2) {
      int fePort = Integer.parseInt(args[0]);
      String discoveryHost = args[1];
      int discoveryPort = Integer.parseInt(args[2]);
      InetAddress addr = InetAddress.getLocalHost();
      String name = addr.getHostName().toString();
      logger.info("Starting Frontend server with name {}, port {}, discoveryHost {}, discoveryPort{}", name, fePort, discoveryHost, discoveryPort);
      // notice discovery
      Message m = new Message();
      m.setMethod(Method.ADD);
      ServerChanges sc = new ServerChanges();
      sc.setId(-1);
      sc.setServerType(ServerType.FE);
      sc.setServerAddressAndPort(new ServerAddressAndPort(name, fePort));
      m.setData(sc);
      
      String result = MessageSender.sentMessage(m, discoveryHost, discoveryPort);
      Message backEndListMessage = new Message(result);
      BackendServersList bsl = (BackendServersList) backEndListMessage.getData();
      primary = bsl.getPrimaryAp();
      secondaryList = bsl.getSecondaryAp();
      ExecutorService executor = Executors.newFixedThreadPool(40);
      ServerSocket serversock = new ServerSocket(fePort);
      logger.info("FrontEnd is ready...");
      while (true) {
        Socket socket = serversock.accept();
        executor.execute(new Services(socket));
      }
    } else {
      logger.error("Usage: java HttpServer <fePort> <discoveryHost> <discoveryPort>");
    }

  }
  
  public static void addBackEnd(BackEndServerInfo sp) {
	  rwLock.writeLock();
	  secondaryList.add(sp);
	  logger.info("current secondary Backend: " + toString(secondaryList));
	  rwLock.writeUnlock();
  }
  
  private static String toString (List<BackEndServerInfo> l) {
	  String s = "";
	  for (BackEndServerInfo sp : l) {
		  s += sp + "\n";
	  }
	  return s;
  }
  
  public static void removeBackEnd(ServerAddressAndPort sp) {
	  rwLock.writeLock();
	  secondaryList.remove(sp);
	  logger.info("current secondary Backend: " + toString(secondaryList));
	  rwLock.writeUnlock();
  }
  
  public static void changePrimary(ServerAddressAndPort sp) {
	  rwLock.writeLock();
	  primary = sp;
	  if (secondaryList.contains(sp)) {
		  secondaryList.remove(sp);
	  }
	  logger.info("current secondary Backend: " + toString(secondaryList));
	  logger.info("current primary: " + sp);
	  rwLock.writeUnlock();
  }
  
  public static ServerAddressAndPort getPrimary() {
	  rwLock.readLock();
	  ServerAddressAndPort result = primary;
	  rwLock.readUnlock();
	  return result;
  }
  
  public static List<BackEndServerInfo> getSecondaryList() {
	  rwLock.readLock();
	  List<BackEndServerInfo> result = secondaryList;
	  rwLock.readUnlock();
	  return result;
  }

}