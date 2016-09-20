package simple.base.annotation.support;

import javax.servlet.http.HttpServletRequest;

import simple.base.Globe;
import simple.base.model.BaseUser;
import simple.config.annotation.support.DefaultValueHandler;

public class LoginUserDefaultValueHandler implements DefaultValueHandler {

	@Override
	public Object handle(Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException {
		return (BaseUser) request.getSession().getAttribute(
				Globe.SESSION_USER_KEY);
	}
}
