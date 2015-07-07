import java.net.ServerSocket;
import org.apache.logging.log4j.LogManager;  
import org.apache.logging.log4j.Logger; 
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscoveryServer {
  private static Logger logger = LogManager.getLogger(DiscoveryServer.class);
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			int dPort = Integer.parseInt(args[0]);
			logger.info("Starting discovery server with port: {}.", dPort);
			ServerSocket serversock = new ServerSocket(dPort);
			logger.info("Discovery server is ready...");
			ExecutorService executor = Executors.newFixedThreadPool(80);
			
			while (true) {
				Socket socket = serversock.accept();
				executor.execute(new ServerManager(socket));
			}
		} else {
		  logger.error("Usage: java HttpServer <fePort> <beHost> <bePort>");
		}
	}
}