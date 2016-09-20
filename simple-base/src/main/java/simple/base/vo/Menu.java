package simple.base.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu implements Comparable<Menu>,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6834356692526147486L;

	private String name;

	private String code;

	private String parentCode;

	private String css = "";

	private String uri = "";

	private boolean hasChildren;

	private List<Menu> children;

	private transient int sort = Integer.MAX_VALUE;

	private transient Long pid;

	private transient Boolean hidden;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public List<Menu> getChildren() {
		return children;
	}

	public void setChildren(List<Menu> children) {
		this.children = children;
	}

	public void addChild(Menu child) {
		if (null == children) {
			children = new ArrayList<Menu>();
		}
		this.children.add(child);
		Collections.sort(children);
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public int compareTo(Menu o) {
		return this.sort - o.sort;
	}
}
