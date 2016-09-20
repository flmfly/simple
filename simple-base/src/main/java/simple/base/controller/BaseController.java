package simple.base.controller;

//import java.util.Set;
//
//import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import simple.base.Globe;
import simple.base.model.BaseMenu;
import simple.base.model.BaseUser;
import simple.base.service.MessageService;
import simple.base.service.RightService;
import simple.base.service.UserService;
import simple.base.vo.Menu;
import simple.core.Constants;
import simple.core.model.ResponseObject;
import simple.core.model.Status;

/**
 * The Class CommonRestController.
 * 
 * @author Jeffrey
 */
@RestController
@RequestMapping(Constants.REST_API_PREFIX)
public class BaseController implements Constants, Globe {

	/** The menu service. */
	@Autowired
	private RightService rightService;

	/** The menu service. */
	@Autowired
	private UserService userService;

	/** The menu service. */
	@Autowired
	private MessageService messageService;

	/** The Constant logger. */
	static final Logger logger = Logger.getLogger(BaseController.class);

	@RequestMapping(value = "/base/menu", method = RequestMethod.GET)
	public List<Menu> getMenu(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		List<Menu> menuList = (List<Menu>) request.getSession().getAttribute(
				Globe.SESSION_MENU_KEY);
		if (null == menuList) {
			return Collections.<Menu> emptyList();
		} else {
			return menuList;
		}
	}

	@RequestMapping(value = "/base/loginuser", method = RequestMethod.GET)
	public ResponseObject getLoginUser(HttpServletRequest request) {
		ResponseObject ro = new ResponseObject();

		Object user = request.getSession().getAttribute(Globe.SESSION_USER_KEY);
		// BaseUser user = new BaseUser();
		// user.setName("Jeffrey");

		if (null != user) {
			ro.setStatus(Status.STATUS_200);
			ro.setData(user);
		} else {
			ro.setStatus(new Status(401, ""));
		}

		return ro;
	}

	@RequestMapping(value = "/base/logout", method = RequestMethod.GET)
	public ResponseObject logout(HttpServletRequest request) {
		ResponseObject ro = new ResponseObject();
		request.getSession().invalidate();
		ro.setStatus(Status.STATUS_200);
		return ro;
	}

	@RequestMapping(value = "/base/login", method = RequestMethod.POST)
	public ResponseObject login(@RequestBody String json,
			HttpServletRequest request) {
		ResponseObject ro = this.userService.login(json, request);

		BaseUser user = (BaseUser) request.getSession().getAttribute(
				Globe.SESSION_USER_KEY);
		if (null != user) {
			Collection<BaseMenu> menuList = this.rightService.getMenu(user);
			request.getSession().setAttribute(Globe.SESSION_MENU_KEY,
					this.rightService.getMenuTree(menuList));
			request.getSession().setAttribute(Globe.SESSION_RIGHT_KEY,
					this.rightService.getDomainRight(menuList));
		}

		return ro;
	}

	@RequestMapping(value = "/base/user/findpassword", method = RequestMethod.POST)
	public ResponseObject findPassword(@RequestBody String json) {
		ResponseObject ro = new ResponseObject();
		ro.setStatus(Status.STATUS_200);
		ro.addInfo("密码已经发往您的邮箱！");
		return ro;
	}

	@RequestMapping(value = "/base/changepassword", method = RequestMethod.POST)
	public ResponseObject resetPassword(@RequestBody String json,HttpServletRequest request) {
		ResponseObject ro = new ResponseObject();
		this.userService.changePassword(json, ro,request);
		return ro;
	}

	/**
	 * 注册.
	 * 
	 * @param json
	 * @return
	 */
	@RequestMapping(value = "/base/register", method = RequestMethod.POST)
	public ResponseObject register(@RequestBody String json,HttpServletRequest request) {
		ResponseObject ro = new ResponseObject();
		this.userService.operateRegister(json, ro,request);
		return ro;
	}

	/**
	 * 找回密码.
	 * 
	 * @param json
	 * @return
	 */
	@RequestMapping(value = "/base/retrievePassword", method = RequestMethod.POST)
	public ResponseObject retrievePassword(@RequestBody String json) {
		ResponseObject ro = new ResponseObject();
		this.userService.retrievePassword(json, ro);
		return ro;
	}

	@RequestMapping(value = "/base/message", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String getMessages(@RequestBody String maxId,
			HttpServletRequest request) {
		ResponseObject ro = new ResponseObject();
		BaseUser user = (BaseUser) request.getSession().getAttribute(
				Globe.SESSION_USER_KEY);
		try {
			ro.setData(this.messageService.getMessagesByUser(user.getAccount(),
					maxId));
			// "admin", maxId));
			ro.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			ro.setStatus(new Status(500, e.getMessage()));
		}

		return messageService.toJsonStr(ro);
	}

	@RequestMapping(value = "/base/message/setread", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseObject setRead(@RequestBody String ids,
			HttpServletRequest request) {
		ResponseObject ro = new ResponseObject();
		BaseUser user = (BaseUser) request.getSession().getAttribute(
				Globe.SESSION_USER_KEY);
		try {
			this.messageService.setRead(user.getAccount(), ids);
			// this.messageService.setRead("admin", ids);
			ro.setStatus(Status.STATUS_200);
		} catch (Exception e) {
			ro.setStatus(new Status(500, e.getMessage()));
		}

		return ro;
	}

}