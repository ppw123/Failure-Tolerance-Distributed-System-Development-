package myHttp;
public class HTTPResponseContent {

  private String responseBody;
  private HTTPResponseHeader responseHeader;

  public HTTPResponseContent() {

  }

  public HTTPResponseContent(String responseBody, HTTPResponseHeader responseHeader) {
    this.responseBody = responseBody;
    this.responseHeader = responseHeader;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public HTTPResponseHeader getResponseHeader() {
    return responseHeader;
  }

  public void setResponseHeader(HTTPResponseHeader responseHeader) {
    this.responseHeader = responseHeader;
  }

  @Override
  public String toString() {
    return "HTTPResponse [responseBody=" + responseBody + ", responseHeader=" + responseHeader + "]";
  }

  public String getResponseString() {
    return this.responseHeader.getResponseHeaderString() + "\n\n" + this.responseBody;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((responseBody == null) ? 0 : responseBody.hashCode());
    result = prime * result + ((responseHeader == null) ? 0 : responseHeader.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HTTPResponseContent other = (HTTPResponseContent) obj;
    if (responseBody == null) {
      if (other.responseBody != null)
        return false;
    } else if (!responseBody.equals(other.responseBody))
      return false;
    if (responseHeader == null) {
      if (other.responseHeader != null)
        return false;
    } else if (!responseHeader.equals(other.responseHeader))
      return false;
    return true;
  }

}
