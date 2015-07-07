package model;

public class Message {
	public enum Method {
		ADD, DELET, SET_PRIMARY, BACKEND_LIST, HELLO, ID, BULLY, SET_VERSION, GET_VERSION, GET_FRONT_END;
	}
	private Method method;
	public Message() {
	}
	private MessageData data;

	public Message(String s) throws MessageParseException {
		String[] array = s.split("\\|");
		if (array.length > 1) {
			this.method = Method.valueOf(array[1]);
			if (method.equals(Method.ADD) || method.equals(Method.DELET) || method.equals(Method.SET_PRIMARY)) {
				this.data = new ServerChanges(array[2]);
			} else if (method.equals(Method.BACKEND_LIST)){
				this.data = new BackendServersList(array[2]);
			} else if (method.equals(Method.ID) || method.equals(Method.SET_VERSION)) {
				this.data = new IdMessageData(Integer.parseInt(array[2]));
			} else {
				this.data = new EmptyMessageData();
			}
		} else {
			throw new MessageParseException("Not method found.");
		}
	}

	public String getMessage(){
		return "MSG|" + method + "|" + data.getMessage();
	}

	public MessageData getData() {
		return data;
	}

	public void setData(MessageData data) {
		this.data = data;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}