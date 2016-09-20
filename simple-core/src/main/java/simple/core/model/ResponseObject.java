package simple.core.model;

import java.util.ArrayList;
import java.util.List;

public class ResponseObject {

	private String JSESSIONID;

	private Status status;

	private List<String> infos;

	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void addInfo(String info) {
		if (null == infos) {
			this.infos = new ArrayList<String>();
		}

		this.infos.add(info);
	}

	public List<String> getInfos() {
		return infos;
	}

	public void setInfos(List<String> infos) {
		this.infos = infos;
	}

	public void setInfo(List<String> infos) {
		this.infos = infos;
	}

	public String getJSESSIONID() {
		return JSESSIONID;
	}

	public void setJSESSIONID(String jSESSIONID) {
		JSESSIONID = jSESSIONID;
	}

}
