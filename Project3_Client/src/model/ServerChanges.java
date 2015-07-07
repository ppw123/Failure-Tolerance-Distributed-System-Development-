package model;

public class ServerChanges implements MessageData {


	public enum ServerType {
		FE, BE, LB;
	}

	private ServerType serverType;
	private ServerAddressAndPort serverAddressAndPort;
	int id;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ServerType getServerType() {
		return serverType;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	

	public ServerAddressAndPort getServerAddressAndPort() {
		return serverAddressAndPort;
	}

	public void setServerAddressAndPort(
			ServerAddressAndPort serverAddressAndPort) {
		this.serverAddressAndPort = serverAddressAndPort;
	}
	
	@Override
	public String getMessage() {
		return serverType.toString() + "*"
				+ serverAddressAndPort.getHost().toString() + "*"
				+ serverAddressAndPort.getPort() + "*" + id;
	}

	public ServerChanges(String string) throws MessageParseException {
		String[] s = string.split("\\*");
		if (s.length != 4) {
			throw new MessageParseException("Message lacking!");
		} else {
			this.serverType = ServerType.valueOf(s[0]);
			this.serverAddressAndPort = new ServerAddressAndPort(s[1],
					Integer.parseInt(s[2]));
			this.id = Integer.parseInt(s[3]);
		}
	}

	public ServerChanges() {
	}

	public ServerChanges(ServerType serverType, String host, int port, int id) {
		this.serverType = serverType;
		this.serverAddressAndPort = new ServerAddressAndPort(host, port);
		this.id = id;
	}

}
