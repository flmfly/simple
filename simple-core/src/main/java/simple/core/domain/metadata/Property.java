package simple.core.domain.metadata;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetProperty;

@ObjectCreate(pattern = "domain/properties/property")
public class Property {
	@SetProperty(pattern = "domain/properties/property")
	public String code;

	@SetProperty(pattern = "domain/properties/property")
	public String title;

	@SetProperty(pattern = "domain/properties/property")
	public Integer sort;

	@SetProperty(pattern = "domain/properties/property/type", attributeName = "code")
	public String type;

	@BeanPropertySetter(pattern = "domain/properties/property/type/ref-domain")
	public String refDomain;

	@BeanPropertySetter(pattern = "domain/properties/property/type/length")
	public Integer length;

	@BeanPropertySetter(pattern = "domain/properties/property/type/precision")
	public Integer precision;

	@BeanPropertySetter(pattern = "domain/properties/property/type/scale")
	public Integer scale;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRefDomain() {
		return refDomain;
	}

	public void setRefDomain(String refDomain) {
		this.refDomain = refDomain;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

}
