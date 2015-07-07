package model;


public class IdMessageData implements MessageData{
	Integer id;
	
	public IdMessageData(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getMessage() {
		return id.toString();
	}

}
