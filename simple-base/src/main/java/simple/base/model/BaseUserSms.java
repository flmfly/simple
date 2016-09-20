package simple.base.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.DataLength;
import simple.config.annotation.DictField;
import simple.config.annotation.Domain;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.SearchField;
import simple.config.annotation.StandardOperation;
import simple.config.annotation.TableColumn;
import simple.core.validation.annotation.UniqueKey;

@Domain(value = "短信信息")
@Entity
@Table(name = "BASE_USER_SMS")
@StandardOperation(add = false, modify = false, delete = false, imp = false)
@UniqueKey(columnNames = { "mobile" }, message = "短信信息已存在！")
@SequenceGenerator(name = "SEQ_BASE_USER_SMS", sequenceName = "SEQ_BASE_USER_SMS")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_USER_SMS") })
public class BaseUserSms implements Serializable {

	private static final long serialVersionUID = -1795995389201796825L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	private Long id;

	// @ManyToOne
	// @JoinColumn(name = "SMS_OPERATE_TYPE")
	// @RepresentationField(sort = 10, title = "短信操作类型", view =
	// RepresentationFieldType.SELECT)
	// @DictField("smsOperateType")
	// @SearchField
	// @Reference(id = "id", label = "name")
	// @AssociateTableColumn(titles = "短信操作类型", columns = "name", sorts = "10")
	// private BaseDictItem smsOperateType;

	@Column(name = "MOBILE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 20, title = "手机号")
	@TableColumn(title = "手机号", sort = 20)
	@NotNull(message = "手机号不能为空！")
	@SearchField
	@Length(max = DataLength.CODE_LENGTH)
	private String mobile;

	@Column(name = "RANDOM_CODE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 30, title = "随机码")
	@TableColumn(title = "随机码", sort = 30)
	@NotNull(message = "随机码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String randomCode;

	 @ManyToOne
	 @JoinColumn(name = "SMS_OPERATE_STATE_ID")
	 @RepresentationField(sort = 40, title = "短信状态", view =RepresentationFieldType.SELECT)
	 @DictField("smsOperateState")
	 @SearchField
	 @Reference(id = "id", label = "name")
	 @AssociateTableColumn(titles = "短信状态", columns = "name", sorts = "40")
	 private BaseDictItem smsOperateState;
	
	@Column(name = "SEND_TIME")
	@RepresentationField(sort = 50, title = "发送开始时间", view = RepresentationFieldType.DATETIME, disable = true)
	@TableColumn(title = "发送开始时间", sort = 50)
	private Date sendTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	// public BaseDictItem getSmsOperateType() {
	// return smsOperateType;
	// }
	//
	// public void setSmsOperateType(BaseDictItem smsOperateType) {
	// this.smsOperateType = smsOperateType;
	// }

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getRandomCode() {
		return randomCode;
	}

	public void setRandomCode(String randomCode) {
		this.randomCode = randomCode;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public BaseDictItem getSmsOperateState() {
		return smsOperateState;
	}

	public void setSmsOperateState(BaseDictItem smsOperateState) {
		this.smsOperateState = smsOperateState;
	}

}
