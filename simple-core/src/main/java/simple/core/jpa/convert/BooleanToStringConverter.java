package simple.core.jpa.convert;

import javax.persistence.AttributeConverter;

public class BooleanToStringConverter implements
		AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean attribute) {
		if (null == attribute) {
			return null;
		}
		return attribute.booleanValue() ? "1" : "0";
	}

	@Override
	public Boolean convertToEntityAttribute(String dbData) {
		if (null == dbData) {
			return null;
		} else if ("1".equals(dbData)) {
			return Boolean.TRUE;
		} else if ("0".equals(dbData)) {
			return Boolean.FALSE;
		}
		return null;
	}

}
