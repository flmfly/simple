package simple.base.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import simple.config.annotation.BooleanValue;
import simple.config.annotation.BusinessKey;
import simple.config.annotation.Domain;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.StandardOperation;
import simple.config.annotation.TableColumn;
import simple.core.jpa.convert.BooleanToStringConverter;

@Domain
@Entity
@Table(name = "BASE_MESSAGE_DELIVERY")
@SequenceGenerator(name = "SEQ_BASE_MESSAGE_DELIVERY", sequenceName = "SEQ_BASE_MESSAGE_DELIVERY")
@StandardOperation(add = false, delete = false, modify = false, imp = false)
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_MESSAGE_DELIVERY") })
@BusinessKey("user,message")
public class BaseMessageDelivery implements Serializable {

	private static final long serialVersionUID = -1986951842535633777L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "RECEIVER_ID")
	@RepresentationField(title = "接收人", sort = 200, view = RepresentationFieldType.REFERENCE)
	@Reference(id = "id", label = "account")
	private BaseUser user;

	@ManyToOne
	@JoinColumn(name = "MESSAGE_ID")
	private BaseMessage message;

	@Column(name = "READ_FLAG", columnDefinition = "CHAR(1)")
	@RepresentationField(title = "查看标志", sort = 150, view = RepresentationFieldType.BOOLEAN, defaultVal = "false")
	@BooleanValue({ "已读", "未读" })
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean readFlag;

	@Column(name = "READ_TIME")
	@RepresentationField(sort = 30, title = "查看时间", view = RepresentationFieldType.DATETIME)
	@TableColumn(title = "查看时间", sort = 30)
	private Date readTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BaseUser getUser() {
		return user;
	}

	public void setUser(BaseUser user) {
		this.user = user;
	}

	public BaseMessage getMessage() {
		return message;
	}

	public void setMessage(BaseMessage message) {
		this.message = message;
	}

	public Boolean getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(Boolean readFlag) {
		this.readFlag = readFlag;
	}

	public Date getReadTime() {
		return readTime;
	}

	public void setReadTime(Date readTime) {
		this.readTime = readTime;
	}

}
