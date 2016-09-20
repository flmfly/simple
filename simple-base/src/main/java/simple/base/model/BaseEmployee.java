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

import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.DictField;
import simple.config.annotation.Domain;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.RepresentationLayout;
import simple.config.annotation.RepresentationLayoutType;
import simple.config.annotation.SearchField;
import simple.config.annotation.TableColumn;
import simple.core.jpa.convert.BooleanToStringConverter;
import simple.core.validation.annotation.UniqueKey;

@Domain
@Entity
@Table(name = "BASE_EMPLOYEE")
@UniqueKey(columnNames = { "no" }, message = "员工编号已存在！")
@RepresentationLayout(view = RepresentationLayoutType.TREE, id = "id", label = "name")
@SequenceGenerator(name = "SEQ_BASE_EMPLOYEE", sequenceName = "SEQ_BASE_EMPLOYEE") 
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_EMPLOYEE") }) 
public class BaseEmployee implements Serializable {

	private static final long serialVersionUID = -6355014909623966480L;

	@Id
	@GeneratedValue(generator="idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "ID", show = false)
	private Long id;

	@Column(name = "NO", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 10, title = "员工编号", isSearchField = true)
	@TableColumn(sort = 10, title = "员工编号")
	@NotNull(message = "员工编号不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String no;

	@Column(name = "NAME", length = DataLength.NAME_LENGTH)
	@RepresentationField(sort = 20, title = "姓名", isSearchField = true)
	@TableColumn(sort = 20, title = "姓名")
	@NotNull(message = "姓名不能为空！")
	@Length(max = DataLength.NAME_LENGTH)
	private String name;
	
	@Column(name = "SEX", columnDefinition = "CHAR(1)")
    @RepresentationField(sort = 21, title = "性别", view = RepresentationFieldType.BOOLEAN)
	@BooleanValue({ "男", "女" })
	@TableColumn(title = "性别", sort = 21)
    @Convert(converter = BooleanToStringConverter.class)
	private Boolean sex;
	
	@ManyToOne
	@JoinColumn(name = "POS_ID")
	@RepresentationField(sort = 30, title = "职务", view = RepresentationFieldType.SELECT, isSearchField = true)
	@DictField("position")
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(titles = "职务", columns = "name", sorts = "30")
	private BaseDictItem position;

	@ManyToOne
	@JoinColumn(name = "ORG_ID")
	@RepresentationField(sort = 40, title = "所属组织", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name", pid = "parent.id")
	@AssociateTableColumn(sorts = "40", titles = "所属组织", columns = "name")
	private BaseOrg org;

	@Column(name = "MOBILE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 50, title = "手机")
	@TableColumn(sort = 50, title = "手机")
	@Length(max = DataLength.CODE_LENGTH)
	private String mobile;

	@Column(name = "PHONE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 60, title = "电话")
	@TableColumn(sort = 60, title = "电话")
	@Length(max = DataLength.CODE_LENGTH)
	private String phone;

	@Column(name = "EMAIL", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 70, title = "邮箱")
	@TableColumn(sort = 70, title = "邮箱")
	@Length(max = DataLength.CODE_LENGTH)
	private String email;
	
	@Column(name = "BIRTH_DAY")
	@RepresentationField(title = "出生日期", view = RepresentationFieldType.DATE, sort = 80)
	@TableColumn(title = "出生日期", sort = 80)
	@SearchField(isRange = true)
	private Date birthDay;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 90, title = "备注", view = RepresentationFieldType.TEXTAREA)
	@TableColumn(title = "备注")
	@Length(max = DataLength.REMARK_LENGTH)
	private String remark;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BaseDictItem getPosition() {
		return position;
	}

	public void setPosition(BaseDictItem position) {
		this.position = position;
	}

	public BaseOrg getOrg() {
		return org;
	}

	public void setOrg(BaseOrg org) {
		this.org = org;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(Date birthDay) {
		this.birthDay = birthDay;
	}

	public Boolean getSex() {
		return sex;
	}

	public void setSex(Boolean sex) {
		this.sex = sex;
	}

}