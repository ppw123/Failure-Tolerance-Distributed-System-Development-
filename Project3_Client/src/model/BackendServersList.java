package model;

import java.util.LinkedList;
import java.util.List;

public class BackendServersList implements MessageData {

	private List<BackEndServerInfo> secondaryAp = new LinkedList<BackEndServerInfo>();
	private ServerAddressAndPort primaryAp;

	public List<BackEndServerInfo> getSecondaryAp() {
		return secondaryAp;
	}

	public void setSecondaryAp(List<BackEndServerInfo> secondaryAp) {
		this.secondaryAp = secondaryAp;
	}

	public ServerAddressAndPort getPrimaryAp() {
		return primaryAp;
	}

	public void setPrimaryAp(ServerAddressAndPort primaryAp) {
		this.primaryAp = primaryAp;
	}

	@Override
	public String getMessage() {
		String secondaryString = "";
		for (BackEndServerInfo sp : secondaryAp) {
			secondaryString += sp.getId() + ":" + sp.getSp().getHost() + ":" + sp.getSp().getPort() + "*";
		}
		return primaryAp.getHost() + ":" + primaryAp.getPort() + "*"
				+ secondaryString;
	}

	public BackendServersList(String string) {
		String[] s = string.split("\\*");
		if (s.length > 0) {
			String[] primary = s[0].split(":");
			this.primaryAp = new ServerAddressAndPort(primary[0],
					Integer.parseInt(primary[1]));

			for (int i = 1; i < s.length; i++) {
				if (s[i].split(":").length > 2) {
				this.secondaryAp.add(new BackEndServerInfo(
						Integer.parseInt(s[i].split(":")[0]),
						s[i].split(":")[1],
						Integer.parseInt(s[i].split(":")[2])));
				} else {
					this.secondaryAp.add(new BackEndServerInfo(
							Integer.parseInt(s[i].split(":")[0]),
							s[i].split(":")[1],
							0));
				}

			}
		} else {

		}
	}

	public BackendServersList() {
	}

	public BackendServersList(List<BackEndServerInfo> secondaryAp,
			ServerAddressAndPort primaryAp) {
		this.secondaryAp = secondaryAp;
		this.primaryAp = primaryAp;
	}
	
	
}
