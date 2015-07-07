package model;

//For each back end server, it has its own informations: id & host & port.
public class BackEndServerInfo {
	public BackEndServerInfo(Integer id, String host, Integer port) {
		this.id = id;
		this.sp = new ServerAddressAndPort(host, port);
	}
	private Integer id;
	private ServerAddressAndPort sp;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public ServerAddressAndPort getSp() {
		return sp;
	}
	public void setSp(ServerAddressAndPort sp) {
		this.sp = sp;
	}
	public BackEndServerInfo() {
	}
	@Override
	public String toString() {
		return "BackEndServerInfo [id=" + id + ", sp=" + sp + "]";
	}
	public BackEndServerInfo(Integer id, ServerAddressAndPort sp) {
		this.id = id;
		this.sp = sp;
	}
	
}
