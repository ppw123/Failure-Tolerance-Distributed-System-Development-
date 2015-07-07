import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import model.BackEndServerInfo;
import model.BackendServersList;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import model.ServerChanges.ServerType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//ServerManager is used to manage all living servers in the system.

public class ServerManager implements Runnable {
	private Socket socket;
	private static Logger logger = LogManager.getLogger(ServerManager.class);

	// private static int nextId = 0;

	public ServerManager(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {

			BufferedReader incoming = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String request = "";
			String line;

			// Get the whole request contents.
			while (!((line = incoming.readLine()).equals("") || line == null)) {
				request += line + "\r\n";
			}
			request = request.substring(0, request.length() - 2);
			logger.debug("Request: " + request);
			OutputStream out = socket.getOutputStream();
			messageRecieve(request, out);
		} catch (Exception e) {
		}
	}

	public void messageRecieve(String s, OutputStream out) {
		Message m;
		try {
			m = new Message(s);
			if (m.getMethod().equals(Method.ADD)
					|| m.getMethod().equals(Method.DELET)
					|| m.getMethod().equals(Method.SET_PRIMARY)) {

				ServerChanges sc = (ServerChanges) m.getData();
				
				if (sc.getServerType().equals(ServerChanges.ServerType.BE)) {
					sc.setId(DataManager.assignId(sc.getServerAddressAndPort()
							.getHost(), sc.getServerAddressAndPort().getPort()));
					/*When add a new back end, firstly, discovery will notice primary,
					 * secondly, notice all front end that there is a new back end.
					 * thirdly, changed the current server list.
					*/
					if (m.getMethod().equals(Method.ADD)) {
						ServerAddressAndPort primaryAp = DataManager
								.getPrimaryAp();
						if (primaryAp == null) {
							DataManager.setPrimaryAp(sc.getServerAddressAndPort());
							primaryAp = DataManager.getPrimaryAp();
							logger.info("Success add back end: " + sc.getServerAddressAndPort());
							for (ServerAddressAndPort sp : DataManager.getFrontEndAp()) {
								MessageSender.sentMessage(m, sp.getHost(), sp.getPort());
							}
							//serverListChange(m.getMethod(), sc);
							//notice backend it is the first one:
							out.write(("F" + "\n\n").getBytes());
							out.close();
							return;
						}
						String result = MessageSender.sentMessage(m, primaryAp.getHost(),
								primaryAp.getPort());
						//Primary finish all notices to secondary and response "S".
						if (result.trim().equals("S")) {
							logger.info("Success add back end: " + sc.getServerAddressAndPort());
							for (ServerAddressAndPort sp : DataManager.getFrontEndAp()) {
								MessageSender.sentMessage(m, sp.getHost(), sp.getPort());
							}
							serverListChange(m.getMethod(), sc);
						} else {
							//do not to remove Id, because if the server added later the Id will be used.
						}
						out.write(("S" + "\n\n").getBytes());
						out.close();
					} else {
						logger.info("Tell all front ends the back end change.");
						for (ServerAddressAndPort sp : DataManager.getFrontEndAp()) {
							MessageSender.sentMessage(m, sp.getHost(), sp.getPort());
						}
						serverListChange(m.getMethod(), sc);
						out.write(("S" + "\n\n").getBytes());
						out.close();
					}
				} else if (sc.getServerType().equals(
						//Front end changes, discovery will notice load balance which located in "Client".
						ServerChanges.ServerType.FE)) {
					serverListChange(m.getMethod(), sc);
					ServerAddressAndPort loadBalance = DataManager
							.getLoadBalance();
					MessageSender.sentMessage(m, loadBalance.getHost(),
							loadBalance.getPort());
					Message msgToSend = new Message();
					msgToSend.setMethod(Method.BACKEND_LIST);
					msgToSend.setData(new BackendServersList(DataManager
							.getSecondaryAp(), DataManager.getPrimaryAp()));
					out.write((msgToSend.getMessage() + "\n\n").getBytes());
					out.close();
				} else if (sc.getServerType().equals(
						//Front end changes, discovery will notice load balance which located in "Client".
						ServerChanges.ServerType.LB)) {
					serverListChange(m.getMethod(), sc);
					out.write("S\n\n".getBytes());
					logger.debug("Send back to LB");
					out.close();
				}
			} else {
				logger.debug("Unsupport Message.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void serverListChange(Method method, ServerChanges sc) {
		ServerAddressAndPort sp = new ServerAddressAndPort(sc
				.getServerAddressAndPort().getHost(), sc
				.getServerAddressAndPort().getPort());
		if (sc.getServerType().equals(ServerType.FE)) {
			if (method.equals(Method.ADD)) {
				DataManager.addFrontEndAp(sp);
			} else if (method.equals(Method.DELET)) {
				DataManager.removeFrontEndAp(sp);
			}
		} else if (sc.getServerType().equals(ServerType.BE)) {
			if (method.equals(Method.ADD)) {
				DataManager.addBackEndAp(new BackEndServerInfo(sc.getId(), sp));
			} else if (method.equals(Method.DELET)) {
				DataManager.removeBackEndAp(sp);
			} else if (method.equals(Method.SET_PRIMARY)) {
				DataManager.removeBackEndAp(sp);
				DataManager.setPrimaryAp(sp);
			}
		} else if (sc.getServerType().equals(ServerType.LB)) {
			if (method.equals(Method.ADD)) {
				DataManager.setLoadBalance(sp);
			} 
		}
	}
}
