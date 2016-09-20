package simple.core.model;

public class FormField implements Comparable<FormField> {

	private String name;

	private String title;

	private transient int sort;

	private FormGroupDesc group;

	private RepresentationType type;

	private Object defaultVal;

	private boolean required = false;

	private FieldValidation validation;

	private String refField;

	private String refPath;

	private Boolean isLongitude;

	private Boolean isLatitude;

	private Boolean isMapInfo;

	private Boolean isMapInfoTitle;

	private String mapCityProperty;

	private Integer mapInfoSort;

	private String mapAddress;

	private Boolean visable;

	private Boolean hasChangedListener;

	private transient boolean importIgnore;

	private transient boolean subImport=false;

	public String getName() {
		return name;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
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

	public FormGroupDesc getGroup() {
		return group;
	}

	public void setGroup(FormGroupDesc group) {
		this.group = group;
	}

	public RepresentationType getType() {
		return type;
	}

	public void setType(RepresentationType type) {
		this.type = type;
	}

	public Object getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(Object defaultVal) {
		this.defaultVal = defaultVal;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public FieldValidation getValidation() {
		return validation;
	}

	public void setValidation(FieldValidation validation) {
		this.validation = validation;
	}

	public String getRefField() {
		return refField;
	}

	public void setRefField(String refField) {
		this.refField = refField;
	}

	public String getRefPath() {
		return refPath;
	}

	public void setRefPath(String refPath) {
		this.refPath = refPath;
	}

	public boolean isHidden() {
		return RepresentationType.HIDDEN.equals(this.getType().getView());
	}

	public Boolean getIsLongitude() {
		return isLongitude;
	}

	public void setIsLongitude(Boolean isLongitude) {
		this.isLongitude = isLongitude;
	}

	public Boolean getIsLatitude() {
		return isLatitude;
	}

	public void setIsLatitude(Boolean isLatitude) {
		this.isLatitude = isLatitude;
	}

	public Boolean getIsMapInfo() {
		return isMapInfo;
	}

	public void setIsMapInfo(Boolean isMapInfo) {
		this.isMapInfo = isMapInfo;
	}

	public Integer getMapInfoSort() {
		return mapInfoSort;
	}

	public void setMapInfoSort(Integer mapInfoSort) {
		this.mapInfoSort = mapInfoSort;
	}

	public Boolean getIsMapInfoTitle() {
		return isMapInfoTitle;
	}

	public void setIsMapInfoTitle(Boolean isMapInfoTitle) {
		this.isMapInfoTitle = isMapInfoTitle;
	}

	public String getMapCityProperty() {
		return mapCityProperty;
	}

	public void setMapCityProperty(String mapCityProperty) {
		this.mapCityProperty = mapCityProperty;
	}

	public String getMapAddress() {
		return mapAddress;
	}

	public void setMapAddress(String mapAddress) {
		this.mapAddress = mapAddress;
	}

	public Boolean getVisable() {
		return visable;
	}

	public void setVisable(Boolean visable) {
		this.visable = visable;
	}

	public boolean getImportIgnore() {
		return importIgnore;
	}

	public void setImportIgnore(boolean importIgnore) {
		this.importIgnore = importIgnore;
	}

	public boolean isSubImport() {
		return subImport;
	}

	public void setSubImport(boolean subImport) {
		this.subImport = subImport;
	}

	public Boolean getHasChangedListener() {
		return hasChangedListener;
	}

	public void setHasChangedListener(Boolean hasChangedListener) {
		this.hasChangedListener = hasChangedListener;
	}

	@Override
	public int compareTo(FormField o) {
		return this.sort - o.sort;
	}

	// memory leak issue
	// public FormField cloneMe() {
	// FormField cloned = new FormField();
	// cloned.setTitle(this.getTitle());
	// cloned.setDefaultVal(this.getDefaultVal());
	// cloned.setName(this.getName());
	// cloned.setSort(this.getSort());
	// cloned.setType(this.type.cloneMe());
	// cloned.setRequired(this.required);
	// cloned.setValidation(this.validation);
	// cloned.setRefField(this.refField);
	// cloned.setRefPath(this.refPath);
	// cloned.setIsLongitude(this.isLongitude);
	// cloned.setIsLatitude(this.isLatitude);
	// cloned.setIsMapInfo(this.isMapInfo);
	// cloned.setMapInfoSort(this.mapInfoSort);
	// cloned.setIsMapInfoTitle(this.isMapInfoTitle);
	// cloned.setMapCityProperty(this.mapCityProperty);
	// cloned.setMapAddress(this.mapAddress);
	// return cloned;
	// }

}
