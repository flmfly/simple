package simple.base.annotation.support;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import simple.base.Globe;
import simple.base.model.BaseUser;
import simple.config.annotation.support.AutoFillHandler;

public class LoginUserAutoFillHandler implements AutoFillHandler {

	private static final Log LOG = LogFactory
			.getLog(LoginUserAutoFillHandler.class);

	@Override
	public void handle(Field field, Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException {

		if(request==null){
			return;
		}
		field.setAccessible(true);

		BaseUser user = (BaseUser) request.getSession().getAttribute(
				Globe.SESSION_USER_KEY);

		if (field.getType().isInstance(user)) {
			field.set(target, user);
		} else {
			LOG.warn("No user found in current session!");
		}

	}
}
