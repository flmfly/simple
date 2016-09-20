package simple.core.model.annotation;

import simple.core.model.FieldValidation;
import simple.core.model.RepresentationType;

public class SearchFieldDesc implements Comparable<SearchFieldDesc> {

	private String name;

	private String title;

	private transient int sort;

	private Boolean isRange;

	private Boolean canFuzzy;

	private RepresentationType type;

	private FieldValidation validation;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean getIsRange() {
		return isRange;
	}

	public void setIsRange(Boolean isRange) {
		this.isRange = isRange;
	}

	public RepresentationType getType() {
		return type;
	}

	public void setType(RepresentationType type) {
		this.type = type;
	}

	public Boolean getCanFuzzy() {
		return canFuzzy;
	}

	public void setCanFuzzy(Boolean canFuzzy) {
		this.canFuzzy = canFuzzy;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public FieldValidation getValidation() {
		return validation;
	}

	public void setValidation(FieldValidation validation) {
		this.validation = validation;
	}

	@Override
	public int compareTo(SearchFieldDesc o) {
		return this.sort - o.sort;
	}
}
