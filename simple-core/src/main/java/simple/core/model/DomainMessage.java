package simple.core.model;

import simple.core.util.MD5Utils;

public class DomainMessage {

	public static final int SUCCESS_CODE = 200;

	public static final int FAILED_CODE = 500;

	private int code;

	private String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = MD5Utils.convertMD5(message);
	}

}
