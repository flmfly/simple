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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import simple.base.annotation.support.LoginUserAutoFillHandler;
import simple.base.annotation.support.LoginUserDefaultValueHandler;
import simple.base.model.BaseDictItem;
import simple.base.model.BaseUser;
import simple.config.annotation.AssociateTableColumn;
import simple.config.annotation.AutoFill;
import simple.config.annotation.AutoFillTrigger;
import simple.config.annotation.BooleanValue;
import simple.config.annotation.DataLength;
import simple.config.annotation.DefaultValue;
import simple.config.annotation.DictField;
import simple.config.annotation.Domain;
import simple.config.annotation.ExtentionPoint;
import simple.config.annotation.Operation;
import simple.config.annotation.Reference;
import simple.config.annotation.ReferenceType;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.RepresentationFieldType;
import simple.config.annotation.SearchField;
import simple.config.annotation.TableColumn;
import simple.config.annotation.support.CurrentDateTimeAutoFillHandler;
import simple.config.annotation.support.DateTimeDefaultValueHandler;
import simple.core.jpa.convert.BooleanToStringConverter;
import simple.core.validation.annotation.UniqueKey;
import simple.jobs.model.JobHandler;
import simple.scheduler.quartz.support.BaseJobRunningHandler;

@Domain
@Entity
@Table(name = "BASE_JOB")
@UniqueKey(columnNames = {"jobCode"}, message = "该JOB重复！")
@ExtentionPoint("simple.scheduler.quartz.service.BaseJobExtention")
@Operation.List({
	@Operation(code = "run", iconStyle = "fa fa-play", multi = false, handler = BaseJobRunningHandler.class, name = "立即执行")
       })
@SequenceGenerator(name = "SEQ_BASE_JOB", sequenceName = "SEQ_BASE_JOB")
@GenericGenerator(name = "idStrategy", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_BASE_JOB") })
public class BaseJob implements Serializable{

	 private static final long serialVersionUID = -1989290321525114027L;
	 
	 private static final int DEFAULT_DATA_LENGTH=1000;

	    @Id
	    @GeneratedValue(generator = "idStrategy") 
		@Column(name = "ID")
		@RepresentationField(view = RepresentationFieldType.HIDDEN)
		@TableColumn(title = "id", show = false)
		private Long id;
	    
	    @Column(name = "JOB_CODE", length =DataLength.NAME_LENGTH)
		@RepresentationField(title = "JOB编码", sort = 10)
	    @SearchField(canFuzzy=true)
		@TableColumn(title = "JOB编码", sort = 10)
	    @NotNull(message = "JOB编码不能为空！")
		@Length(max =DataLength.NAME_LENGTH)
		private String jobCode;
	    
	    @Column(name = "NAME", length =DataLength.NAME_LENGTH)
		@RepresentationField(title = "JOB名称", sort = 20)
	    @SearchField(canFuzzy=true)
		@TableColumn(title = "JOB名称", sort = 20)
	    @NotNull(message = "JOB名称不能为空！")
		@Length(max =DataLength.NAME_LENGTH)
		private String name; 
	    
	    
	    @Column(name = "CRON_EXPR", length =DataLength.NAME_LENGTH)
		@RepresentationField(title = "Cron表达式", sort = 30)
		@TableColumn(title = "Cron表达式", sort = 30)
	    @NotNull(message = "Cron表达式不能为空！")
		@Length(max =DataLength.NAME_LENGTH)
		private String cronExpr;
	    
	    @ManyToOne
		@JoinColumn(name = "JOB_TYPE_ID")
		@RepresentationField(sort = 40, title = "JOB类型", view = RepresentationFieldType.SELECT, isSearchField = true)
		@DictField("jobType")
		@Reference(id = "id", label = "name")
		@AssociateTableColumn(sorts = "40", titles = "JOB类型", columns = "name")
		@NotNull(message = "JOB类型不能为空！")
		private BaseDictItem jobType;
	    
	    
	    @ManyToOne
		@JoinColumn(name = "JOB_Handler_ID")
		@RepresentationField(sort = 50, title = "JOB处理类", view = RepresentationFieldType.REFERENCE, isSearchField = true)
	    @Reference(id = "id", label = "jobHandlerDesc", type = ReferenceType.SINGLE_QUERY, viewFields = "methodParamDesc")
		@AssociateTableColumn(sorts = "50", titles = "JOB处理类", columns = "jobHandlerDesc")
		@NotNull(message = "JOB处理类不能为空！")
		private JobHandler jobHandler;
	    
	    
		@Column(name = "METHOD_PARAM", length =DEFAULT_DATA_LENGTH)
		@RepresentationField(title = "方法参数", sort = 60, view = RepresentationFieldType.TEXTAREA)
		@TableColumn(title = "方法参数", sort = 60)
		@Length(max =DEFAULT_DATA_LENGTH)
		private String methodParam;
		
		@Column(name = "STATE", columnDefinition = "CHAR(1)")
	    @RepresentationField(sort = 70, title = "状态", isSearchField = true, view = RepresentationFieldType.BOOLEAN)
		@BooleanValue({ "启用", "禁用" })
		@TableColumn(title = "状态", sort = 70)
		@NotNull(message = "状态不能为空！")
		@Convert(converter = BooleanToStringConverter.class)
		private Boolean state;
	    
	    @Column(name = "SUCC_NOTIFY", columnDefinition = "CHAR(1)")
	    @RepresentationField(sort = 80, title = "成功通知", isSearchField = true, view = RepresentationFieldType.BOOLEAN)
		@BooleanValue({ "启用", "禁用" })
		@TableColumn(title = "成功通知", sort = 80)
		@NotNull(message = "成功通知不能为空！")
		@Convert(converter = BooleanToStringConverter.class)
		private Boolean succNotify;
	    
	    @Column(name = "OBSERVER", length =DataLength.REMARK_LENGTH)
		@RepresentationField(title = "观察者", sort = 90)
		@TableColumn(title = "观察者", sort = 90)
		@Length(max =DataLength.REMARK_LENGTH)
		private String observer;
	    
		@Column(name = "REMARK", length =DataLength.REMARK_LENGTH)
		@RepresentationField(title = "备注", sort = 100, view = RepresentationFieldType.TEXTAREA)
		@TableColumn(title = "备注", sort = 100)
		@Length(max =DataLength.REMARK_LENGTH)
		private String remark;

		@Column(name = "CREATE_TIME")
		@RepresentationField(title = "创建时间", sort = 150, view = RepresentationFieldType.DATETIME, disable = true)
		@DefaultValue(handler = DateTimeDefaultValueHandler.class)
		private Date createTime;

		@ManyToOne
		@JoinColumn(name = "CREATE_USER_ID")
		@RepresentationField(title = "创建人", sort = 160, view = RepresentationFieldType.REFERENCE, disable = true)
		@Reference(id = "id", label = "account")
		@DefaultValue(handler = LoginUserDefaultValueHandler.class)
		private BaseUser createUserID;

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

		public String getJobCode() {
			return jobCode;
		}

		public void setJobCode(String jobCode) {
			this.jobCode = jobCode;
		}


		public String getCronExpr() {
			return cronExpr;
		}

		public void setCronExpr(String cronExpr) {
			this.cronExpr = cronExpr;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public Boolean getSuccNotify() {
			return succNotify;
		}

		public void setSuccNotify(Boolean succNotify) {
			this.succNotify = succNotify;
		}

		public String getObserver() {
			return observer;
		}

		public void setObserver(String observer) {
			this.observer = observer;
		}


		public Boolean getState() {
			return state;
		}

		public void setState(Boolean state) {
			this.state = state;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public BaseDictItem getJobType() {
			return jobType;
		}

		public void setJobType(BaseDictItem jobType) {
			this.jobType = jobType;
		}

		public JobHandler getJobHandler() {
			return jobHandler;
		}

		public void setJobHandler(JobHandler jobHandler) {
			this.jobHandler = jobHandler;
		}

		public String getMethodParam() {
			return methodParam;
		}

		public void setMethodParam(String methodParam) {
			this.methodParam = methodParam;
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

		@Override
		public String toString() {
			return "BaseJob [id=" + id + ", jobCode=" + jobCode + ", name="
					+ name + ", cronExpr=" + cronExpr + ", jobType=" + jobType
					+ ", succNotify=" + succNotify + ", observer=" + observer
					+ ", state=" + state + ", jobHandler=" + jobHandler
					+ ", methodParam=" + methodParam + ", remark=" + remark
					+ "]";
		} 

		

		
	    
}
