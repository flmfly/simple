package simple.base.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import simple.base.model.BaseUser;
import simple.config.annotation.support.OperationHandler;
import simple.core.service.BaseService;
import simple.core.util.MD5Utils;

@Component
public class BaseUserResetOperationHandler implements OperationHandler {

	@Autowired
	private BaseService baseService;
	
	@Override
	public boolean disabled(Object domain) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OperationResult handle(Map<String, Object> parameters,
			List<Object> domains) {
		// TODO Auto-generated method stub
		OperationResult result=new OperationResult();
		result.setSuccess(true);
		String pass=(String)parameters.get("password");
		if(StringUtils.isBlank(pass)){
			result.setSuccess(false);
			result.addErrorMessage("请输入新密码！");
			return result;
		}
		for (Object domain : domains) {
			BaseUser baseUser=(BaseUser)domain;
			baseUser.setLastLoginTime(null);
			baseUser.setPassword(pass);
			baseUser.setPassword1(pass);
			baseUser.setIsMD5(true);
			baseService.update(baseUser);
		}
		return result;
	}

}
