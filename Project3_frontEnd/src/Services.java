import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import model.BackEndServerInfo;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import myHttp.HTTPConstants;
import myHttp.HTTPRequest;
import myHttp.HTTPRequestLine;
import myHttp.HTTPRequestParser;
import myHttp.HTTPResponse;
import myHttp.HTTPResponseContent;
import myHttp.HTTPResponseParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Services implements Runnable {
  private Socket socket;
  private static Logger logger = LogManager.getLogger(Services.class);
//zengjia jieshou backend change
  //xiugai du xie
  public Services() {
  }

  public Services(Socket socket) {
    this.socket = socket;
  }

  @SuppressWarnings("unchecked")
  public void run() {

    try {
      BufferedReader incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String requestHeader = "";
      String requestBody = "";
      String line;

      String responseBody = "";

      // Get the request header.
      while (!(line = incoming.readLine()).equals("")) {
        requestHeader += line + "\r\n";
      }
      requestHeader = requestHeader.substring(0, requestHeader.length() - 2);

      try {
    	 Message m = new Message(requestHeader);
    	 ServerChanges sc = (ServerChanges) m.getData();
    	 
    	 if (m.getMethod().equals(Method.ADD)) {
    		 logger.info("Add a new back end: " + sc.getServerAddressAndPort());
    		 HttpServer.addBackEnd(new BackEndServerInfo(sc.getId(), sc.getServerAddressAndPort()));
    		 OutputStream out = socket.getOutputStream();
    	     out.write(("S\n\n").getBytes());
    	     out.close();
    		 return;
    	 } else if (m.getMethod().equals(Method.DELET)) {
    		 logger.info("Remove a back end: " + sc.getServerAddressAndPort());
    		 HttpServer.removeBackEnd(sc.getServerAddressAndPort());
    		 OutputStream out = socket.getOutputStream();
    	     out.write(("S\n\n").getBytes());
    	     out.close();
    		 return;
    	 } else if (m.getMethod().equals(Method.SET_PRIMARY)) {
    		 logger.info("Set primary: " + sc.getServerAddressAndPort());
    		 HttpServer.changePrimary(sc.getServerAddressAndPort());
    		 OutputStream out = socket.getOutputStream();
    	     out.write(("S\n\n").getBytes());
    	     out.close();
    		 return;
    	 }
      } catch (Exception e) {
    	 logger.debug("Not supported Message Request.");
      }
      
      HTTPRequest hr = HTTPRequestParser.parseHeader(requestHeader);
      for (int i = 0; i < hr.getContentLength(); i++) {
        char c = (char) incoming.read();
        requestBody += c;
      }
      hr.setBody(requestBody);
      logger.debug("Receive a request from client.\nHeader:\n{}\nBody:\n{}", requestHeader, requestBody);
      HTTPRequestLine httpRL = hr.getHttpRL();
      HTTPResponseContent toClient = null;

      // POST REQUEST
      if (httpRL.getMethod().equals(HTTPConstants.HTTPMethod.POST)) {
        if (httpRL.getUripath().equals("/tweets")) {
          if (hr.getCt().equals("application/json")) {
            JSONParser jp = new JSONParser();
            JSONObject joBody;
            try {
              joBody = (JSONObject) jp.parse(hr.getBody());

              if (joBody.get("text") != null) {
                String text = joBody.get("text").toString();
                if (text.contains("#")) {

                  if (text.endsWith("#") || text.contains("# ")) {
                    // If there is only #.
                    toClient = HTTPResponse.response_400();
                  } else {
                    // 201 response.

                    // send post request to back end.
                    JSONObject beBody = new JSONObject();
                    beBody.put("tweet", text);
                    JSONArray hashtags = new JSONArray();
                    String[] textS = text.split("#");
                    for (int i = 1; i < textS.length; i++) {
                      hashtags.add(textS[i].split(" ").clone()[0]);
                    }
                    beBody.put("hashtags", hashtags);
                    
                    //eventual consistency
                    String consistency = httpRL.getParameters().get("c");
                    if (consistency != "eventual") {
                      consistency = "strong";
                    }
                    beBody.put("consistency", consistency);
                    
                    hr.setBody(beBody.toJSONString());
                    ServerAddressAndPort sp = getBackEndToWrite(consistency);
                    HTTPResponseContent response = sendRequest(hr, sp.getHost(), sp.getPort());
                    logger.debug("Response from backend: {}", response.getResponseString());
                    String successResponseCode = HTTPResponse.response_201().getResponseHeader().getCode();
                    if (successResponseCode.equals(response.getResponseHeader().getCode())) {
                      toClient = HTTPResponse.response_201();
                    } else {
                      toClient = HTTPResponse.response_500();
                    }
                  }
                } else {
                  // If there is no # hash tag
                  logger.info("No hash tag.");
                  toClient = HTTPResponse.response_400();
                }
              } else {
                toClient = HTTPResponse.response_400();
              }

            } catch (ParseException e) {
              // If not jason format
              toClient = HTTPResponse.response_400();
              e.printStackTrace();
            }

          }

        } else {
          // If not "/tweets".
          logger.error("Page {} not found", httpRL.getUripath());
          toClient = HTTPResponse.response_404();
        }

      }
      // GET REQUEST.
      else if (httpRL.getMethod().equals(HTTPConstants.HTTPMethod.GET)) {
        if (httpRL.getUripath().equals("/tweets")) {
          if (httpRL.getParameters().containsKey("q") && !httpRL.getParameters().get("q").contains(" ")) {

            // If 200 ok
            String key = httpRL.getParameters().get("q");
            Integer versionNumber = FrontEndCache.getVersionNum(key);

            hr.getHttpRL().getParameters().put("v", versionNumber.toString());
            ServerAddressAndPort sp = getBcakEndToRead();
            HTTPResponseContent response = sendRequest(hr, sp.getHost(), sp.getPort());

            HTTPResponseContent response1 = HTTPResponse.response_304();
            HTTPResponseContent response2 = HTTPResponse.response_200();

            if (response1.equals(response)) {
              // return from cache.
              responseBody = FrontEndCache.searchInCache(key);
              logger.debug("Response body from cache: {}.", responseBody);
            } else if (response.getResponseHeader().getCode().equals(response2.getResponseHeader().getCode())) {
              // return the data from back end.
              responseBody = response.getResponseBody();
              logger.debug("Response body from backend: {}.", responseBody);
              // store cache
              FrontEndCache.saveToCache(responseBody);
            }
            JSONParser jp = new JSONParser();
            try {
              JSONObject jb = (JSONObject) jp.parse(responseBody);
              JSONObject jToClient = new JSONObject();
              jToClient.put("q", jb.get("q"));
              jToClient.put("tweets", jb.get("tweets"));
              responseBody = jToClient.toJSONString();
            } catch (ParseException e) {
              e.printStackTrace();
            }
            logger.debug("Response body ready to send to client: {}.", responseBody);
            toClient = HTTPResponse.response_200();
            toClient.setResponseBody(responseBody);
            toClient.getResponseHeader().setContentLength(responseBody.length());
          } else {

            toClient = HTTPResponse.response_400();
          }
        } else {
          // If not "/tweets".
          toClient = HTTPResponse.response_404();
        }
      } else if (httpRL.getMethod().equals(HTTPConstants.HTTPMethod.PUT)) {
        toClient = HTTPResponse.response_503();
      } else if (httpRL.getMethod().equals(HTTPConstants.HTTPMethod.DELETE)) {
        toClient = HTTPResponse.response_503();
      }
      OutputStream out = socket.getOutputStream();
      out.write(toClient.getResponseString().getBytes());

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private ServerAddressAndPort getBackEndToWrite() {
	  return HttpServer.getPrimary();
  }
  
  private ServerAddressAndPort getBcakEndToRead() {
	  Random r = new Random();
	  List<BackEndServerInfo> availableAps = HttpServer.getSecondaryList();
	  if (availableAps.size() == 0) {
		  return HttpServer.getPrimary();
	  }
	  int index = r.nextInt(availableAps.size());
	  return availableAps.get(index).getSp();
  }

  private HTTPResponseContent sendRequest(HTTPRequest httpRequest, String host, int port) {
    try {
      // FE sent request.
      String content = httpRequest.getHTTPRequestString();
      Socket socket = new Socket(host, port);
      OutputStream out = socket.getOutputStream();
      out.write(content.getBytes());

      // FE receive response.
      BufferedReader incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), HTTPRequestParser.parseHeader(content).getCharset()));
      String line = "";
      String responseHeader = "";
      String responseBody = "";
      while (!(line = incoming.readLine()).equals("")) {
        responseHeader += line + "\n";
      }
      responseHeader = responseHeader.substring(0, responseHeader.length() - 1);

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
