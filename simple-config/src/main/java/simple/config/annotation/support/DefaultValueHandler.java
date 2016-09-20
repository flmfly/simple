package simple.config.annotation.support;

import javax.servlet.http.HttpServletRequest;

public interface DefaultValueHandler {

	public Object handle(Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException;
}
