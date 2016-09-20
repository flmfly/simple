package simple.sms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.Domain;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.SearchField;
import simple.config.annotation.StandardOperation;
import simple.config.annotation.TableColumn;
import simple.core.jpa.convert.BooleanToStringConverter;

@Domain(value = "短信日志信息")
@Entity
@Table(name = "MESSAGE_SMS_LOG")
@StandardOperation(add = false, modify = false, delete = false, imp = false)
@SequenceGenerator(name = "SEQ_MESSAGE_SMS_LOG", sequenceName = "SEQ_MESSAGE_SMS_LOG")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_MESSAGE_SMS_LOG") })
public class MessageSmsLog implements Serializable {

	private static final long serialVersionUID = -1795995389201796825L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	public Long id;

	@Column(name = "MOBILE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 20, title = "手机号")
	@TableColumn(title = "手机号", sort = 20)
	@NotNull(message = "手机号不能为空！")
	@SearchField
	@Length(max = DataLength.CODE_LENGTH)
	public String mobile;

	@Column(name = "MESSAGE", length = DataLength.MAX_STRING_LENGTH)
	@RepresentationField(sort = 30, title = "发送内容")
	@TableColumn(title = "发送内容", sort = 30)
	@NotNull(message = "发送内容不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	public String message;

	@Column(name = "SEND_STATE", columnDefinition = "CHAR(1)")
	@RepresentationField(sort = 40, title = "发送状态", isSearchField = true, view = RepresentationFieldType.BOOLEAN)
	@BooleanValue({ "成功", "失败" })
	@TableColumn(title = "发送状态", sort = 40)
	@NotNull(message = "发送状态不能为空！")
	@Convert(converter = BooleanToStringConverter.class)
	public Boolean sendState;

	@Column(name = "SEND_TIME")
	@RepresentationField(sort = 50, title = "发送时间", view = RepresentationFieldType.DATETIME, disable = true)
	@TableColumn(title = "发送时间", sort = 50)
	public Date sendTime;

	@Column(name = "MID", length = DataLength.CODE_LENGTH)
	@Length(max = DataLength.CODE_LENGTH)
	public String mid;
}
