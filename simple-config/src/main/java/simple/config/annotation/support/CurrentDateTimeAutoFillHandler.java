package simple.config.annotation.support;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import simple.config.annotation.util.CalendarUtils;

public class CurrentDateTimeAutoFillHandler implements AutoFillHandler {

	@Override
	public void handle(Field field, Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		field.set(target, CalendarUtils.getCurrentDateTime());
	}
}
