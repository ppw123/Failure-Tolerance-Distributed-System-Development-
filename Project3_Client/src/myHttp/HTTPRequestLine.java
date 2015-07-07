package myHttp;
import java.util.HashMap;

/**
 * HTTPRequestLine is a data structure that stores a Java representation of the
 * parsed Request-Line.
 **/
public class HTTPRequestLine {

	private HTTPConstants.HTTPMethod method;
	private String uripath;
	private HashMap<String, String> parameters;
	private String httpversion;

	public HTTPRequestLine() {

	}

	public HTTPRequestLine(HTTPConstants.HTTPMethod method, String uripath,
			HashMap<String, String> parameters, String httpversion) {

		this.method = method;
		this.uripath = uripath;
		this.parameters = parameters;
		this.httpversion = httpversion;
	}

	public String getHTTPRequestLineString() {
		String parameterString = "";
		for (String key : this.parameters.keySet()) {
			parameterString += key + "=" + this.parameters.get(key) + "&";
		}
		if (parameterString.endsWith("&")) {
      parameterString = parameterString.substring(0,
        parameterString.length() - 1);
    }
		String s = this.method + " " + this.uripath + "?" + parameterString
				+ " " + this.httpversion;
		return s;
	}

	public HTTPConstants.HTTPMethod getMethod() {
		return method;
	}

	public void setMethod(HTTPConstants.HTTPMethod method) {
		this.method = method;
	}

	public String getUripath() {
		return uripath;
	}

	public void setUripath(String uripath) {
		this.uripath = uripath;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getHttpversion() {
		return httpversion;
	}

	public void setHttpversion(String httpversion) {
		this.httpversion = httpversion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((httpversion == null) ? 0 : httpversion.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((uripath == null) ? 0 : uripath.hashCode());
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
		HTTPRequestLine other = (HTTPRequestLine) obj;
		if (httpversion == null) {
			if (other.httpversion != null)
				return false;
		} else if (!httpversion.equals(other.httpversion))
			return false;
		if (method != other.method)
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (uripath == null) {
			if (other.uripath != null)
				return false;
		} else if (!uripath.equals(other.uripath))
			return false;
		return true;
	}

}