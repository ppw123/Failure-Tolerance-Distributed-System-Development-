package myHttp;
public class HTTPResponseParser {
  public static HTTPResponseContent parse(String response) {

    String[] responseArray = response.split("\n\n");
    String header = responseArray[0];
    String[] headerArray = header.split("\n");
    String code = headerArray[0];
    Integer contentLength = 0;
    for (String line : headerArray) {
      if (line.startsWith("Content-Length:")) {
        contentLength = Integer.parseInt(line.split(":")[1].trim());
      }
    }
    if (responseArray.length > 1) {
      return new HTTPResponseContent(responseArray[1], new HTTPResponseHeader(code, contentLength));
    } else {
      return new HTTPResponseContent("", new HTTPResponseHeader(code, contentLength));
    }
  }
}
