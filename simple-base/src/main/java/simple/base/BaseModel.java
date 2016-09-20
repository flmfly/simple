package simple.base;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import simple.base.annotation.support.LoginUserAutoFillHandler;
import simple.base.annotation.support.LoginUserDefaultValueHandler;
import simple.base.model.BaseUser;
import simple.config.annotation.AutoFill;
import simple.config.annotation.AutoFillTrigger;
import simple.config.annotation.DefaultValue;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.TableColumn;
import simple.config.annotation.support.CurrentDateTimeAutoFillHandler;
import simple.config.annotation.support.DateTimeDefaultValueHandler;

@MappedSuperclass
public abstract class BaseModel implements Serializable {
	
	private static final long serialVersionUID = 1848555106297592507L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", show = false)
	private Long id;

	@Column(name = "CREATE_TIME")
	@RepresentationField(title = "创建时间", sort = 99996, view = RepresentationFieldType.DATETIME, disable = true)
	@DefaultValue(handler = DateTimeDefaultValueHandler.class)
	private Date createTime;

	@ManyToOne
	@JoinColumn(name = "CREATE_USER_ID")
	@RepresentationField(title = "创建人", sort = 99997, view = RepresentationFieldType.REFERENCE, disable = true)
	@Reference(id = "id", label = "account")
	@DefaultValue(handler = LoginUserDefaultValueHandler.class)
	private BaseUser createUserID;

	@Column(name = "UPDATE_TIME")
	@RepresentationField(title = "最后修改时间", sort = 99998, view = RepresentationFieldType.DATETIME, disable = true)
	@AutoFill(handler = CurrentDateTimeAutoFillHandler.class, trigger = AutoFillTrigger.ALWAYS)
	private Date updateTime;

	@ManyToOne
	@JoinColumn(name = "UPDATE_USER_ID")
	@RepresentationField(title = "最后修改人", sort = 99999, view = RepresentationFieldType.REFERENCE, disable = true)
	@AutoFill(handler = LoginUserAutoFillHandler.class, trigger = AutoFillTrigger.ALWAYS)
	@Reference(id = "id", label = "account")
	private BaseUser updateUserID;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public BaseUser getCreateUserID() {
		return createUserID;
	}

	public void setCreateUserID(BaseUser createUserID) {
		this.createUserID = createUserID;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public BaseUser getUpdateUserID() {
		return updateUserID;
	}

	public void setUpdateUserID(BaseUser updateUserID) {
		this.updateUserID = updateUserID;
	}
}
