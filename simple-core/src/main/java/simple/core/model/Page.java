package simple.core.model;

import java.util.List;

public class Page {

	private int pageNumber;

	private int pageSize;

	private long total;

	private Object example;

	private String sort;

	private List<Object> list;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Object getExample() {
		return example;
	}

	public void setExample(Object example) {
		this.example = example;
	}

	public String getSort() {
		if (null == sort || "".equals(sort)) {
			return "id";
		}
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public List<Object> getList() {
		return list;
	}

	public void setList(List<Object> list) {
		this.list = list;
	}

}
