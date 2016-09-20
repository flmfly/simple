package simple.base.web.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.filter.OncePerRequestFilter;

import simple.base.Globe;
import simple.core.model.ResponseObject;
import simple.core.model.Status;

import com.google.gson.Gson;

public class SecurityFilter extends OncePerRequestFilter {

	private static final Gson GSON = new Gson();

	private static Set<String> ignoreUri = new HashSet<String>();

	protected void initFilterBean() throws ServletException {
		String[] ignore = super.getFilterConfig().getInitParameter("ignore")
				.split(",");
		for (int i = 0; i < ignore.length; i++) {
			String uri = ignore[i];
			ignoreUri.add(uri);
		}
	}

	private boolean isIgnoreUri(String reqUri) {
		if (reqUri.indexOf("/rest/api/") == -1
				&& reqUri.indexOf("/func/api/") == -1) {
			return true;
		}

		boolean rtn = false;
		for (String uri : ignoreUri) {
			if (reqUri.indexOf(uri) != -1) {
				rtn = true;
				break;
			}
		}
		return rtn;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		boolean login = true;

		ResponseObject ro = null;
		if (!this.isIgnoreUri(request.getRequestURI())) {
			ro = new ResponseObject();
			HttpSession session = request.getSession(false);

			if (session == null || session.isNew()) {
				login = false;
				ro.addInfo("登录超时，请重新登录！");
			} else if (null == request.getSession().getAttribute(
					Globe.SESSION_USER_KEY)) {
				login = false;
				ro.addInfo("您还没有登陆！");
			}
		}

		if (!login) {
			ro.setStatus(new Status(401, ""));
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json; charset=utf-8");
			PrintWriter out = null;
			try {
				out = response.getWriter();
				out.append(GSON.toJson(ro));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
			return;
		}

		boolean right = true;

		String domainName = null;
		if (!this.isIgnoreUri(request.getRequestURI())) {
			@SuppressWarnings("unchecked")
			Set<String> rightSet = (Set<String>) request.getSession()
					.getAttribute(Globe.SESSION_RIGHT_KEY);
			if (null != rightSet) {
				StringTokenizer st = new StringTokenizer(
						request.getRequestURI(), "/");
				String token = "";
				while (st.hasMoreTokens() && !"api".equals(token)) {
					token = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					domainName = st.nextToken();
					right = rightSet.contains(domainName);
				}
			} else {
				right = false;
			}
		}

		if (!right) {
			ro.setStatus(new Status(404, "您没有访问" + domainName + "的权限！"));
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json; charset=utf-8");
			PrintWriter out = null;
			try {
				out = response.getWriter();
				out.append(GSON.toJson(ro));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
			return;
		}

		filterChain.doFilter(request, response);
	}
}
