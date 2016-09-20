package simple.core.orm.hibernate;

public class FieldDesc {

	public static enum Type {
		REF, STRING, LONG, DOUBLE, BOOLEAN, INTEGER, FLOAT, DATE
	}

	public Type type;

	public Class<?> refClass;

	public String name;

	public Integer length;

	public String columnName;

	public String columnDefinition;

	public Class<?> converter;
}
