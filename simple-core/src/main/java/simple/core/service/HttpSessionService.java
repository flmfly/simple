package simple.core.service;

import javax.servlet.http.HttpSession;

public abstract class HttpSessionService {
	protected ThreadLocal<HttpSession> session = new ThreadLocal<HttpSession>();

	public void setSession(HttpSession session) {
		this.session.set(session);
	}

	public HttpSession getSession(){
		return this.session.get();
	}
	
	public Object getAttribute(String name) {
		return this.session.get().getAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		this.session.get().setAttribute(name, value);
	}

	public abstract Object getLoginUser();
	
	public abstract String getLoginUserAccount();
}
