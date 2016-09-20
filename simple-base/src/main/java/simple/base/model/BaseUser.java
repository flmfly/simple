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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.base.support.BaseUserResetOperationHandler;
import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.DictField;
import simple.config.annotation.Domain;
import simple.config.annotation.ExtentionPoint;
import simple.config.annotation.Operation;
import simple.config.annotation.OperationParameter;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.SMSSupport;
import simple.config.annotation.TableColumn;
import simple.config.annotation.TreeInfo;
import simple.core.jpa.convert.BooleanToStringConverter;
import simple.core.validation.annotation.FieldCombination;
import simple.core.validation.annotation.UniqueKey;

@Domain(value = "用户管理")
@Entity
@Table(name = "BASE_USER")
@UniqueKey(columnNames = { "account" }, message = "用户名重复！")
@SMSSupport(sender = "simple.sms.service.JuXinSMSSender", messageHandler = "simple.base.support.BaseUserDomainHandler")
@TreeInfo(id = "id", label = "name")
@ExtentionPoint("simple.base.support.BaseUserExtentionPoint")
@Operation(parameters = { @OperationParameter(code = "password", title = "新密码", value = "111111") }, code = "reset", iconStyle = "fa fa-expeditedssl", multi = true, name = "重置密码", handler = BaseUserResetOperationHandler.class)
@SequenceGenerator(name = "SEQ_BASE_USER", sequenceName = "SEQ_BASE_USER")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_USER") })
@FieldCombination(fieldNames = { "id", "password", "password1" }, fieldRules = {
		"2,3", "1" }, message = "密码和确认密码不能为空！")
public class BaseUser implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "ID", type = Number.class, show = false)
	private Long id;

	@Column(name = "ACCOUNT", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 10, title = "用户名", isSearchField = true)
	@TableColumn(title = "用户名", sort = 10)
	@NotNull(message = "用户名不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String account;

	@Column(name = "NAME", length = DataLength.NAME_LENGTH)
	@RepresentationField(sort = 20, title = "姓名", isSearchField = true)
	@TableColumn(title = "姓名", sort = 20)
	@Length(max = DataLength.CODE_LENGTH)
	private String name;

	@ManyToOne
	@JoinColumn(name = "EMP_ID")
	@RepresentationField(sort = 30, title = "所属人员", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(sorts = "30", titles = "所属人员", columns = "name")
	private BaseEmployee employee;

	@ManyToOne
	@JoinColumn(name = "TYPE_ID")
	@RepresentationField(sort = 40, title = "用户类型", view = RepresentationFieldType.SELECT, isSearchField = true)
	@DictField("userType")
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(titles = "用户类型", columns = "name", sorts = "40")
	@NotNull(message = "用户类型不能为空！")
	private BaseDictItem type;

	@Column(name = "PWD", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 50, title = "密码")
	// @NotNull(message = "密码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String password;

	@RepresentationField(sort = 60, title = "确认密码")
	// @NotNull(message = "确认密码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String password1;

	@ManyToOne
	@JoinColumn(name = "STATE_ID")
	@RepresentationField(sort = 70, title = "状态", view = RepresentationFieldType.SELECT, isSearchField = true)
	@DictField("userState")
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(titles = "状态", columns = "name", sorts = "50")
	@NotNull(message = "状态不能为空！")
	private BaseDictItem state;

	@Column(name = "IS_MD5", columnDefinition = "CHAR(1)")
	@RepresentationField(sort = 80, title = "密码是否加密", view = RepresentationFieldType.BOOLEAN, defaultVal = "false")
	@BooleanValue({ "是", "否" })
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean isMD5;

	@Column(name = "LAST_LOGIN_TIME")
	@RepresentationField(sort = 90, title = "最后登录时间", view = RepresentationFieldType.DATETIME, disable = true)
	private Date lastLoginTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BaseEmployee getEmployee() {
		return employee;
	}

	public void setEmployee(BaseEmployee employee) {
		this.employee = employee;
	}

	public BaseDictItem getType() {
		return type;
	}

	public void setType(BaseDictItem type) {
		this.type = type;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public BaseDictItem getState() {
		return state;
	}

	public void setState(BaseDictItem state) {
		this.state = state;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Boolean isMD5() {
		return isMD5;
	}

	public void setIsMD5(Boolean isMD5) {
		this.isMD5 = isMD5;
	}

	// @ManyToMany(fetch = FetchType.LAZY, mappedBy = "users")
	// private Set<BaseRole> roles;

}