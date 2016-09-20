package simple.base.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.Domain;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.RepresentationLayout;
import simple.config.annotation.RepresentationLayoutType;
import simple.config.annotation.TableColumn;
import simple.config.annotation.TreeInfo;
import simple.core.jpa.convert.BooleanToStringConverter;
import simple.core.validation.annotation.UniqueKey;

@Domain(defaultSort="sort")
@Entity
@Table(name = "BASE_MENU")
@UniqueKey(columnNames = { "code" }, message = "编码已存在！")
@TreeInfo(id = "id", label = "name", pid = "parent.id")
@RepresentationLayout(view = RepresentationLayoutType.TREE, id = "id", label = "name", pid = "parent.id")
@SequenceGenerator(name = "SEQ_BASE_MENU", sequenceName = "SEQ_BASE_MENU")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_MENU") })
public class BaseMenu implements Serializable {

	private static final long serialVersionUID = -5417250117391305303L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false, sort = 10)
	private Long id;

	@Column(name = "CODE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 10, title = "编码", isSearchField = true)
	@TableColumn(title = "编码")
	@NotNull(message = "编码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String code;

	@Column(name = "NAME", length = DataLength.NAME_LENGTH)
	@RepresentationField(sort = 20, title = "名称", isSearchField = true)
	@TableColumn(title = "名称")
	@NotNull(message = "名称不能为空！")
	@Length(max = DataLength.NAME_LENGTH)
	private String name;

	@ManyToOne
	@JoinColumn(name = "RESOURCE_ID")
	@RepresentationField(sort = 30, title = "资源", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(titles = "资源名称,资源地址", columns = "name,uri", sorts = "50,60")
	private BaseResource resource;

	@ManyToOne
	@JoinColumn(name = "PARENT_ID")
	@RepresentationField(sort = 40, title = "上级菜单", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name", pid = "parent.id")
	@AssociateTableColumn(sorts = "40", titles = "上级菜单", columns = "name")
	private BaseMenu parent;

	@Column(name = "ICON_CSS", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 50, title = "图标样式")
	@Length(max = DataLength.CODE_LENGTH)
	private String iconCss;

	@Column(name = "SORT", columnDefinition="NUMERIC(4,0)")
	@RepresentationField(sort = 60, title = "排序")
	@DecimalMax(value = "9999")
	private Integer sort;

	@Column(name = "HIDDEN", columnDefinition = "CHAR(1)")
	@RepresentationField(sort = 65, title = "是否隐藏", view = RepresentationFieldType.BOOLEAN)
	@BooleanValue({ "是", "否" })
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean hidden;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 70, title = "备注", view = RepresentationFieldType.TEXTAREA)
	@TableColumn(title = "备注")
	@Length(max = DataLength.REMARK_LENGTH)
	private String remark;

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

	public String getIconCss() {
		return iconCss;
	}

	public void setIconCss(String iconCss) {
		this.iconCss = iconCss;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public BaseMenu getParent() {
		return parent;
	}

	public void setParent(BaseMenu parent) {
		this.parent = parent;
	}

	public BaseResource getResource() {
		return resource;
	}

	public void setResource(BaseResource resource) {
		this.resource = resource;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

}
