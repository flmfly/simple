package simple.jobs.model;

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

import simple.base.annotation.support.LoginUserAutoFillHandler;
import simple.base.model.BaseUser;
import simple.config.annotation.AutoFill;
import simple.config.annotation.AutoFillTrigger;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.Domain;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.TableColumn;
import simple.config.annotation.support.CurrentDateTimeAutoFillHandler;
import simple.core.jpa.convert.BooleanToStringConverter;
import simple.core.validation.annotation.UniqueKey;

@Domain
@Entity
@Table(name = "JOB_HANDLER")
@UniqueKey(columnNames = {"jobHandlerPath"}, message = "Job的实现类路径不能重复！")
@SequenceGenerator(name = "SEQ_JOB_HANDLER", sequenceName = "SEQ_JOB_HANDLER")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_JOB_HANDLER") })
public class JobHandler implements Serializable{
	
	private static final long serialVersionUID = -1989290321525414027L;
	
	
	private static final int LONG_LENGTH=1000;
	
	@Id
	@GeneratedValue(generator = "idStrategy") 
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", show = false)
	private Long id;
	
	
	@Column(name = "JOB_HANDLER_PATH", length =DataLength.LONG_TEXT_LENGTH) 
	@RepresentationField(title = "JOBHandler路径", sort = 10,isSearchField = true)
	@TableColumn(title = "JOBHandler路径", sort = 10)
    @NotNull(message = "JOBHandler路径不能为空！")
	@Length(max =DataLength.LONG_TEXT_LENGTH)
	private String jobHandlerPath;
	
	
	@Column(name = "NAME", length =DataLength.LONG_TEXT_LENGTH)
	@RepresentationField(title = "JOBHandler名称", sort = 20)
	@TableColumn(title = "JOBHandler名称", sort = 20)
    @NotNull(message = "JOBHandler名称不能为空！")
	@Length(max =DataLength.LONG_TEXT_LENGTH)
	private String name;
	
	
	@Column(name = "JOB_HANDLER_DESC", length =DataLength.LONG_TEXT_LENGTH)
	@RepresentationField(title = "handler描述", sort = 20,isSearchField = true)
	@TableColumn(title = "JOBHandler描述", sort = 20)
    @NotNull(message = "JOBHandler描述不能为空！")
	@Length(max =DataLength.NAME_LENGTH)
	private String jobHandlerDesc;
	
	
	@Column(name = "METHOD_PARAM_DESC", length =LONG_LENGTH)
	@RepresentationField(title = "方法参数描述", sort = 20)
	@TableColumn(title = "方法参数描述", sort = 20)
	@Length(max =LONG_LENGTH)
	private String methodParamDesc;
	
	@Column(name = "REMARK", length =DataLength.REMARK_LENGTH)
	@RepresentationField(title = "备注", sort = 40)
	@TableColumn(title = "备注", sort = 40)
	@Length(max =DataLength.REMARK_LENGTH)
	private String remark;
	

    @Column(name = "STATE", columnDefinition = "CHAR(1)")
    @RepresentationField(sort = 30, title = "状态", isSearchField = true, view = RepresentationFieldType.BOOLEAN)
	@BooleanValue({ "启用", "禁用" })
	@TableColumn(title = "状态", sort = 30)
	@NotNull(message = "状态不能为空！")
    @Convert(converter = BooleanToStringConverter.class)
	private Boolean state;
    
    
    @Column(name = "UPDATE_TIME")
	@RepresentationField(title = "最后修改时间", sort = 170, view = RepresentationFieldType.DATETIME, disable = true)
	@AutoFill(handler = CurrentDateTimeAutoFillHandler.class, trigger = AutoFillTrigger.ALWAYS)
	private Date updateTime;

	@ManyToOne
	@JoinColumn(name = "UPDATE_USER_ID")
	@RepresentationField(title = "最后修改人", sort = 180, view = RepresentationFieldType.REFERENCE, disable = true)
	@AutoFill(handler = LoginUserAutoFillHandler.class, trigger = AutoFillTrigger.ALWAYS)
	@Reference(id = "id", label = "account")
	private BaseUser updateUserID;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobHandlerPath() {
		return jobHandlerPath;
	}

	public void setJobHandlerPath(String jobHandlerPath) {
		this.jobHandlerPath = jobHandlerPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJobHandlerDesc() {
		return jobHandlerDesc;
	}

	public void setJobHandlerDesc(String jobHandlerDesc) {
		this.jobHandlerDesc = jobHandlerDesc;
	}

	public String getMethodParamDesc() {
		return methodParamDesc;
	}

	public void setMethodParamDesc(String methodParamDesc) {
		this.methodParamDesc = methodParamDesc;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
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
