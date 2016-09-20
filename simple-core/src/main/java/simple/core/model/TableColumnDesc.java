package simple.core.model;

import java.util.ArrayList;
import java.util.List;

public class TableColumnDesc implements Comparable<TableColumnDesc> {

	private transient Class<?> type;

	private String title;

	private transient String format;

	private String name;

	private boolean show = true;

	private transient int sort;

	private transient String handler;

	private Boolean sortable;

	private ImageGalleryTableColumnDesc imageGallery;

	private List<String> booleanValue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public ImageGalleryTableColumnDesc getImageGallery() {
		return imageGallery;
	}

	public void setImageGallery(ImageGalleryTableColumnDesc imageGallery) {
		this.imageGallery = imageGallery;
	}

	public List<String> getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(List<String> booleanValue) {
		this.booleanValue = booleanValue;
	}

	public void addBooleanValue(String value) {
		if (null == this.booleanValue) {
			this.booleanValue = new ArrayList<String>();
		}
		this.booleanValue.add(value);
	}

	public Boolean getSortable() {
		return sortable;
	}

	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
	}

	@Override
	public int compareTo(TableColumnDesc o) {
		return this.sort - o.sort;
	}

	public boolean hasHandler() {
		return null != this.handler && !"".equals(this.handler);
	}
}
