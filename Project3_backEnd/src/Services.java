import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.BackEndServerInfo;
import model.BackendServersList;
import model.IdMessageData;
import model.Message;
import model.Message.Method;
import model.ServerAddressAndPort;
import model.ServerChanges;
import model.ServerChanges.ServerType;
import myHttp.HTTPConstants;
import myHttp.HTTPConstants.HTTPMethod;
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
  private static Logger logger = LogManager.getLogger(Services.class);
  private Socket socket;
  private Boolean isInit = false;

  public Services() {
  }

  public Services(Socket socket, Boolean isInit) {
    this.socket = socket;
    this.isInit = isInit;
  }

  @SuppressWarnings("unchecked")
  public void run() {
    if (isInit) {
      isInit = false;
      // notice discovery
      Message m = new Message();
      m.setMethod(Method.ADD);
      ServerChanges sc = new ServerChanges();
      sc.setId(-1);
      sc.setServerType(ServerType.BE);
      sc.setServerAddressAndPort(new ServerAddressAndPort(HttpServer.getHost(), HttpServer.getPort()));
      m.setData(sc);
      String response = MessageSender.sentMessage(m, HttpServer.getDiscoveryHost(), HttpServer.getDiscoveryPort());

      if (response.trim().equals("F")) {
        logger.info("This is the first back end, set as primary");
        BackendMetaData.setPrimary(true);
        BackendMetaData.setPrimary(new ServerAddressAndPort(HttpServer.getHost(), HttpServer.getPort()));
        BackendMetaData.setId(sc.getId());
      }
      return;
    } else {
      try {

        BufferedReader incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestHeader = "";
        String requestBody = "";
        String line;

        String responseBody = "";

        // Get the whole request contents.
        while (!(line = incoming.readLine()).equals("")) {
          requestHeader += line + "\r\n";
        }
        requestHeader = requestHeader.substring(0, requestHeader.length() - 2);
        // logger.debug("request: " + requestHeader);
        if (requestHeader.startsWith("MSG")) {
          Message m = new Message(requestHeader);

          if (m.getMethod().equals(Method.ADD)) {
            ServerChanges sc = (ServerChanges) m.getData();
            logger.info("Add a new back end: " + sc.getServerAddressAndPort());
            // if
            // (HttpServer.getHost().equals(sc.getServerAddressAndPort().getHost())
            // &&
            // HttpServer.getPort() ==
            // sc.getServerAddressAndPort().getPort()) {
            // logger.info("This is the first back end, set as primary");
            // BackendMetaData.setPrimary(true);
            // BackendMetaData.setPrimary(new
            // ServerAddressAndPort(HttpServer.getHost(),
            // HttpServer.getPort()));
            // BackendMetaData.setId(sc.getId());
            // return;
            // }

            if (BackendMetaData.isPrimary()) {
              // notice discovery
              OutputStream out = socket.getOutputStream();
              out.write("S\n\n".getBytes());
              out.close();
              // tell that secondary the list
              Message msgOfSecondaryList = new Message();
              msgOfSecondaryList.setMethod(Method.BACKEND_LIST);
              BackendServersList bsl = new BackendServersList();
              bsl.setPrimaryAp(BackendMetaData.getPrimary());
              bsl.setSecondaryAp(BackendMetaData.getSecondaryServerList());
              msgOfSecondaryList.setData(bsl);
              logger.debug("Send current secondary list to new secondary." + msgOfSecondaryList.getMessage());
              MessageSender.sentMessage(msgOfSecondaryList, sc.getServerAddressAndPort().getHost(), sc
                  .getServerAddressAndPort().getPort());
              // copy data to it
              logger.debug("Copy data to new secondary.");
              List<String> tweets = Storage.getAllTweets();
              for (String tweet : tweets) {
                JSONObject beBody = new JSONObject();
                beBody.put("tweet", tweet);
                JSONArray hashtags = new JSONArray();
                String[] textS = tweet.split("#");
                for (int i = 1; i < textS.length; i++) {
                  hashtags.add(textS[i].split(" ").clone()[0]);
                }
                beBody.put("hashtags", hashtags);
                HTTPRequest hr = new HTTPRequest();
                hr.setCharset("utf-8");
                HTTPRequestLine hrl = new HTTPRequestLine();
                hrl.setMethod(HTTPMethod.POST);
                hrl.setHttpversion("HTTP/1.1");
                hrl.setParameters(new HashMap<String, String>());
                hrl.setUripath("/tweets");
                hr.setHttpRL(hrl);
                hr.setCt("application/json");
                hr.setContentLength(beBody.toJSONString().length());
                hr.setBody(beBody.toJSONString());
                HTTPResponseContent response = sendRequest(hr, sc.getServerAddressAndPort().getHost(), sc
                    .getServerAddressAndPort().getPort());
                if (!response.getResponseHeader().getCode().equals("HTTP/1.1 201 Created")) {
                  // retry;
                  // response = sendRequest(hr, sc
                  // .getServerAddressAndPort()
                  // .getHost(), sc
                  // .getServerAddressAndPort()
                  // .getPort());
                  logger.debug("Cannot get correct response when copy message to new secondary.");
                }
              }
              logger.info("Tell all secondaries the new one.");
              // add to list
              BackendMetaData.addSecondaryMap(sc.getServerAddressAndPort().getHost(), sc.getServerAddressAndPort()
                  .getPort(), sc.getId());
              // tell all secondary
              for (ServerAddressAndPort sap : BackendMetaData.getActiveSecondaryMap().values()) {
                MessageSender.sentMessage(m, sap.getHost(), sap.getPort());
              }
              logger.info("Tell the new secondary id :" + sc.getId());
              // tell that secondary the id
              Message msgId = new Message();
              msgId.setMethod(Method.ID);
              IdMessageData idmd = new IdMessageData(sc.getId());
              msgId.setData(idmd);
              MessageSender.sentMessage(msgId, sc.getServerAddressAndPort().getHost(), sc.getServerAddressAndPort()
                  .getPort());
              logger.info("Add to secondary list.");

              return;

            }
            // add to list
            BackendMetaData.addSecondaryMap(sc.getServerAddressAndPort().getHost(), sc.getServerAddressAndPort()
                .getPort(), sc.getId());
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.BACKEND_LIST)) {
            logger.info("Received backend list.");
            BackendServersList bsl = (BackendServersList) m.getData();
            BackendMetaData.setPrimary(bsl.getPrimaryAp());
            Map<Integer, ServerAddressAndPort> mm = new HashMap<Integer, ServerAddressAndPort>();
            for (BackEndServerInfo bei : bsl.getSecondaryAp()) {
              mm.put(bei.getId(), bei.getSp());
            }
            BackendMetaData.setActiveSecondaryMap(mm);
            logger.info("finish set backend list.");
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.DELET)) {
            ServerChanges sc = (ServerChanges) m.getData();
            logger.info("Remove a back end: " + sc.getServerAddressAndPort());
            BackendMetaData.removeSecondaryMap(sc.getServerAddressAndPort().getHost(), sc.getServerAddressAndPort()
                .getPort());
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.SET_PRIMARY)) {
            ServerChanges sc = (ServerChanges) m.getData();
            BackendMetaData.setPrimary(sc.getServerAddressAndPort());
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.HELLO)) {
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.ID)) {
            IdMessageData idmd = (IdMessageData) m.getData();
            BackendMetaData.setId(idmd.getId());
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.SET_VERSION)) {
            IdMessageData idmd = (IdMessageData) m.getData();
            Storage.syncVersion(idmd.getId());
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.GET_VERSION)) {
            OutputStream out = socket.getOutputStream();
            out.write((Storage.getVersion().toString() + "\n\n").getBytes());
            out.close();
            return;
          } else if (m.getMethod().equals(Method.BULLY)) {
            OutputStream out = socket.getOutputStream();
            out.write("S\n\n".getBytes());
            out.close();
            BullyServices bs = new BullyServices();
            bs.bully();
            return;
          }
        } else {
          HTTPRequest hr = HTTPRequestParser.parseHeader(requestHeader);
          for (int i = 0; i < hr.getContentLength(); i++) {
            char c = (char) incoming.read();
            requestBody += c;
          }
          hr.setBody(requestBody);

          HTTPRequestLine httpRL = hr.getHttpRL();
          HTTPResponseContent toFrontEnd = null;
          logger.debug("Receive a request.\nHeader:\n{}\nBody:\n{}", requestHeader, requestBody);

          // POST REQUEST

          if (httpRL.getMethod().equals(HTTPConstants.HTTPMethod.POST)) {
            if (httpRL.getUripath().equals("/tweets")) {
              if (hr.getCt().equals("application/json")) {
                logger.debug("Post request. body:{}.", hr.getBody());
                JSONParser jp = new JSONParser();
                JSONObject joBody;
                try {
                  joBody = (JSONObject) jp.parse(hr.getBody());
                  String tweet = joBody.get("tweet").toString();
                  JSONArray hashtag = (JSONArray) joBody.get("hashtags");
                  Storage.addTweet(tweet, hashtag);
                  logger.info("Create a tweet into this backend: " + tweet);
                  
                  if (BackendMetaData.isPrimary()) {
                    logger.info("This is primary backend, will copy the tweet to secondary nodes:");
                    for (Integer key : getSeconday().keySet()) {
                      ServerAddressAndPort s = getSeconday().get(key);
                      HTTPResponseContent response = sendRequest(hr, s.getHost(), s.getPort());

                      if (!response.getResponseHeader().getCode().equals("HTTP/1.1 201 Created")) {
                        logger.debug(response.getResponseHeader().getCode());
                        logger.debug("Secondary replica" + s.getHost() + s.getPort()
                            + " failed, and cannot sent request to it!");
                        removeSecondary(s.getHost(), s.getPort());

                      }
                      logger.info("Copied to secondary with id " + key + " and " + s);
                    }
                  }
                  logger.info("Success created tweet.");
                  toFrontEnd = HTTPResponse.response_201();
                } catch (ParseException e) {
                  toFrontEnd = HTTPResponse.response_500();
                  logger.error("Internal Error happends when create tweet. Error message: {}", e.getMessage());
                  e.printStackTrace();
                }
              }
            } else {
              logger.info("Page \"{}\" not found.", httpRL.getUripath());
              toFrontEnd = HTTPResponse.response_404();
            }

          }
          // GET REQUEST.
          else if (httpRL.getMethod().equals(HTTPConstants.HTTPMethod.GET)) {
            if (httpRL.getUripath().equals("/tweets")) {
              if (httpRL.getParameters().containsKey("q") && httpRL.getParameters().containsKey("v")) {
                // If 200 ok
                String key = httpRL.getParameters().get("q");
                Integer version = Integer.parseInt(httpRL.getParameters().get("v"));
                logger.debug("Response for get Request, key:{}, version:{}.", key, version);
                responseBody = Storage.readTweet(key, version);
                if (responseBody == null) {
                  logger.info("Version not modifiyed.");
                  toFrontEnd = HTTPResponse.response_304();
                } else {
                  logger.info("Return a new response body for get:{}.", responseBody);
                  toFrontEnd = HTTPResponse.response_200();
                  toFrontEnd.setResponseBody(responseBody);
                  toFrontEnd.getResponseHeader().setContentLength(responseBody.length());
                }
              } else {
                // unknown internal server error
                logger.info("Do not have version or query word in Get request.");
                toFrontEnd = HTTPResponse.response_500();
              }
            } else {
              // uri is not /tweets
              logger.info("Page \"{}\" not found.", httpRL.getUripath());
              toFrontEnd = HTTPResponse.response_404();
            }
          }
          OutputStream out = socket.getOutputStream();
          String toFrontEndResponse = toFrontEnd.getResponseString();
          logger.debug("Response: {}", toFrontEndResponse);
          out.write(toFrontEndResponse.getBytes());
 //         out.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // Used to keep the secondarylist are the same in both primary and secondary
  // replica.
  public void removeSecondary(String host, int port) {
    BackendMetaData.removeSecondaryMap(host, port);
    Message m = new Message();
    m.setMethod(Method.DELET);
    ServerChanges sc = new ServerChanges(ServerType.BE, host, port, -1);
    m.setData(sc);
    Map<Integer, ServerAddressAndPort> activeSecondaryMap = BackendMetaData.getActiveSecondaryMap();
    for (Integer key : activeSecondaryMap.keySet()) {
      MessageSender.sentMessage(m, activeSecondaryMap.get(key).getHost(), activeSecondaryMap.get(key).getPort());
    }
    // notice discovery
    MessageSender.sentMessage(m, HttpServer.getDiscoveryHost(), HttpServer.getDiscoveryPort());
  }

  public void addSecondary(String host, int port, Integer id) {
    BackendMetaData.addSecondaryMap(host, port, id);
    Message m = new Message();
    m.setMethod(Method.ADD);
    ServerChanges sc = new ServerChanges(ServerType.BE, host, port, id);
    m.setData(sc);
    Map<Integer, ServerAddressAndPort> activeSecondaryMap = BackendMetaData.getActiveSecondaryMap();
    for (Integer key : activeSecondaryMap.keySet()) {
      MessageSender.sentMessage(m, activeSecondaryMap.get(key).getHost(), activeSecondaryMap.get(key).getPort());
    }
  }

  public Map<Integer, ServerAddressAndPort> getSeconday() {
    return BackendMetaData.getActiveSecondaryMap();
  }

  private HTTPResponseContent sendRequest(HTTPRequest httpRequest, String host, int port) {
    try {
      // Primary replica sent request to secondary replica.
      String content = httpRequest.getHTTPRequestString();
      Socket socket = new Socket(host, port);
      OutputStream out = socket.getOutputStream();
      out.write(content.getBytes());

      // Primary replica receive response.
      BufferedReader incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), HTTPRequestParser
          .parseHeader(content).getCharset()));
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
