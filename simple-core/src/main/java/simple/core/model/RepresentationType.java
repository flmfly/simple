package simple.core.model;

import java.util.ArrayList;
import java.util.List;

public class RepresentationType {
	public static final String INPUT = "input";
	public static final String CHECKBOX = "checkbox";
	public static final String RADIO = "radio";
	public static final String SELECT = "select";
	public static final String TEXTAREA = "textarea";
	public static final String HIDDEN = "hidden";
	public static final String REFERENCE = "reference";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String DATETIME = "datetime";
	public static final String TAB = "tab";
	public static final String BOOLEAN = "boolean";
	public static final String TAGS = "tags";
	public static final String ENUM = "enum";
	public static final String HTML_EDITOR = "html_editor";

	public static final String QRCODE = "qrcode";

	private String view = INPUT;

	private boolean disabled = false;

	private List<Option> options;

	private String refName;

	private String refId;

	private String refLabel;

	private List<FormField> refFields = new ArrayList<FormField>();

	private String dict;

	private String depend;

	private String dependAssociateField;

	private String refDomainName;

	private String tabType;

	private boolean editable = true;

	private String refType;

	private String placeholder;

	private AttachmentDesc attachment;

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void addOption(Option option) {
		if (null == this.options) {
			this.options = new ArrayList<Option>();
		}
		this.options.add(option);
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getRefLabel() {
		return refLabel;
	}

	public void setRefLabel(String refLabel) {
		this.refLabel = refLabel;
	}

	public String getDict() {
		return dict;
	}

	public void setDict(String dict) {
		this.dict = dict;
	}

	public String getDepend() {
		return depend;
	}

	public void setDepend(String depend) {
		this.depend = depend;
	}

	public String getDependAssociateField() {
		return dependAssociateField;
	}

	public void setDependAssociateField(String dependAssociateField) {
		this.dependAssociateField = dependAssociateField;
	}

	public String getRefDomainName() {
		return refDomainName;
	}

	public void setRefDomainName(String refDomainName) {
		this.refDomainName = refDomainName;
	}

	public String getTabType() {
		return tabType;
	}

	public void setTabType(String tabType) {
		this.tabType = tabType;
	}

	public boolean getEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public List<FormField> getRefFields() {
		return refFields;
	}

	// private void setRefFields(List<FormField> refFields) {
	// this.refFields = refFields;
	// }

	public void addRefField(FormField refField) {
		this.refFields.add(refField);
	}

	public String getRefType() {
		return refType;
	}

	public void setRefType(String refType) {
		this.refType = refType;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public AttachmentDesc getAttachment() {
		return attachment;
	}

	public void setAttachment(AttachmentDesc attachment) {
		this.attachment = attachment;
	}

	// public RepresentationType cloneMe() {
	// RepresentationType cloned = new RepresentationType();
	// cloned.setDict(this.dict);
	// cloned.setView(this.view);
	// cloned.setDisabled(this.disabled);
	// cloned.setRefName(this.refName);
	// cloned.setRefId(this.refId);
	// cloned.setRefLabel(this.refLabel);
	// cloned.setRefFields(this.refFields);
	// cloned.setDepend(this.depend);
	// cloned.setRefDomainName(this.refDomainName);
	// cloned.setDependAssociateField(this.dependAssociateField);
	// cloned.setTabType(this.tabType);
	// cloned.setEditable(this.editable);
	// cloned.setRefType(this.refType);
	// if (BOOLEAN.equals(this.view)) {
	// cloned.setOptions(this.options);
	// }
	//
	// return cloned;
	// }

}
