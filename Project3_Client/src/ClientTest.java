import model.ServerAddressAndPort;
import myHttp.HTTPResponse;
import myHttp.HTTPResponseContent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Does not assert value in this test, because 
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientTest {
	static ServerAddressAndPort lb = new ServerAddressAndPort("localhost", 7080);
	static Client c = new Client(lb);
	private static Logger logger = LogManager.getLogger(Client.class);

	@BeforeClass
	public static void addFrontEnd() {
//		String host1 = "localhost";
//		String host2 = "localhost";
//		 String host1 = "mc01";
//		 String host2 = "mc02";
//		Integer port1 = 7051;
//		Integer port2 = 7052;
//		logger.info("Add front end\n{}:{}\n{}:{}", host1, port1, host2, port2);
//		c.addFrontEnd(host1, port1);
//		c.addFrontEnd(host2, port2);
	}

	@Test
	public void test01_correctPost() {
		logger.info("Test correct post.");
		String body = "{\"text\":\"correct Post #ht\"}";
		String content = "POST /tweets HTTP/1.1\r\nContent-Length:"
				+ body.length()
				+ "\r\nContent-Type: application/json;charset=utf-8\r\n\r\n"
				+ body;
		HTTPResponseContent response = c.sendRequest(content);
		logger.info("Response from correct post: \n{}.",
				response.getResponseString());
		Assert.assertEquals(response, HTTPResponse.response_201());
	}

	@Test
	public void test02_correctPost2() {
		logger.info("Test correct post2.");
		String body = "{\"text\":\"correct Post 2 #ht #ts\"}";
		String content = "POST /tweets HTTP/1.1\r\nContent-Length:"
				+ body.length()
				+ "\r\nContent-Type: application/json;charset=utf-8\r\n\r\n"
				+ body;
		HTTPResponseContent response = c.sendRequest(content);
		logger.info("Response from correct post2: \n{}.",
				response.getResponseString());
		Assert.assertEquals(response, HTTPResponse.response_201());
	}
	
	@Test
	public void test03_correctPost3() {
		logger.info("Test correct post3.");
		String body = "{\"text\":\"correct Post 3 #hta #tsa\"}";
		String content = "POST /tweets HTTP/1.1\r\nContent-Length:"
				+ body.length()
				+ "\r\nContent-Type: application/json;charset=utf-8\r\n\r\n"
				+ body;
		HTTPResponseContent response = c.sendRequest(content);
		logger.info("Response from correct post3: \n{}.",
				response.getResponseString());
		Assert.assertEquals(response, HTTPResponse.response_201());
	}

	@Test
	public void test04_correctGetFromBackEnd() {
		logger.info("Test correct get from backend.");
		String body = "";
		String content = "GET /tweets?q=ht HTTP/1.1\r\nContent-Length:"
				+ body.length()
				+ "\r\nContent-Type: application/json;charset=utf-8\r\n\r\n"
				+ body;
		HTTPResponseContent response = c.sendRequest(content);
		logger.info("Response from correct get from backend: \n{}.",
				response.getResponseString());
		// do not assert body, because if we run test several times, the result
		// will be different.
		// because the we can store same tweets in the Backend.
		Assert.assertEquals(response.getResponseHeader().getCode(),
				HTTPResponse.response_200().getResponseHeader().getCode());
	}
	@Test
	public void test05_correctGetFromBackEnd() {
		logger.info("Test correct get from backend.");
		String body = "";
		String content = "GET /tweets?q=ht HTTP/1.1\r\nContent-Length:"
				+ body.length()
				+ "\r\nContent-Type: application/json;charset=utf-8\r\n\r\n"
				+ body;
		HTTPResponseContent response = c.sendRequest(content);
		logger.info("Response from correct get from backend: \n{}.",
				response.getResponseString());
		// do not assert body, because if we run test several times, the result
		// will be different.
		// because the we can store same tweets in the Backend.
		Assert.assertEquals(response.getResponseHeader().getCode(),
				HTTPResponse.response_200().getResponseHeader().getCode());
	}
	@Test
	public void test06_correctGetFromBackEnd() {
		logger.info("Test correct get from backend.");
		String body = "";
		String content = "GET /tweets?q=ht HTTP/1.1\r\nContent-Length:"
				+ body.length()
				+ "\r\nContent-Type: application/json;charset=utf-8\r\n\r\n"
				+ body;
		HTTPResponseContent response = c.sendRequest(content);
		logger.info("Response from correct get from backend: \n{}.",
				response.getResponseString());
		// do not assert body, because if we run test several times, the result
		// will be different.
		// because the we can store same tweets in the Backend.
		Assert.assertEquals(response.getResponseHeader().getCode(),
				HTTPResponse.response_200().getResponseHeader().getCode());
	}

	
}
