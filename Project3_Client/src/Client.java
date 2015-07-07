import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import model.EmptyMessageData;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import myHttp.HTTPRequestParser;
import myHttp.HTTPResponseContent;
import myHttp.HTTPResponseParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
	private static Logger logger = LogManager.getLogger(Client.class);
//	List<ServerAddressAndPort> frontEndAp = new LinkedList<ServerAddressAndPort>();
//	List<ServerAddressAndPort> backEndAp = new LinkedList<ServerAddressAndPort>();
//	List<ServerAddressAndPort> availableAps = new LinkedList<ServerAddressAndPort>();
	private ServerAddressAndPort loadBalanceServer;
	public Client(ServerAddressAndPort ls) {
		logger.info("New client with loadBalance: " + ls);
		loadBalanceServer = new ServerAddressAndPort(ls.getHost(), ls.getPort());
	}

	
//	// Add front end: method 1, known host & port
//	public Boolean addFrontEnd(String host, int port) {
//
//		ServerAddressAndPort serverAp = new ServerAddressAndPort(host, port);
//		if (availableAps.contains(serverAp)) {
//			frontEndAp.add(serverAp);
//			availableAps.remove(serverAp);
//			
//			ServerAddressAndPort r = randomFE();
//		
//			
//			if(r != null){
//				String content = "1";
//				sentListRequest(content, r.getHost(), r.getPort());
//			} else {
//				
//			};
//			
//			
//			logger.debug("Add new front end: " + serverAp.toString());
//			return true;
//		} else {
//			logger.debug("Add new front end failed, because server:"
//					+ serverAp.toString() + " has been used.");
//			return false;
//
//		}
//	}
//
//	// Add front end: method 2, random
//	public Boolean addFrontEnd() {
//		if (availableAps.size() > 0) {
//			Random r = new Random();
//			int index = r.nextInt(availableAps.size());
//			ServerAddressAndPort ap = availableAps.get(index);
//			frontEndAp.add(ap);
//			availableAps.remove(index);
//			logger.debug("Add new front end: " + ap.toString());
//			return true;
//		} else {
//			logger.debug("There are no server available now.");
//			return false;
//		}
//	}
//
//	// Add back end: method 1, known host & port
//	public Boolean addBackEnd(String host, int port) {
//		ServerAddressAndPort serverAp = new ServerAddressAndPort(host, port);
//		if (availableAps.contains(serverAp)) {
//			backEndAp.add(serverAp);
//			availableAps.remove(serverAp);
//			logger.debug("Add new back end: " + serverAp.toString());
//			return true;
//		} else {
//			logger.debug("Add new back end failed, because server:"
//					+ serverAp.toString() + " has been used.");
//			return false;
//		}
//	}
//
//	// Add back end: method 2, random
//	public Boolean addBackEnd() {
//		if (availableAps.size() > 0) {
//			Random r = new Random();
//			int index = r.nextInt(availableAps.size());
//			ServerAddressAndPort ap = availableAps.get(index);
//			backEndAp.add(ap);
//			availableAps.remove(index);
//			logger.debug("Add new back end: " + ap.toString());
//			return true;
//		} else {
//			logger.debug("There are no server available now.");
//			return false;
//		}
//	}

//	//Randomly choose front end to access.
//	private ServerAddressAndPort randomFE() {
//		if (frontEndAp.size() > 0) {
//			Random r = new Random();
//			return frontEndAp.get(r.nextInt(frontEndAp.size()));
//		} else {
//			logger.error("There is no front end exists.");
//			return null;
//		}
//
//	}

	
	public HTTPResponseContent sendRequest(String content) {
		try {
			logger.info("Sending message...");
			// Client sent request.
			Message m = new Message();
			m.setMethod(Method.GET_FRONT_END);
			m.setData(new EmptyMessageData());
			String fe = MessageSender.sentMessage(m, loadBalanceServer.getHost(), loadBalanceServer.getPort());
			logger.info("Front End to connect: " + fe);
			String[] fea = fe.split(":");
			ServerAddressAndPort s = new ServerAddressAndPort(fea[0], Integer.parseInt(fea[1]));
			logger.info("Using FrontEnd: {}:{}", s.getHost(), s.getPort());
			Socket socket = new Socket(s.getHost(), s.getPort());
			OutputStream out = socket.getOutputStream();
			out.write(content.getBytes());

			// FE receive response.
			BufferedReader incoming = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), HTTPRequestParser.parseHeader(
							content).getCharset()));
			String line = "";
			String responseHeader = "";
			String responseBody = "";
			while (!(line = incoming.readLine()).equals("")) {
				responseHeader += line + "\n";
			}
			responseHeader = responseHeader.substring(0,
					responseHeader.length() - 1);

			HTTPResponseContent hr = HTTPResponseParser.parse(responseHeader);
			for (int i = 0; i < hr.getResponseHeader().getContentLength(); i++) {
				char c = (char) incoming.read();
				responseBody += c;
			}
			hr.setResponseBody(responseBody);
			out.close();
			incoming.close();
			socket.close();
			return hr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
