package simple.scheduler.quartz.service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import simple.scheduler.quartz.impl.DefaultJob;
import simple.scheduler.quartz.model.BaseJob;

@Service
public class JobsHandlerService implements ApplicationContextAware {

	private static final Logger log = Logger
			.getLogger(JobsHandlerService.class);

	private ApplicationContext applicationContext;

	private Set<SchedulerFactory> schedulerFactorySet = new HashSet<SchedulerFactory>();

	protected Scheduler getScheduler(String schedulerName)
			throws SchedulerException {
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		schedulerFactorySet.add(schedulerFactory);
		initSchedulerFactory(schedulerFactory, schedulerName);
		SchedulerRepository repository = SchedulerRepository.getInstance();
		synchronized (repository) {
			Scheduler existingScheduler = (schedulerName != null ? repository
					.lookup(schedulerName) : null);
			if (existingScheduler != null) {
				return existingScheduler;
			}
			Scheduler newScheduler = schedulerFactory.getScheduler();
			newScheduler.start();
			return newScheduler;
		}
	}

	public boolean checkSchedulerExsit(String schedulerName) {
		try {
			SchedulerRepository repository = SchedulerRepository.getInstance();
			synchronized (repository) {
				Scheduler existingScheduler = (schedulerName != null ? repository
						.lookup(schedulerName) : null);
				if (existingScheduler != null) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * 执行任务线程.
	 */
	public static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";

	/**
	 * 执行任务线程数.
	 */
	public static final int DEFAULT_THREAD_COUNT = 1;

	/**
	 * 初始化调度器工厂.
	 * 
	 * @param schedulerFactory
	 *            调度器工厂
	 * @param schedulerName
	 *            调度器名称
	 * @throws SchedulerException
	 *             调度器异常
	 */
	private void initSchedulerFactory(SchedulerFactory schedulerFactory,
			String schedulerName) throws SchedulerException {

		Properties props = new Properties();
		props.setProperty(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON,
				"true");
		props.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT,
				"true");
		props.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN,
				"true");
		props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS,
				SimpleThreadPool.class.getName());
		props.setProperty(PROP_THREAD_COUNT,
				Integer.toString(DEFAULT_THREAD_COUNT));
		props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, "AUTO");

		props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, schedulerName);

		((StdSchedulerFactory) schedulerFactory).initialize(props);
	}

	/**
	 * 添加任务.
	 * 
	 * @param jobUnit
	 *            任务
	 */
	public void addJob(BaseJob baseJob) throws Exception {

		try {
			Scheduler scheduler = getScheduler(baseJob.getJobCode());

			JobDetail job = new JobDetail(baseJob.getJobCode(),
					Scheduler.DEFAULT_GROUP, DefaultJob.class);
			job.getJobDataMap().put(DefaultJob.ITEM_NAME, baseJob);

			job.getJobDataMap().put("applicationContext",
					this.applicationContext);

			scheduler.addJob(job, true);

			CronTrigger trigger = new CronTrigger(baseJob.getJobCode(),
					Scheduler.DEFAULT_GROUP, baseJob.getJobCode(),
					Scheduler.DEFAULT_GROUP, baseJob.getCronExpr());

			boolean triggerExists = (scheduler.getTrigger(trigger.getName(),
					trigger.getGroup()) != null);

			if (triggerExists) {
				scheduler.rescheduleJob(trigger.getName(), trigger.getGroup(),
						trigger);
			} else {
				scheduler.scheduleJob(trigger);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 删除任务
	 * 
	 * @param name
	 */
	public void deleteJob(String name) throws Exception {
		try {
			Scheduler scheduler = getScheduler(name);
			scheduler.deleteJob(name, Scheduler.DEFAULT_GROUP);
			scheduler.shutdown(false); 
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * .立即执行任务.
	 * 
	 * @param name
	 *            name
	 */
	public void triggerJob(String name) throws Exception {
		try {
			Scheduler scheduler = getScheduler(name);
			scheduler.triggerJob(name, Scheduler.DEFAULT_GROUP);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@PreDestroy
	public void callMeBeforeShutdown() {
		for (SchedulerFactory schedulerFactory : schedulerFactorySet) {
			try {
				schedulerFactory.getScheduler().shutdown(true);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
	} 

}
