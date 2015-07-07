package myHttp;

public class HTTPResponseHeader {
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((contentLength == null) ? 0 : contentLength.hashCode());
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
    HTTPResponseHeader other = (HTTPResponseHeader) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (contentLength == null) {
      if (other.contentLength != null)
        return false;
    } else if (!contentLength.equals(other.contentLength))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "HTTPResponseHeader [code=" + code + ", contentLength=" + contentLength + "]";
  }

  private String code;
  private Integer contentLength;
  
  public HTTPResponseHeader(String code, Integer contentLength) {
    super();
    this.code = code;
    this.contentLength = contentLength;
  }

  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public Integer getContentLength() {
    return contentLength;
  }
  public void setContentLength(Integer contentLength) {
    this.contentLength = contentLength;
  }
  
  public String getResponseHeaderString() {
    return this.code + "\n" + "Content-Length: " + this.contentLength;
  }

}
