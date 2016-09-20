package simple.scheduler.quartz.model;

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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.base.annotation.support.LoginUserAutoFillHandler;
import simple.base.model.BaseUser;
import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.AutoFill;
import simple.config.annotation.AutoFillTrigger;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.Domain;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.SearchField;
import simple.config.annotation.TableColumn;
import simple.config.annotation.support.CurrentDateTimeAutoFillHandler;
import simple.core.jpa.convert.BooleanToStringConverter;

@Domain(value="任务日志",defaultSort="-startTime")
@Entity
@Table(name = "BASE_JOB_LOG")
@SequenceGenerator(name = "SEQ_BASE_JOB_LOG", sequenceName = "SEQ_BASE_JOB_LOG")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_JOB_LOG") })
public class BaseJobLog implements Serializable {

	private static final long serialVersionUID = -1989291329520114027L;

	private static final int ESPECIAL_LENGTH = 3800;

	@Id
	@GeneratedValue(generator = "idStrategy") 
	@Column(name = "ID")
	@RepresentationField(view = RepresentationFieldType.HIDDEN)
	@TableColumn(title = "id", show = false)
	private Long id;

	@Column(name = "IS_COMPLETE", columnDefinition = "CHAR(1)")
	@RepresentationField(title = "是否完成", sort = 20)
	@TableColumn(title = "是否完成", sort = 20)
	@BooleanValue({ "完成", "未完成" })
	@Length(max = DataLength.SHORT_NAME_LENGTH)
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean isComplete;

	@Column(name = "EXEC_RESULT", length = DataLength.SHORT_NAME_LENGTH)
	@RepresentationField(title = "执行结果", sort = 10, isSearchField = true)
	@TableColumn(title = "执行结果", sort = 10)
	@Length(max = DataLength.SHORT_NAME_LENGTH)
	private String execResult;

	@Column(name = "START_TIME")
	@RepresentationField(title = "开始时间", sort = 30, view = RepresentationFieldType.DATETIME)
	@SearchField(isRange=true)
	@TableColumn(title = "开始时间", sort = 30)
	private Date startTime;

	@Column(name = "END_TIME")
	@RepresentationField(title = "结束时间", sort = 40, view = RepresentationFieldType.DATETIME)
	@TableColumn(title = "结束时间", sort = 40)
	private Date endTime;

	@Column(name = "ERROR_INFO", length = ESPECIAL_LENGTH)
	@RepresentationField(title = "错误信息", sort = 50)
	@TableColumn(title = "错误信息", sort = 50)
	@Length(max = ESPECIAL_LENGTH)
	private String errorInfo;

	@Column(name = "REMARK", length = DataLength.REMARK_LENGTH)
	@RepresentationField(sort = 60, title = "备注", view = RepresentationFieldType.TEXTAREA)
	@TableColumn(title = "备注", sort = 60)
	@Length(max = DataLength.CODE_LENGTH)
	private String remark;

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

	@ManyToOne
	@JoinColumn(name = "BASE_JOB_ID")
	@RepresentationField(title = "job名称", sort = 9, view = RepresentationFieldType.REFERENCE, isSearchField = true)
	@Reference(id = "id", label = "name")
	@AssociateTableColumn(sorts = "9", titles = "job名称", columns = "name")
	private BaseJob baseJobId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(Boolean isComplete) {
		this.isComplete = isComplete;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getExecResult() {
		return execResult;
	}

	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public BaseJob getBaseJobId() {
		return baseJobId;
	}

	public void setBaseJobId(BaseJob baseJobId) {
		this.baseJobId = baseJobId;
	}

}
