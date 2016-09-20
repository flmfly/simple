package simple.scheduler.quartz.impl;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 *
 */
public class DefaultJobListener implements JobListener {
	
	private static final Logger log = Logger.getLogger(DefaultJobListener.class);
	
	
	private String name;
	
	public DefaultJobListener(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

    public void jobToBeExecuted(JobExecutionContext inContext) {
		log.info("Job " + inContext.getJobDetail().getKey().getName() + " is about to be executed.");
	}

	public void jobExecutionVetoed(JobExecutionContext inContext) {
		log.info("Job " + inContext.getJobDetail().getKey().getName() + " execution was vetoed.");
	}

	public void jobWasExecuted(JobExecutionContext inContext, JobExecutionException inException) {
		log.info("Job " + inContext.getJobDetail().getKey().getName() + " was executed.");
	}

}
