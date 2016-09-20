package simple.core.model.webservice;

import java.util.List;

public class Request {

	protected String func;

	protected Page page;

	protected Object query;

	protected Boolean delta;

	protected List<Object> data;

	protected Integer operation;

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Object getQuery() {
		return query;
	}

	public void setQuery(Object query) {
		this.query = query;
	}

	public Boolean getDelta() {
		return delta;
	}

	public void setDelta(Boolean delta) {
		this.delta = delta;
	}

	public List<Object> getData() {
		return data;
	}

	public void setData(List<Object> data) {
		this.data = data;
	}

	public Integer getOperation() {
		return operation;
	}

	public void setOperation(Integer operation) {
		this.operation = operation;
	}

}
