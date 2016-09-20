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
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.RepresentationLayout;
import simple.config.annotation.RepresentationLayoutType;
import simple.config.annotation.TableColumn;
import simple.core.validation.annotation.UniqueKey;

@Domain
@Entity
@Table(name = "BASE_ORG")
@UniqueKey(columnNames = { "code" }, message="编码已存在！")
@RepresentationLayout(view = RepresentationLayoutType.TREE, id = "id", label = "name", pid = "parent.id")
@SequenceGenerator(name = "SEQ_BASE_ORG", sequenceName = "SEQ_BASE_ORG") 
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_ORG") }) 
public class BaseOrg implements Serializable {

	private static final long serialVersionUID = -235956001668574740L;

	@Id
	@GeneratedValue(generator="idStrategy")
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", type = Number.class, show = false)
	private Long id;

	@Column(name = "CODE", length = DataLength.CODE_LENGTH)
	@RepresentationField(sort = 10, title = "编码", isSearchField = true)
	@TableColumn(title = "编码")
	@NotNull(message = "编码不能为空！")
	@Length(max = DataLength.CODE_LENGTH)
	private String code;

	@Column(name = "NAME", length = DataLength.NAME_LENGTH)
	@RepresentationField(sort = 30, title = "名称", isSearchField = true)
	@TableColumn(title = "名称")
	@NotNull(message = "名称不能为空！")
	@Length(max = DataLength.NAME_LENGTH)
	private String name;

	@ManyToOne
	@JoinColumn(name = "PARENT_ID")
	@RepresentationField(sort = 20, title = "上级组织", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name", pid = "parent.id")
	@AssociateTableColumn(sorts = "30", titles = "上级组织", columns = "name")
	private BaseOrg parent;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 40, title = "备注", view = RepresentationFieldType.TEXTAREA)
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

	public BaseOrg getParent() {
		return parent;
	}

	public void setParent(BaseOrg parent) {
		this.parent = parent;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	

}