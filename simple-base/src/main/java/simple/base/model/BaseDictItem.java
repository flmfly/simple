package simple.base.model;

import java.io.Serializable;

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
import simple.config.annotation.Domain;
import simple.config.annotation.ExtentionPoint;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.RepresentationLayout;
import simple.config.annotation.RepresentationLayoutType;
import simple.config.annotation.Synchronization;
import simple.config.annotation.TableColumn;
import simple.core.validation.annotation.UniqueKey;

@Domain(defaultSort = "sort")
@Synchronization(by = "dict.code", synchronizedFiels = "id,code,name,dict.id,dict.code,dict.name", cache = true)
@Entity
@ExtentionPoint("simple.base.support.BaseDictItemExtentionPoint")
@Table(name = "BASE_DICT_ITEM")
@UniqueKey(columnNames = { "dict", "code" }, message = "编码已存在！")
@RepresentationLayout(view = RepresentationLayoutType.TREE, id = "id", label = "name", pid = "parent.id")
@SequenceGenerator(name = "SEQ_BASE_DICT_ITEM", sequenceName = "SEQ_BASE_DICT_ITEM")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_DICT_ITEM") })
public class BaseDictItem implements Serializable {

	private static final long serialVersionUID = 1181336729294662547L;

	@Id
	@GeneratedValue(generator = "idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "DICT_ID")
	@RepresentationField(sort = 5, title = "分类", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(titles = "字典编码,字典名称", columns = "code,name")
	private BaseDict dict;

	@Column(name = "CODE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 20, title = "编码", isSearchField = true)
	@TableColumn(title = "编码")
	@NotNull(message = "编码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String code;

	@ManyToOne
	@JoinColumn(name = "PARENT_ID")
	@RepresentationField(sort = 10, title = "上级字典项", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name", pid = "parent.id")
	@AssociateTableColumn(sorts = "30", titles = "上级字典项", columns = "name")
	private BaseDictItem parent;

	@Column(name = "NAME", length = DataLength.NAME_LENGTH)
	@RepresentationField(sort = 30, title = "名称", isSearchField = true)
	@TableColumn(title = "名称")
	@NotNull(message = "名称不能为空！")
	@Length(max = DataLength.NAME_LENGTH)
	private String name;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 40, title = "备注", view = RepresentationFieldType.TEXTAREA)
	@TableColumn(title = "备注")
	@Length(max = DataLength.REMARK_LENGTH)
	private String remark;

	@Column(name = "SORT", columnDefinition = "NUMERIC(6,0)")
	@RepresentationField(title = "序号", sort = 30)
	@TableColumn(title = "序号", sort = 30)
	private Integer sort;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BaseDict getDict() {
		return dict;
	}

	public void setDict(BaseDict dict) {
		this.dict = dict;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public BaseDictItem getParent() {
		return parent;
	}

	public void setParent(BaseDictItem parent) {
		this.parent = parent;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}
}