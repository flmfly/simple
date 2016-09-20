package simple.base.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import simple.base.model.BaseMessage;
import simple.base.model.BaseMessage.MessageType;
import simple.base.model.BaseMessage.Priority;
import simple.base.model.BaseMessageDelivery;
import simple.base.model.BaseUser;
import simple.base.vo.Message;
import simple.core.service.BaseService;

@Service
public class MessageService extends BaseService {

	@Autowired
	private UserService userService;

	private static Map<String, Set<Message>> userSlot = new HashMap<String, Set<Message>>();

	public void setRead(String account, String ids) {
		if (userSlot.containsKey(account)) {
			StringTokenizer st = new StringTokenizer(ids, ",");
			Set<Long> idSet = new HashSet<Long>();
			while (st.hasMoreTokens()) {
				Long id = Long.parseLong(st.nextToken());
				idSet.add(id);
				BaseMessageDelivery bmd = super.get(BaseMessageDelivery.class,
						id);
				bmd.setReadFlag(true);
				bmd.setReadTime(new Date());
				super.save(bmd);
			}

			Set<Message> readSet = new HashSet<Message>();
			Set<Message> msgList = userSlot.get(account);
			for (Message msg : msgList) {
				if (idSet.contains(msg.getDeliveryId())) {
					readSet.add(msg);
				}
			}

			msgList.removeAll(readSet);
		}
	}

	public List<Message> getMessagesByUser(String account, String maxId) {
		List<Message> rtn = new ArrayList<Message>();

		if (userSlot.containsKey(account)) {
			Set<Message> slot = userSlot.get(account);
			long id = -1;
			if (StringUtils.isNotBlank(maxId)) {
				id = Long.parseLong(maxId);
			}
			long currentTime = System.currentTimeMillis();
			Set<Message> expiredMsgs = new HashSet<Message>();
			for (Message tempMsg : slot) {
				if (null != tempMsg.getExpire()
						&& tempMsg.getExpire().getTime() <= currentTime) {
					expiredMsgs.add(tempMsg);
				} else {
					if (tempMsg.getDeliveryId().longValue() > id) {
						rtn.add(tempMsg);
					}
				}
			}

			slot.removeAll(expiredMsgs);
		}

		return rtn;
	}

	public void publishBulletin(String title, String content, Date expire,
			Priority priority) {
		this.saveMessage(title, content, this.userService.getAllUsers(),
				priority, MessageType.BULLETIN, expire);
	}

	public void pushMessage(String title, String content,
			List<BaseUser> receivers, Priority priority) {
		this.saveMessage(title, content, receivers, priority,
				MessageType.MESSAGE, null);
	}

	public void pushAlarm(String title, String content,
			List<BaseUser> receivers, Priority priority) {
		this.saveMessage(title, content, receivers, priority,
				MessageType.ALARM, null);
	}

	private BaseMessage saveMessage(String title, String content,
			List<BaseUser> receivers, Priority priority, MessageType type,
			Date expire) {
		BaseMessage msg = new BaseMessage();
		msg.setTitle(title);
		msg.setContent(content);
		msg.setExpire(expire);
		msg.setPriority(priority);
		msg.setPushTime(new Date());
		msg.setType(type);

		Set<BaseMessageDelivery> bmdSet = new HashSet<BaseMessageDelivery>();
		for (BaseUser baseUser : receivers) {
			BaseMessageDelivery bmd = new BaseMessageDelivery();
			bmd.setReadFlag(false);
			bmd.setUser(baseUser);
			bmdSet.add(bmd);
		}

		super.update(msg);

		for (BaseMessageDelivery baseMessageDelivery : bmdSet) {
			baseMessageDelivery.setMessage(msg);
			super.update(baseMessageDelivery);
		}

		this.pushMsgToSlot(bmdSet);

		return msg;
	}

	public void pushMsgToSlot(Collection<BaseMessageDelivery> bmdSet) {
		for (BaseMessageDelivery bmd : bmdSet) {
			String key = bmd.getUser().getAccount();
			Set<Message> msgList = userSlot.get(key);
			if (null == msgList) {
				msgList = new LinkedHashSet<Message>();
				userSlot.put(key, msgList);
			}

			Message vo = new Message();
			BeanUtils.copyProperties(bmd.getMessage(), vo,"users");
			vo.setDeliveryId(bmd.getId());
			msgList.add(vo);
		}
	}

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager txManager;

	@PostConstruct
	private void init() {
		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@SuppressWarnings("unchecked")
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Criteria criteria = hibernateBaseDAO
						.getCriteria(BaseMessageDelivery.class);
				criteria.createCriteria("message").add(
						Restrictions.or(Restrictions.isNull("expire"),
								Restrictions.gt("expire", new Date())));
				criteria.add(Restrictions.isNull("readFlag"));
				pushMsgToSlot(criteria.list());
			}
		});
	}

	@PreDestroy
	public void destroy() {
	}
}
