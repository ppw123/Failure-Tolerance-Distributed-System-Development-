import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.BackEndServerInfo;
import model.EmptyMessageData;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import model.ServerChanges.ServerType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpServer {
	private static Logger logger = LogManager.getLogger(HttpServer.class);
	private static String discoveryHost;
	private static int discoveryPort;
	private static String host;
	private static int port;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		if (args.length > 2) {
			port = Integer.parseInt(args[0]);
			discoveryHost = args[1];
			discoveryPort = Integer.parseInt(args[2]);
			InetAddress addr = InetAddress.getLocalHost();
			host = addr.getHostName().toString();
			logger.info(
					"Starting Backend server with name {}, port {}, discoveryHost {}, discoveryPort{}",
					host, port, discoveryHost, discoveryPort);

			

			// timer hello
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					Message m = new Message();
					m.setMethod(Method.HELLO);
					m.setData(new EmptyMessageData());
					if (BackendMetaData.isPrimary()) {
						for (BackEndServerInfo besi : BackendMetaData
								.getSecondaryServerList()) {
							try {
								String s = MessageSender.sentMessage(m, besi.getSp()
										.getHost(), besi.getSp().getPort());
								if (!s.trim().equals("S")) {
									logger.info("Secondary backend down: " + besi);
									removeSecondary(besi);
								}
							} catch (Exception e) {
								logger.info("Secondary backend down: " + besi);
								removeSecondary(besi);
							}
						}
					} else {
						if (BackendMetaData
									.getPrimary() != null) {
						try {
							String s = MessageSender.sentMessage(m, BackendMetaData
									.getPrimary().getHost(), BackendMetaData
									.getPrimary().getPort());
							if (!s.trim().equals("S")) {
								logger.info("Primary backend down: " + BackendMetaData
										.getPrimary());
								BullyServices bs = new BullyServices();
								bs.bully();
								
							}
						} catch (Exception e) {
							logger.info("Primary backend down: " + BackendMetaData
									.getPrimary());
							BullyServices bs = new BullyServices();
							bs.bully();
						}
						}
					}
				}
				

				private void removeSecondary(BackEndServerInfo besi) {
					Message msgToSend = new Message();
					msgToSend.setMethod(Method.DELET);
					ServerChanges sc = new ServerChanges();
					sc.setId(besi.getId());
					sc.setServerType(ServerType.BE);
					sc.setServerAddressAndPort(new ServerAddressAndPort(
							besi.getSp().getHost(), besi
									.getSp().getPort()));
					msgToSend.setData(sc);
					MessageSender.sentMessage(msgToSend, discoveryHost,
							discoveryPort);
					BackendMetaData.removeSecondaryMap(besi
							.getSp().getHost(), besi.getSp()
							.getPort());
					for (BackEndServerInfo besi2 : BackendMetaData
							.getSecondaryServerList()) {
						MessageSender.sentMessage(msgToSend, besi2.getSp()
								.getHost(), besi2.getSp()
								.getPort());
					}
				}
			}, 3000, 3000);
			ServerSocket serversock = new ServerSocket(port);
			logger.info("Backend server is ready...");
			ExecutorService executor = Executors.newFixedThreadPool(80);
			executor.execute(new Services(null, true));
			while (true) {
				Socket socket = serversock.accept();
				executor.execute(new Services(socket, false));
			}
			

		} else {
			logger.error("Usage: java HttpServer <bePort> <disHost> <disPort>");
		}
	}

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		HttpServer.host = host;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		HttpServer.port = port;
	}

	public static String getDiscoveryHost() {
		return discoveryHost;
	}

	public static void setDiscoveryHost(String discoveryHost) {
		HttpServer.discoveryHost = discoveryHost;
	}

	public static int getDiscoveryPort() {
		return discoveryPort;
	}

	public static void setDiscoveryPort(int discoveryPort) {
		HttpServer.discoveryPort = discoveryPort;
	}
}