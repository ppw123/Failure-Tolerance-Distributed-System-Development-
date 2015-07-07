package myHttp;
import org.apache.logging.log4j.LogManager;  
import org.apache.logging.log4j.Logger; 

public class HTTPRequest {

	// http request consists of requestline and content-type.
  private HTTPRequestLine httpRL;
  private String ct;
  private String charset;
  private Integer contentLength;
  private String body;
	private static Logger logger = LogManager.getLogger(HTTPRequest.class);

	public HTTPRequest() {

	}

	public HTTPRequest(HTTPRequestLine httpRL, String ct, String charset, Integer contentLength,
			String body) {
		super();
		this.httpRL = httpRL;
		this.ct = ct;
		this.charset = charset;
		this.contentLength = contentLength;
		this.body = body;
	}

	public String getHTTPRequestString(){
		String httpRequestString = this.httpRL.getHTTPRequestLineString() + "\r\n" + "Content-Length: " + this.body.length() + "\r\n" + "Content-Type: " +
	this.ct + ";charser=" + this.charset + "\r\n\r\n" + this.body;
		logger.debug("HTTP Request String is {}.", httpRequestString);
		return httpRequestString;
	}
	
	public HTTPRequestLine getHttpRL() {
		return httpRL;
	}

	public void setHttpRL(HTTPRequestLine httpRL) {
		this.httpRL = httpRL;
	}

	public String getCt() {
		return ct;
	}

	public void setCt(String ct) {
		this.ct = ct;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Integer getContentLength() {
    return contentLength;
  }

  public void setContentLength(Integer contentLength) {
    this.contentLength = contentLength;
  }

  public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "HTTPRequest [httpRL=" + httpRL + ", ct=" + ct + ", charset="
				+ charset + ", body=" + body + "]";
	}

}
