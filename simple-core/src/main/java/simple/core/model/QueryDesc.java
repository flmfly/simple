package simple.core.model;

import java.util.List;

public class QueryDesc extends ResponseObject {

	private List<FormField> params;

	private List<String> columns;

	public List<FormField> getParams() {
		return params;
	}

	public void setParams(List<FormField> params) {
		this.params = params;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

}
