import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Message;


public class MessageSender {
	private static Logger logger = LogManager.getLogger(MessageSender.class);

    public static String sentMessage(Message m, String host, int port){
  		Socket socket;
  		try {
  			logger.debug("Send message (" + m.getMessage() + " ) to " + host + ":" + port);
  			socket = new Socket(host, port);
  			OutputStream out = socket.getOutputStream();
  			out.write((m.getMessage() + "\n\n").getBytes());
  			//out.flush();
  			BufferedReader incoming = new BufferedReader(new InputStreamReader(
  					socket.getInputStream()));
  			
  			String line = "";
  			String response = "";
  			while (!(line = incoming.readLine()).equals("")){
  				logger.debug(line);
  				response += line + "\n";
  			}
  			response = response.substring(0, response.length() - 1);
  			out.close();
  			incoming.close();
  			socket.close();
  			
  			return response;
  		} catch (UnknownHostException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		} catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
  		return null;
  		
  	}
}
