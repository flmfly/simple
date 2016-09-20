package simple.base.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.config.annotation.DataLength;
import simple.config.annotation.Domain;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.TableColumn;
import simple.core.validation.annotation.UniqueKey;

@Domain
@Entity
@Table(name = "BASE_RESOURCE")
@UniqueKey(columnNames = { "code" }, message = "编码已存在！")
@SequenceGenerator(name = "SEQ_BASE_RESOURCE", sequenceName = "SEQ_BASE_RESOURCE") 
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_RESOURCE") }) 
public class BaseResource implements Serializable {

	private static final long serialVersionUID = -5417250117391305303L;

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
	@RepresentationField(sort = 20, title = "名称", isSearchField = true)
	@TableColumn(title = "名称")
	@NotNull(message = "名称不能为空！")
	@Length(max = DataLength.NAME_LENGTH)
	private String name;

	@Column(name = "URI", length = DataLength.LONG_TEXT_LENGTH)
	@RepresentationField(sort = 30, title = "资源位置")
	@TableColumn(title = "资源位置")
	@NotNull(message = "资源位置不能为空！")
	@Length(max = DataLength.LONG_TEXT_LENGTH)
	private String uri;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 40, title = "备注", view = RepresentationFieldType.TEXTAREA)
	@TableColumn(title = "备注")
	@Length(max = DataLength.REMARK_LENGTH)
	private String remark;

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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
