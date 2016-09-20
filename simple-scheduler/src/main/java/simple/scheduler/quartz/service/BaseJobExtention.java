package simple.scheduler.quartz.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.config.annotation.support.ExtentionPoint;
import simple.scheduler.quartz.model.BaseJob;

@Component
public class BaseJobExtention implements ExtentionPoint {

	Logger logger = Logger.getLogger(BaseJobExtention.class);

	@Autowired
	private JobsHandlerService jobsHandlerService;

	@Autowired
	private BaseJobService baseJobService;

	@Override
	public void beforeSave(Object entity) {

	}

	@Override
	public void afterSave(Object entity) {
		BaseJob baseJob = (BaseJob) entity;
		if (null == baseJob.getUpdateTime() && baseJob.getState()) {
			try {
				jobsHandlerService.addJob(baseJob);
				logger.info("create,addjob,baseJob:" + baseJob.toString());
				return;
			} catch (Exception e) {
				logger.error("add Job error,baseJob:" + baseJob.toString());
				e.printStackTrace();
			}
		}
		boolean falg = jobsHandlerService.checkSchedulerExsit(baseJob
				.getJobCode());
		if (!baseJob.getState() && falg) {
			try {
				jobsHandlerService.deleteJob(baseJob.getJobCode());
				logger.info("deleteJob,baseJob:" + baseJob.toString());
				return;
			} catch (Exception e) {
				logger.error("delete job error,baseJob:" + baseJob.toString());
			}
		}
		if (baseJob.getState()) {
			try {
				jobsHandlerService.addJob(baseJob);
				logger.info("addjob,baseJob:" + baseJob.toString());
				return;
			} catch (Exception e) {
				logger.error("add job error,baseJob:" + baseJob.toString());
			}
		}

	}

	@Override
	public void beforeFetch(Object entity) {

	}

	@Override
	public void afterFetch(Object entity) {

	}

	@Override
	public void beforeDelete(Object entity) {
		BaseJob baseJob = (BaseJob) entity;
		try {
			jobsHandlerService.deleteJob(baseJob.getJobCode());
			logger.info("deleteJob,baseJob:" + baseJob.toString());
		} catch (Exception e) {
			logger.error("delete job error,baseJob:" + baseJob.toString());
		} 
	}

	@Override
	public void afterDelete(Object entity) {

	}

}
