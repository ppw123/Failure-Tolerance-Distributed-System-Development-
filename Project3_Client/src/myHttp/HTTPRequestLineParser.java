package myHttp;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;  
import org.apache.logging.log4j.Logger; 

public class HTTPRequestLineParser {

  private static Logger logger = LogManager.getLogger(HTTPRequestLine.class);
	/**
	 * This method takes as input the Request-Line exactly as it is read from
	 * the socket. It returns a Java object of type HTTPRequestLine containing a
	 * Java representation of the line.
	 * 
	 * The signature of this method may be modified to throw exceptions you feel
	 * are appropriate. The parameters and return type may not be modified.
	 * 
	 * 
	 * @param line
	 * @return
	 */
	public static HTTPRequestLine parse(String line) {
		// A Request-Line is a METHOD followed by SPACE followed by URI followed
		// by SPACE followed by VERSION
		// A VERSION is 'HTTP/' followed by 1.0 or 1.1
		// A URI is a '/' followed by PATH followed by optional '?' PARAMS
		// PARAMS are of the form key'='value'&'
	  
		String[] containArray = line.split(" ");
		try {
			return new HTTPRequestLine(convertToHTTPMethod(containArray[0]),
					parseUripath(containArray[1]),
					parseUriParams(containArray[1]),
					parseHTTPVersion(containArray[2]));
		} catch (Exception e) {
		  logger.error("Error happends when parse HTTP RequestLine. Error message: {}.", e.getMessage());
			return null;
		}

	}

	private static HTTPConstants.HTTPMethod convertToHTTPMethod(
			String methodString) throws Exception {
		switch (methodString) {
		case "OPTIONS":
			return HTTPConstants.HTTPMethod.OPTIONS;
		case "GET":
			return HTTPConstants.HTTPMethod.GET;
		case "HEAD":
			return HTTPConstants.HTTPMethod.HEAD;
		case "POST":
			return HTTPConstants.HTTPMethod.POST;
		case "PUT":
			return HTTPConstants.HTTPMethod.PUT;
		case "DELETE":
			return HTTPConstants.HTTPMethod.DELETE;
		case "TRACE":
			return HTTPConstants.HTTPMethod.TRACE;
		case "CONNECT":
			return HTTPConstants.HTTPMethod.CONNECT;
		}
		throw new Exception("Error method");

	}

	private static String parseUripath(String uri) {
		String[] uripath = uri.split("\\?");
		return uripath[0];
	}

	private static HashMap<String, String> parseUriParams(String uri)
			throws Exception {
		HashMap<String, String> uriParams = new HashMap<String, String>();
		if (uri.split("\\?").length > 1) {
			String params = uri.split("\\?")[1];
			String[] paramArray = params.split("&");
			for (String param : paramArray) {
				String[] kv = param.split("=");
				try {
					String key = "";
					String value = "";
					key = java.net.URLDecoder.decode(kv[0].trim(), "UTF-8");
					if (kv.length > 1) {
						if (kv.length > 2) {
							throw new Exception("error param");
						}
						value = java.net.URLDecoder.decode(kv[1].trim(),
								"UTF-8");
					} else {
						value = "";
					}
					uriParams.put(key, value);
				} catch (UnsupportedEncodingException e) {
					System.out
							.println("Cannot decode string." + e.getMessage());
				}
			}

		}
		return uriParams;
	}

	private static String parseHTTPVersion(String version) throws Exception {
		if (version.equals("HTTP/1.0") || version.equals("HTTP/1.1")) {
			return version;
		} else {
			throw new Exception("Error version");
		}

	}
}