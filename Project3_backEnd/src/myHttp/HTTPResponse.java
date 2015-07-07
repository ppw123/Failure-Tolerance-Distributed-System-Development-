package myHttp;
public class HTTPResponse {

  public static HTTPResponseContent response_400() {
    HTTPResponseContent response_400 = new HTTPResponseContent();
    response_400.setResponseBody("");
    response_400.setResponseHeader(new HTTPResponseHeader("HTTP/1.1 400 Bad Request", response_400.getResponseBody().length()));
    return response_400;
  }

  public static HTTPResponseContent response_201() {
    HTTPResponseContent response_201 = new HTTPResponseContent();
    response_201.setResponseBody("");
    response_201.setResponseHeader(new HTTPResponseHeader("HTTP/1.1 201 Created", response_201.getResponseBody().length()));
    return response_201;
  }

  public static HTTPResponseContent response_500() {
    HTTPResponseContent response_500 = new HTTPResponseContent();
    response_500.setResponseBody("");
    response_500.setResponseHeader(new HTTPResponseHeader("HTTP/1.1 500 Internal Server Error", response_500.getResponseBody().length()));
    return response_500;
  }

  public static HTTPResponseContent response_404() {
    HTTPResponseContent response_404 = new HTTPResponseContent();
    response_404.setResponseBody("");
    response_404.setResponseHeader(new HTTPResponseHeader("HTTP/1.1 404 Not Found", response_404.getResponseBody().length()));
    return response_404;
  }

  public static HTTPResponseContent response_304() {
    HTTPResponseContent response_304 = new HTTPResponseContent();
    response_304.setResponseBody("");
    response_304.setResponseHeader(new HTTPResponseHeader("HTTP/1.1 304 Not Modified", response_304.getResponseBody().length()));
    return response_304;
  }

  public static HTTPResponseContent response_200() {
    HTTPResponseContent response_200 = new HTTPResponseContent();
    response_200.setResponseBody("");
    response_200.setResponseHeader(new HTTPResponseHeader("HTTP/1.1 200 Ok", response_200.getResponseBody().length()));
    return response_200;
  }

}
