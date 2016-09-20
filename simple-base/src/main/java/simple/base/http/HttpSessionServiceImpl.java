package simple.base.http;

import simple.base.Globe;
import simple.base.model.BaseUser;
import simple.core.service.HttpSessionService;

public class HttpSessionServiceImpl extends HttpSessionService {

	@Override
	public Object getLoginUser() {
		// 当为导入,job执行的时候HttpSession是不存在的
		if (session.get() == null) {
			return null;
		}
		return session.get().getAttribute(Globe.SESSION_USER_KEY);
	}

	@Override
	public String getLoginUserAccount() {
		// TODO Auto-generated method stub
		BaseUser baseUser = (BaseUser) getLoginUser();
		return baseUser != null ? baseUser.getAccount() : null;
	}

}
