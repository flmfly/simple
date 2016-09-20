package simple.core.model;

public class Option {

	private Object id;

	private String name;

	public Option(Object id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
