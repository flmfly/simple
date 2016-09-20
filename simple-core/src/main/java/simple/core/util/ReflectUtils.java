package simple.core.util;

import java.lang.reflect.Field;

import org.apache.commons.lang.ArrayUtils;

public final class ReflectUtils {

	private ReflectUtils() {

	}

	public static Field getField(Class<?> target, String fieldName)
			throws NoSuchFieldException, SecurityException {
		if (target == null || target.equals(Object.class)) {
			throw new NoSuchFieldException("No Such Field: " + fieldName);
		}
		Field field = null;
		try {
			field = target.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			field = getField(target.getSuperclass(), fieldName);
		}
		return field;
	}

	public static Field[] getDeclaredFields(Class<?> target) {
		if (target.getSuperclass().equals(Object.class)) {
			return target.getDeclaredFields();
		} else {
			return (Field[]) ArrayUtils.addAll(target.getDeclaredFields(),
					getDeclaredFields(target.getSuperclass()));
		}
	}
}
