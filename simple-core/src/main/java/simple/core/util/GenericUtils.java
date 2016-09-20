package simple.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public final class GenericUtils {

	private GenericUtils() {

	}

	public static Class<?> getCollectionElementGeneric(Field field) {
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		return (Class<?>) pt.getActualTypeArguments()[0];
	}

}
