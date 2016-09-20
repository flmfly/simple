package simple.base.vo;

import java.util.Date;

import simple.base.model.BaseMessage.MessageType;
import simple.base.model.BaseMessage.Priority;

public class Message  implements Comparable<Message>{
	private String title;

	private String content;

	private Date pushTime;

	private Priority priority;

	private MessageType type;

	private Date expire;

	private Long deliveryId;

	public Long getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(Long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getPushTime() {
		return pushTime;
	}

	public void setPushTime(Date pushTime) {
		this.pushTime = pushTime;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}

	@Override
	public int compareTo(Message o) {
		// TODO Auto-generated method stub
		return deliveryId.compareTo(o.deliveryId);
	}

}
