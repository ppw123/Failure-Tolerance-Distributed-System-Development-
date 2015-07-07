import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Service implements Runnable {
	private static Logger logger = LogManager.getLogger(Service.class);
	private Socket socket;

	public Service() {
	}

	public Service(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {

			BufferedReader incoming = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String request = "";
			String line;

			// Get the whole request contents.
			while (!(line = incoming.readLine()).equals("")) {
				request += line + "\r\n";
			}
			request = request.substring(0, request.length() - 2);
			Message m;
			try {
				m = new Message(request);
				if (m.getMethod().equals(Method.ADD)) {
					ServerChanges sc = (ServerChanges) m.getData();
					logger.info("Add Front End Server to loadbalance: " + sc.getServerAddressAndPort());
					LoadBalance.addFrontEndServer(sc.getServerAddressAndPort());
					OutputStream out = socket.getOutputStream();
					out.write(("S" + "\n\n").getBytes());
					out.close();
				} else if (m.getMethod().equals(Method.GET_FRONT_END)) {
					List<ServerAddressAndPort> frontEndAp = LoadBalance
							.getFrontEndServers();
					if (frontEndAp.size() > 0) {
						Random r = new Random();
						ServerAddressAndPort selected = frontEndAp.get(r
								.nextInt(frontEndAp.size()));
						OutputStream out = socket.getOutputStream();
						out.write((selected.getHost() + ":" + selected
								.getPort() + "\n\n").getBytes());
						out.close();
					} else {
						logger.error("There is no front end exists.");
					}
				}
			} catch (Exception e) {
				// TODO log
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
	}

}
