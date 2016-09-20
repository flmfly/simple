package simple.base.support;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.base.model.BaseUser;
import simple.config.annotation.support.ExtentionPoint;
import simple.core.service.BaseService;
import simple.core.util.MD5Utils;

@Component
public class BaseUserExtentionPoint implements ExtentionPoint {

	@Autowired
	private BaseService baseService;

	@Override
	public void beforeSave(Object entity) {
		// TODO Auto-generated method stub
		BaseUser baseUser = (BaseUser) entity;
		if (baseUser == null) {
			return;
		}

		if (null != baseUser.getId()
				&& StringUtils.isBlank(baseUser.getPassword())
				&& StringUtils.isBlank(baseUser.getPassword1())) {
			BaseUser tmpUser = this.baseService.get(BaseUser.class,
					baseUser.getId());
			this.baseService.evit(tmpUser);
			baseUser.setPassword(tmpUser.getPassword());
			baseUser.setPassword1(tmpUser.getPassword1());
		} else if (baseUser.isMD5() != null && baseUser.isMD5()) {
			String pass = baseUser.getPassword();
			pass = MD5Utils.digest(pass);
			baseUser.setPassword(pass);
			baseUser.setPassword1(pass);
		}
	}

	@Override
	public void afterSave(Object entity) {

	}

	@Override
	public void beforeFetch(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterFetch(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterDelete(Object entity) {
	}

}
