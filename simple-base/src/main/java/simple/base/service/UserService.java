package simple.base.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import simple.base.Globe;
import simple.base.model.BaseDictItem;
import simple.base.model.BaseUser;
import simple.base.model.BaseUserSms;
import simple.base.verifycode.RandomValidateCode;
import simple.base.vo.User;
import simple.core.model.ResponseObject;
import simple.core.model.Status;
import simple.core.service.BaseService;
import simple.core.util.MD5Utils;

@Service
public class UserService extends BaseService {

	Logger logger = Logger.getLogger(UserService.class);

	@Autowired
	private SmsService smsService;

	@Autowired
	private DictItemService dictItemService;

	@SuppressWarnings("unchecked")
	public BaseUser getUser(User user) {
		Criteria criteria = super.hibernateBaseDAO.getCriteria(BaseUser.class);
		criteria.add(Restrictions.eq("account", user.getAccount()));
		// criteria.add(Restrictions.eq("password", user.getPassword()));

		List<BaseUser> users = criteria.list();
		if (!users.isEmpty()) {
			return users.iterator().next();
		}

		return null;
	}

	public ResponseObject login(String json, HttpServletRequest request) {
		User user = this.getObjectFromJson(json, User.class);

		ResponseObject ro = new ResponseObject();

		ro.setData(user);

		Object failedTimes = request.getSession().getAttribute(
				Globe.SESSION_LOGIN_FAILED_TIME_KEY);

		int failed = 0;
		if (null != failedTimes) {
			failed = (Integer) failedTimes;
		}

		if (failed > 2 || user.isNeedVerify()) {
			Object sverify = request.getSession().getAttribute(
					RandomValidateCode.RANDOMCODEKEY);
			if (null == sverify
					|| !sverify.toString().equals(user.getVerifyCode())) {
				ro.addInfo("验证码错误！");
				ro.setStatus(new Status(401, "验证码错误！"));
				user.setNeedVerify(true);
				return ro;
			}
		}

		BaseUser baseUser = this.getUser(user);
		//先判断这个用户是否短信验证码不为null。
		if(null != user.getSmsVerifyCode()){
			// 如果短信验证码不为null,则判断短信验证码是否正确.
				BaseUserSms baseUserSms = smsService.getBaseSmsByMobile(user
						.getAccount());
				if (null == baseUserSms
						|| StringUtils.isBlank(baseUserSms.getRandomCode())) {
					ro.addInfo("短信验证码错误!");
					ro.setStatus(new Status(402, "短信验证码错误!"));
					user.setNeedVerify(true);
					return ro;
				} else {
					if (!StringUtils.equalsIgnoreCase(user.getSmsVerifyCode(),
							baseUserSms.getRandomCode())) {
						ro.addInfo("短信验证码错误!");
						ro.setStatus(new Status(402, "短信验证码错误!"));
						user.setNeedVerify(true);
						return ro;
					} else {
						if(null == baseUser){
							baseUser = new BaseUser();
							baseUser.setAccount(user.getAccount());
							baseUser.setState(dictItemService
									.getBaseDictItemByDictCodeAndDictItemCode("userState",
											"userTypeRegist"));
						}
						baseUser.setLastLoginTime(new Date());
						request.getSession().setAttribute(
								Globe.SESSION_USER_KEY, baseUser);
						ro.setStatus(Status.STATUS_200);
						hibernateBaseDAO.update(baseUser);
						BaseDictItem smsOperateState= dictItemService.getBaseDictItemByDictCodeAndDictItemCode("smsOperateState", "invalid");
						baseUserSms.setSmsOperateState(smsOperateState);
						hibernateBaseDAO.update(baseUserSms);
					}
				}
				
		}else if (null != baseUser) {
				if (!verifyPassword(user.getPassword(), baseUser)) {
					request.getSession()
							.removeAttribute(Globe.SESSION_USER_KEY);
					request.getSession().setAttribute(
							Globe.SESSION_LOGIN_FAILED_TIME_KEY, failed + 1);
					if (failed + 1 > 2) {
						user.setNeedVerify(true);
					}
					ro.addInfo("用户名或者密码错误！");
					ro.setStatus(new Status(401, "用户名或者密码错误！"));
				} else if (baseUser.getLastLoginTime() == null) {
					request.getSession()
							.removeAttribute(Globe.SESSION_USER_KEY);
					request.getSession().setAttribute(
							Globe.SESSION_LOGIN_FAILED_TIME_KEY, failed + 1);
					if (failed + 1 > 2) {
						user.setNeedVerify(true);
					}
					ro.addInfo("首次登陆请先修改密码！");
					ro.setStatus(new Status(401, "首次登陆请先修改密码！"));
				} else {
					if (null == baseUser.getEmployee()) {
						user.setUserName(baseUser.getName());
					} else {
						user.setUserName(baseUser.getEmployee().getName());
					}

					user.setUserId(baseUser.getId());
					if (null != baseUser.getEmployee()) {
						user.setEmployeeId(baseUser.getEmployee().getId());
						user.setEmployeeNo(baseUser.getEmployee().getNo());
						user.setEmployeeName(baseUser.getEmployee().getName());
						user.setEmployeeMobile(baseUser.getEmployee()
								.getMobile());
						if (null != baseUser.getEmployee().getPosition()) {
							user.setPosition(baseUser.getEmployee()
									.getPosition().getName());
						}
					}
					request.getSession().setAttribute(Globe.SESSION_USER_KEY,
							baseUser);
					request.getSession().removeAttribute(
							Globe.SESSION_LOGIN_FAILED_TIME_KEY);
					user.clearPassword();
					ro.setStatus(Status.STATUS_200);
					if (baseUser.isMD5() == null || !baseUser.isMD5()) {
						String pass = baseUser.getPassword();
						pass = MD5Utils.digest(pass);
						baseUser.setPassword(pass);
						baseUser.setPassword1(pass);
						baseUser.setIsMD5(true);
					}
					baseUser.setLastLoginTime(new Date());
					hibernateBaseDAO.update(baseUser);
				}
			} else {
			request.getSession().removeAttribute(Globe.SESSION_USER_KEY);
			request.getSession().setAttribute(
					Globe.SESSION_LOGIN_FAILED_TIME_KEY, failed + 1);
			if (failed + 1 > 2) {
				user.setNeedVerify(true);
			}
			ro.addInfo("用户名或者密码错误！");
			ro.setStatus(new Status(401, "用户名或者密码错误！"));
		}
		return ro;
	}

	private boolean verifyPassword(String password, BaseUser baseUser) {
		String pass1 = password;
		String pass2 = baseUser.getPassword();
		if (baseUser.isMD5() != null && baseUser.isMD5()) {
			pass1 = MD5Utils.digest(pass1);
		}
		return StringUtils.equals(pass1, pass2);
	}

	@SuppressWarnings("unchecked")
	public void changePassword(String userJson, ResponseObject ro,
			HttpServletRequest request) {
		User user = this.getObjectFromJson(userJson, User.class);
		Criteria criteria = super.hibernateBaseDAO.getCriteria(BaseUser.class);

		criteria.add(Restrictions.eq("account", user.getAccount()));

		List<BaseUser> userList = criteria.list();

		Status s = new Status(300, "密码修改失败！");

		if (user.isNeedVerify()) {
			Object sverify = request.getSession().getAttribute(
					RandomValidateCode.RANDOMCODEKEY);
			if (null == sverify
					|| !sverify.toString().equals(user.getVerifyCode())) {
				ro.addInfo("验证码错误！");
				ro.setStatus(new Status(401, "验证码错误！"));
				return;
			}
		}
		if (null != user.getSmsVerifyCode()) {
			BaseUserSms baseUserSms = smsService.getBaseSmsByMobile(user
					.getAccount());
			if (null == baseUserSms
					|| StringUtils.isBlank(baseUserSms.getRandomCode())) {
				ro.addInfo("短信验证码错误!");
				ro.setStatus(new Status(402, "短信验证码错误!"));
				return;
			} else {
				if (!StringUtils.equalsIgnoreCase(user.getSmsVerifyCode(),
						baseUserSms.getRandomCode())) {
					ro.addInfo("短信验证码错误!");
					ro.setStatus(new Status(402, "短信验证码错误!"));
					return;
				}else{
					BaseDictItem smsOperateState= dictItemService.getBaseDictItemByDictCodeAndDictItemCode("smsOperateState", "invalid");
					baseUserSms.setSmsOperateState(smsOperateState);
					hibernateBaseDAO.update(baseUserSms);
				}
			}
			BaseUser baseUser = userList.get(0);
			String pass = MD5Utils.digest(user.getPassword());
			baseUser.setLastLoginTime(new Date());
			baseUser.setPassword(pass);
			baseUser.setPassword1(pass);
			baseUser.setIsMD5(true);
			hibernateBaseDAO.update(baseUser);
			ro.setStatus(Status.STATUS_200);
		} else {
			if (!userList.isEmpty()) {
				BaseUser baseUser = userList.get(0);
				if (!verifyPassword(user.getPassword(), baseUser)) {
					ro.addInfo("用户名或者密码错误！");
					ro.setStatus(s);
					return;
				} else if (!user.getNewPassword().equals(user.getPassword1())) {
					ro.addInfo("新密码和确认密码不一致！");
					ro.setStatus(s);
					return;
				} else {
					String pass = MD5Utils.digest(user.getNewPassword());
					baseUser.setLastLoginTime(new Date());
					baseUser.setPassword(pass);
					baseUser.setPassword1(pass);
					baseUser.setIsMD5(true);
					hibernateBaseDAO.update(baseUser);
					ro.setStatus(Status.STATUS_200);
				}
			} else {
				ro.addInfo("用户名或者密码错误！");
				ro.setStatus(s);
				return;
			}
		}
	}

	// public void operateSendSms(String userJson, ResponseObject ro){
	// User user = this.getObjectFromJson(userJson, User.class);
	// Status s = new Status(400, "发送短信验证码失败！");
	// if(StringUtils.isBlank(user.getAccount())){
	// ro.addInfo("用户手机号不能为空！");
	// ro.setStatus(s);
	// return;
	// }
	// //校验该手机号是否已经注册过.
	// BaseUser baseUser=this.getUser(user);
	// if(null != baseUser){
	// ro.addInfo("该用户已经注册过,无需再次注册！");
	// ro.setStatus(s);
	// return;
	// }
	// if(!smsService.checkSmsSendTimeInterval(user.getAccount(),
	// Constants.SMS_REGISTER)){
	// ro.addInfo("两次发送短信时间间隔太短,请稍等再次注册.");
	// ro.setStatus(s);
	// return;
	// }
	// BaseUserSms
	// baseSms=smsService.getBaseSmsByMobile(user.getAccount(),Constants.SMS_REGISTER);
	// if(null == baseSms){
	// baseSms = new BaseUserSms();
	// }
	// //生成随机六位码
	// Random random = new Random();
	// String randomCode=String.valueOf(random.nextInt(899999)+100000);
	// logger.info("randomcode--------->"+randomCode);
	// baseSms.setMobile(user.getAccount());
	// baseSms.setRandomCode(randomCode);
	// baseSms.setSendStartTime(new Date());
	// //发送短信.
	// //baseSms.setSendState(true);
	// baseSms.setSendEndTime(new Date());
	// smsService.updateBaseSms(baseSms,Constants.SMS_REGISTER);
	// ro.setStatus(Status.STATUS_200);
	// }
	//
	/**
	 * 
	 * @return
	 */
	public void operateRegister(String json, ResponseObject ro,
			HttpServletRequest request) {
		User user = this.getObjectFromJson(json, User.class);
		Status s = new Status(440, "注册失败！");
		// 校验该手机号是否已经注册过.
		BaseUser baseUser = this.getUser(user);
		if (null != baseUser && StringUtils.isNotBlank(baseUser.getPassword())) {
			ro.addInfo("该用户已经注册过,无需再次注册！");
			ro.setStatus(s);
		}

		if (StringUtils.isBlank(user.getAccount())) {
			ro.addInfo("用户手机号不能为空！");
			ro.setStatus(s);
		}
		if (StringUtils.isBlank(user.getPassword())
				|| StringUtils.isBlank(user.getPassword1())) {
			ro.addInfo("密码不能为空！");
			ro.setStatus(s);
		} else if (!user.getPassword().equals(user.getPassword1())) {
			ro.addInfo("两次密码不一致！");
			ro.setStatus(s);
		}
		if (user.isNeedVerify()) {
			Object sverify = request.getSession().getAttribute(
					RandomValidateCode.RANDOMCODEKEY);
			if (null == sverify
					|| !sverify.toString().equals(user.getVerifyCode())) {
				ro.addInfo("验证码错误！");
				ro.setStatus(new Status(401, "验证码错误！"));
			}

		}
		// 校验短信验证码是否正确.
		if (null != user.getSmsVerifyCode()) {
			BaseUserSms baseUserSms = smsService.getBaseSmsByMobile(user
					.getAccount());
			if (null != baseUserSms
					&& !StringUtils.isBlank(baseUserSms.getRandomCode())) {
				if (!StringUtils.equalsIgnoreCase(user.getSmsVerifyCode(),
						baseUserSms.getRandomCode())) {
					ro.addInfo("短信验证码错误!");
					ro.setStatus(new Status(402, "短信验证码错误!"));
					user.setNeedVerify(true);
				}
			} else {
				if (!StringUtils.equalsIgnoreCase(user.getVerifyCode(),
						baseUserSms.getRandomCode())) {
					ro.addInfo("请先获取验证码!");
					ro.setStatus(new Status(402, "请先获取验证码!"));
					user.setNeedVerify(true);
				}else{
					BaseDictItem smsOperateState= dictItemService.getBaseDictItemByDictCodeAndDictItemCode("smsOperateState", "invalid");
					baseUserSms.setSmsOperateState(smsOperateState);
					hibernateBaseDAO.update(baseUserSms);
				}
			}
		}
		if (null != ro.getInfos() && ro.getInfos().size() > 0) {
			return;
		}
		if(null ==baseUser){
			 baseUser = new BaseUser();
		}
		baseUser.setAccount(user.getAccount());
		String pass = MD5Utils.digest(user.getNewPassword());
		baseUser.setLastLoginTime(new Date());
		baseUser.setPassword(pass);
		baseUser.setPassword1(pass);
		baseUser.setIsMD5(true);
		baseUser.setState(dictItemService
				.getBaseDictItemByDictCodeAndDictItemCode("userState",
						"userTypeRegist"));
		hibernateBaseDAO.update(baseUser);
		ro.setStatus(Status.STATUS_200);
	}

	/**
	 * 发送短信密码.
	 * 
	 * @return
	 */
	public void retrievePassword(String json, ResponseObject ro) {

	}

	List<BaseUser> getAllUsers() {
		return super.find(BaseUser.class);
	}

	@Value("${sms.send.time.interval}")
	private Long smsSendTimeInterval;

}
