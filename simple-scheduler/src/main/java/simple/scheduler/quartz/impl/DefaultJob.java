package simple.scheduler.quartz.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import simple.jobs.actuator.MethodActuator;
import simple.notice.NoticeCallback;
import simple.notice.NoticeMessage;
import simple.scheduler.quartz.NotRecordLogException;
import simple.scheduler.quartz.PartSuccessfulException;
import simple.scheduler.quartz.model.BaseJob;
import simple.scheduler.quartz.model.BaseJobLog;
import simple.scheduler.quartz.service.BaseJobLogService;
import simple.scheduler.quartz.service.MailNoticeService;

/**
 * 
 */
@Service
public class DefaultJob implements Job {

	private static final Logger log = Logger.getLogger(DefaultJob.class);

	public static final String ITEM_NAME = "_basejob&item";

	public static final String NEWLINE = "\r\n";

	private ApplicationContext applicationContext;

	@Override
	public void execute(final JobExecutionContext ctx)
			throws JobExecutionException {

		JobDataMap data = ctx.getJobDetail().getJobDataMap();

		BaseJob baseJob = (BaseJob) data.get(ITEM_NAME);

		this.applicationContext = (ApplicationContext) data
				.get("applicationContext");

		String jobName = baseJob.getName();
		Boolean succNotify = baseJob.getSuccNotify();
		String observer = baseJob.getObserver();

		final NoticeMessage message = new NoticeMessage();
		message.setName(jobName);
		boolean hasObserver = StringUtils.isNotBlank(observer);
		try {
			call(ctx, message);
		} catch (Exception e) {
			throw new JobExecutionException(e);
		} finally {
			if (!hasObserver) {
				return;
			}
			if ((succNotify != null && succNotify) // 成功后通知
					|| message.hasError()) { // 异常通知
				try {
					message.setDone(true);
					NoticeCallback notice = new MailNoticeService(
							applicationContext);
					notice.setObserver(observer);
					notice.notify(message);
				} catch (Exception e) {
					throw new JobExecutionException(e);
				}
			}
		}

	}

	public void call(JobExecutionContext ctx, NoticeMessage message) {
		String jobCode = ctx.getJobDetail().getKey().getName();
		JobDataMap data = ctx.getJobDetail().getJobDataMap();
		log.info("Job " + jobCode + " is about to be executed.");
		BaseJobLog baseJobLog = new BaseJobLog();
		baseJobLog.setStartTime(new Date());
		boolean success = false;
		boolean failure = false;
		boolean partsuc = false;
		boolean notRecordLog = false;
		try {
			BaseJob baseJob = (BaseJob) data.get(ITEM_NAME);

			baseJobLog.setBaseJobId(baseJob);
			try {
				String params = baseJob.getMethodParam() != null ? baseJob
						.getMethodParam() : "";
				log.info("execute " + baseJob.getJobCode() + " ...");
				MethodActuator methodActuator = new MethodActuator(baseJob
						.getJobType().getCode(), applicationContext);
				Object obj = methodActuator.execute(baseJob.getJobHandler()
						.getJobHandlerPath(), params);
				if (obj != null) {
					log.info(obj.toString());
				}
				success = true;
				message.plusSuccess();
			} catch (Exception e) {
				Throwable cause = e;
				while (cause.getCause() != null) {
					cause = cause.getCause();
				}
				if (cause instanceof NotRecordLogException) {
					notRecordLog = true;
					return;
				}
				message.plusFailure();
				if (cause instanceof PartSuccessfulException) {
					partsuc = true;
				} else {
					failure = true;
					log.error(cause.getMessage(), cause);
				}

				message.appendFailureMessage(NEWLINE).append("Exception: ")
						.append(NEWLINE)
						.append(baseJob.getJobHandler().getJobHandlerPath())
						.append("-->")
						.append(ExceptionUtils.getRootCauseMessage(cause))
						.append(NEWLINE)
						.append(ExceptionUtils.getFullStackTrace(cause))
						.append(NEWLINE).append(NEWLINE);
			}
		} finally {
			if (notRecordLog) {
				return;
			}
			log.info("Job " + ctx.getJobDetail().getKey().getName()
					+ " was executed.");
			message.setEnd(new Date());
			boolean state = false;
			if (success && failure || partsuc) {
				state = false;
			} else if (success && !failure) {
				state = true;
			}
			try {
				baseJobLog.setEndTime(new Date());
				baseJobLog.setIsComplete(state);
				baseJobLog
						.setErrorInfo(message.getFailureMessage().length() >= 2000 ? (message
								.getFailureMessage().substring(0, 2000) + "...")
								: message.getFailureMessage());
				baseJobLog.setExecResult(state ? "success" : "failed");
				applicationContext.getBean(BaseJobLogService.class).save(
						baseJobLog);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

}
