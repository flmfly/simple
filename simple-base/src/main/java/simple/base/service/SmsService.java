package simple.base.service;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import simple.base.model.BaseUserSms;
import simple.core.service.BaseService;

@Service
public class SmsService extends BaseService {

	@Autowired
	private DictItemService dictItemService;

	public Long getTimeInterval() {
		return smsSendTimeInterval;
	}

	// /**
	// * 用于校验上次和这次发送短信的时间间隔。
	// * @param mobile
	// * @return
	// */
	public boolean checkSmsSendTimeInterval(BaseUserSms baseSms) {
		if (null != baseSms && null != baseSms.getSendTime()
				&& null != smsSendTimeInterval) {
			if (System.currentTimeMillis() - baseSms.getSendTime().getTime() < smsSendTimeInterval) {
				return false;
			}
		}
		return true;
	}
	@SuppressWarnings("unchecked")
	public BaseUserSms getBaseSmsByMobile(String mobile) {
		Criteria criteria = super.hibernateBaseDAO
				.getCriteria(BaseUserSms.class);
		criteria.createAlias("smsOperateState","smsOperateStateAlias");
		criteria.add(Restrictions.eq("mobile", mobile));
		criteria.addOrder(Order.desc("sendTime"));
		criteria.add(Restrictions.eq("smsOperateStateAlias.code","valid"));
		List<BaseUserSms> basesmses = criteria.list();
		if (!basesmses.isEmpty()) {
			return basesmses.iterator().next();
		}
		return null;
	}

	// public void updateBaseSms(BaseUserSms baseSms) {
	// BaseSmsLog baseSmsLog = new BaseSmsLog();
	// baseSmsLog.setMobile(baseSms.getMobile());
	// baseSmsLog.setRandomCode(baseSms.getRandomCode());
	// baseSmsLog.setSendStartTime(baseSms.getSendStartTime());
	// baseSmsLog.setSendEndTime(baseSms.getSendEndTime());
	// baseSmsLog.setSendState(baseSms.getSendState());
	// hibernateBaseDAO.save(baseSmsLog);
	// hibernateBaseDAO.save(baseSms);
	// }

	@Value("${sms.send.time.interval}")
	private Long smsSendTimeInterval;
}
