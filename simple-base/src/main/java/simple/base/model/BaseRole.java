package simple.base.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
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
import simple.config.annotation.TabView;
import simple.config.annotation.TabViewType;
import simple.config.annotation.TableColumn;
import simple.core.jpa.convert.BooleanToStringConverter;
import simple.core.validation.annotation.UniqueKey;

@Domain
@Entity
@Table(name = "BASE_ROLE")
@UniqueKey(columnNames = { "code" }, message = "编码已存在！")
@SequenceGenerator(name = "SEQ_BASE_ROLE", sequenceName = "SEQ_BASE_ROLE") 
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_ROLE") }) 
public class BaseRole implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	private Long id;

	@Column(name = "CODE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 10, title = "编码", isSearchField = true)
	@TableColumn(title = "编码", sort = 10)
	@NotNull(message = "编码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String code;

	@Column(name = "NAME", length = DataLength.NAME_LENGTH)
	@RepresentationField(sort = 20, title = "名称", isSearchField = true)
	@TableColumn(title = "名称", sort = 20)
	@NotNull(message = "名称不能为空！")
	@Length(max = DataLength.NAME_LENGTH)
	private String name;

	@Column(name = "STATE", columnDefinition = "CHAR(1)")
	@RepresentationField(sort = 30, title = "状态", isSearchField = true, view = RepresentationFieldType.BOOLEAN)
	@BooleanValue({ "启用", "禁用" })
	@TableColumn(title = "状态", sort = 30)
	@NotNull(message = "状态不能为空！")
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean state;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 40, title = "备注", view = RepresentationFieldType.TEXTAREA)
	@Length(max = DataLength.REMARK_LENGTH)
	private String remark;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "BASE_ROLE_USER", joinColumns = { @JoinColumn(name = "ROLE_ID", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "USER_ID", nullable = false, updatable = false) })
	@OrderBy("id ASC")
	@RepresentationField(view = RepresentationFieldType.TAB, title = "用户")
	@TabView(TabViewType.SELECT_LIST)
	private Set<BaseUser> users;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "BASE_ROLE_MENU", joinColumns = { @JoinColumn(name = "ROLE_ID", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "MENU_ID", nullable = false, updatable = false) })
	@RepresentationField(view = RepresentationFieldType.TAB, title = "菜单")
	@TabView(TabViewType.SELECT_LIST)
	@OrderBy("parent ASC")
	private Set<BaseMenu> menus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Set<BaseUser> getUsers() {
		return users;
	}

	public void setUsers(Set<BaseUser> users) {
		this.users = users;
	}

	public Set<BaseMenu> getMenus() {
		return menus;
	}

	public void setMenus(Set<BaseMenu> menus) {
		this.menus = menus;
	}

}