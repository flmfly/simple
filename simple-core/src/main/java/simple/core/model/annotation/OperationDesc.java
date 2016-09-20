package simple.core.model.annotation;

import java.util.ArrayList;
import java.util.List;

import simple.config.annotation.OperationParameter;
import simple.core.model.FormField;

public class OperationDesc {
	private String name;

	private String code;

	private boolean multi;

	private String target;

	private String iconStyle;

	private List<FormField> parameters;

	private transient String disableDetectHandler;

	private transient String handler;

	private transient List<OperationParameter> parameterAnnotations;

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

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getIconStyle() {
		return iconStyle;
	}

	public void setIconStyle(String iconStyle) {
		this.iconStyle = iconStyle;
	}

	public String getDisableDetectHandler() {
		return disableDetectHandler;
	}

	public void setDisableDetectHandler(String disableDetectHandler) {
		this.disableDetectHandler = disableDetectHandler;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public List<FormField> getParameters() {
		return parameters;
	}

	public void addParameter(FormField parameter) {
		if (null == parameters) {
			parameters = new ArrayList<FormField>();
		}
		parameters.add(parameter);
	}

	public List<OperationParameter> getParameterAnnotations() {
		return parameterAnnotations;
	}

	public void addParameterAnnotation(OperationParameter parameter) {
		if (null == parameterAnnotations) {
			parameterAnnotations = new ArrayList<OperationParameter>();
		}
		parameterAnnotations.add(parameter);
	}

}
