package myHttp;
public class HTTPRequestParser {

  public static HTTPRequest parseHeader(String requestHeader) {
    String[] header = requestHeader.split("\r\n");
    String httpRequestLine = header[0];

    String ct = "";
    String charset = "";
    Integer contentLength = 0;
    for (String line : header) {
      if (line.startsWith("Content-Type:")) {
        ct = line.split(";")[0].split(":")[1].trim();
        charset = line.split(";")[1].split("=")[1].trim();
      }
      if (line.startsWith("Content-Length:")) {
        contentLength = Integer.parseInt(line.split(":")[1].trim());
      }
    }

    return new HTTPRequest(HTTPRequestLineParser.parse(httpRequestLine), ct, charset, contentLength, null);
  }
}
