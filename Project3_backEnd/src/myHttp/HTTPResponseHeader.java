package myHttp;

public class HTTPResponseHeader {
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
