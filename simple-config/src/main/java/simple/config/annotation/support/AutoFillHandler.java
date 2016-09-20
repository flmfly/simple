package simple.config.annotation.support;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

public interface AutoFillHandler {

	public void handle(Field field, Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException;

}
