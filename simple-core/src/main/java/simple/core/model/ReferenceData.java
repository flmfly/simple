package simple.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Base on jsTree data structure. http://www.jstree.com/
 * 
 * @author Jeffrey
 *
 */
public class ReferenceData {

	private Long id;

	private String text;

	private transient Long pid;

	private List<ReferenceData> children;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<ReferenceData> getChildren() {
		return children;
	}

	public void setChildren(List<ReferenceData> children) {
		this.children = children;
	}

	public void addChild(ReferenceData child) {
		if (null == children) {
			children = new ArrayList<ReferenceData>();
		}
		children.add(child);
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

}
