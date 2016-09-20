package simple.base.support;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.base.model.BaseMessage;
import simple.base.model.BaseMessageDelivery;
import simple.base.model.BaseUser;
import simple.base.service.MessageService;
import simple.config.annotation.support.ExtentionPoint;
import simple.core.service.CriteriaService;

@Component
public class BaseMessageExtentionPoint implements ExtentionPoint {

	@Autowired
	private MessageService messageService;
	
	@Autowired
	private CriteriaService criteriaService;
	
	@Override
	public void beforeSave(Object entity) {
		BaseMessage baseMessage=(BaseMessage)entity;
		Set<BaseUser> users=baseMessage.getUsers();
		Set<BaseMessageDelivery> messageDeliveries = new HashSet<BaseMessageDelivery>();
		if(users!=null){
			for (BaseUser u : users) {
				BaseMessageDelivery delivery=new BaseMessageDelivery();
				delivery.setUser(u);
				messageDeliveries.add(delivery);
			}
		}
		baseMessage.setUsers(new LinkedHashSet<BaseUser>(0));
		baseMessage.setMessageDeliveries(messageDeliveries);
	}

	@Override
	public void afterSave(Object entity) {
		BaseMessage baseMessage=(BaseMessage)entity;
		messageService.pushMsgToSlot(baseMessage.getMessageDeliveries());
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
