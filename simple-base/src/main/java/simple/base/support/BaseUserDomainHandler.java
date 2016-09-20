package simple.base.support;

import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import simple.base.model.BaseDictItem;
import simple.base.model.BaseUserSms;
import simple.base.service.DictItemService;
import simple.base.service.SmsService;
import simple.base.vo.User;
import simple.config.annotation.support.DomainHandlerAdapter;
import simple.core.model.DomainMessage;
import simple.core.service.BaseService;

@Component
public class BaseUserDomainHandler extends DomainHandlerAdapter {

	@Autowired
	private BaseService baseService;

	@Autowired
	private DictItemService dictItemService;

	@Autowired
	private SmsService smsService;

//	@Value("${register.sms.prev.str}")
	private String msgPrevStr;

	@Override
	public Object getMessage(String json) {
		User user = this.baseService.getObjectFromJson(json, User.class);
		BaseUserSms baseSms = this.smsService.getBaseSmsByMobile(user
				.getAccount());
		DomainMessage dm = new DomainMessage();
		if (this.smsService.checkSmsSendTimeInterval(baseSms)) {
			if (null == baseSms) {
				baseSms = new BaseUserSms();
				baseSms.setMobile(user.getAccount());
			}
			Random random = new Random();
			int randomMath = random.nextInt(899999);
			baseSms.setRandomCode(String.valueOf((int) (randomMath + 100000)));
			baseSms.setSendTime(new Date());
			BaseDictItem smsOperateState = dictItemService
					.getBaseDictItemByDictCodeAndDictItemCode(
							"smsOperateState", "valid");
			baseSms.setSmsOperateState(smsOperateState);
			this.baseService.update(baseSms);
			dm.setCode(DomainMessage.SUCCESS_CODE);
			dm.setMessage(msgPrevStr + baseSms.getRandomCode());
		} else {
			dm.setCode(DomainMessage.FAILED_CODE);
			dm.setMessage("请在"
					+ ((int) (this.smsService.getTimeInterval() / 1000))
					+ "后重新发送！");
		}
		return dm;
	}
}
