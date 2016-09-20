package simple.config.annotation.support;

import javax.servlet.http.HttpServletRequest;

import simple.config.annotation.util.CalendarUtils;

public class DateDefaultValueHandler implements DefaultValueHandler {

	@Override
	public Object handle(Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException {
		return CalendarUtils.getCurrentDate();
	}
}
