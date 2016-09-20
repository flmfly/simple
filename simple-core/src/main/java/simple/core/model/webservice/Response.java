package simple.core.model.webservice;

import java.util.List;

import simple.core.model.Status;

public class Response extends Request {

	public Response() {

	}

	public Response(Request request) {
		super.func = request.func;
		super.page = request.page;
		super.query = request.query;
		super.delta = request.delta;
		super.data = request.data;
		super.operation = request.operation;
	}

	private Status status;

	private List<List<String>> validation;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<List<String>> getValidation() {
		return validation;
	}

	public void setValidation(List<List<String>> validation) {
		this.validation = validation;
	}

}
