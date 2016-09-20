package simple.base;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;

import simple.config.annotation.BooleanValue;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.core.jpa.convert.BooleanToStringConverter;

@MappedSuperclass
public abstract class BaseValiditySupportModel extends BaseModel  implements Serializable{

	private static final long serialVersionUID = 717953865648421969L;

	@Column(name = "IS_VALID", columnDefinition = "CHAR(1)")
	@RepresentationField(title = "是否有效", sort = 99995, view = RepresentationFieldType.BOOLEAN, defaultVal = "true")
	@BooleanValue({ "有效", "无效" })
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean isValid;

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

}
