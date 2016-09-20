package simple.base.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.config.annotation.DataLength;
import simple.config.annotation.Domain;
import simple.config.annotation.ExtentionPoint;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.SearchField;
import simple.config.annotation.TabView;
import simple.config.annotation.TabViewType;
import simple.config.annotation.TableColumn;

@Domain
@Entity
@Table(name = "BASE_MESSAGE")
@SequenceGenerator(name = "SEQ_BASE_MESSAGE", sequenceName = "SEQ_BASE_MESSAGE")
// @StandardOperation(add = false, delete = false, modify = false, imp = false)
@ExtentionPoint("simple.base.support.BaseMessageExtentionPoint")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_MESSAGE") })
public class BaseMessage implements Serializable {

	private static final long serialVersionUID = 5487425984781858092L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	private Long id;

	@Column(name = "TITLE", length = DataLength.LONG_TEXT_LENGTH)
	@RepresentationField(sort = 10, title = "标题")
	@SearchField(canFuzzy = true)
	@TableColumn(title = "标题", sort = 10)
	@NotNull(message = "标题不能为空！")
	@Length(max = DataLength.LONG_TEXT_LENGTH)
	private String title;

	@Column(name = "CONTENT", length = DataLength.MAX_STRING_LENGTH)
	@RepresentationField(sort = 20, title = "内容" ,view =RepresentationFieldType.TEXTAREA)
	@TableColumn(title = "内容", sort = 20)
	@NotNull(message = "内容不能为空！")
	@Length(max = DataLength.MAX_STRING_LENGTH)
	private String content;

	@Column(name = "PUSH_TIME")
	@RepresentationField(sort = 30, title = "推送时间", view = RepresentationFieldType.DATETIME)
	@SearchField(isRange = true)
	@TableColumn(title = "推送时间", sort = 30)
	private Date pushTime;

	@Column(name = "EXPIRE")
	@RepresentationField(sort = 40, title = "失效时间", view = RepresentationFieldType.DATETIME)
	@SearchField(isRange = true)
	@TableColumn(title = "失效时间", sort = 40)
	private Date expire;

	@Column(name = "PRIORITY")
	@RepresentationField(sort = 50, title = "优先级")
	@SearchField
	@TableColumn(title = "优先级", sort = 50)
	@Enumerated(EnumType.ORDINAL)
	private Priority priority;

	@Column(name = "TYPE")
	@RepresentationField(sort = 60, title = "类型")
	@SearchField
	@TableColumn(title = "类型", sort = 60)
	@Enumerated(EnumType.ORDINAL)
	private MessageType type;

	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "BASE_MESSAGE_DELIVERY", joinColumns = { @JoinColumn(name = "MESSAGE_ID", nullable = true, updatable = true ) }, inverseJoinColumns = { @JoinColumn(name = "RECEIVER_ID", nullable = true, updatable = true) })
	@OrderBy("id ASC")
	@RepresentationField(view = RepresentationFieldType.TAB, title = "接收用户")
	@TabView(TabViewType.SELECT_LIST)
	private Set<BaseUser> users;
	
	@OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
	@Cascade({ CascadeType.ALL })
//	@RepresentationField(title = "接收人列表", view = RepresentationFieldType.TAB)
//	@TabView
	private Set<BaseMessageDelivery> messageDeliveries = new HashSet<BaseMessageDelivery>(
			0);
	
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
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

	public enum MessageType {
		BULLETIN, MESSAGE, ALARM
	}

	public enum Priority {
		LOW, MEDIUM, HIGH
	}
	
	public Set<BaseMessageDelivery> getMessageDeliveries() {
		return messageDeliveries;
	}

	public void setMessageDeliveries(Set<BaseMessageDelivery> messageDeliveries) {
		this.messageDeliveries = messageDeliveries;
	}

	public Set<BaseUser> getUsers() {
		return users;
	}

	public void setUsers(Set<BaseUser> users) {
		this.users = users;
	}

}
