//package simple.scheduler.quartz.service;
//
//import org.apache.log4j.Logger;
//
//import simple.core.service.ISpecialOperationHanler;
//import simple.scheduler.quartz.model.BaseJob;
//
//public class StopJobHandler implements ISpecialOperationHanler<BaseJob>{
//	
//	/** The Constant logger. */
//	static final Logger logger = Logger.getLogger(StopJobHandler.class);
//
//	@Override
//	public void handle(BaseJob baseJob) throws Exception {
//		logger.info(String.format("StopJobHandler start....,baseJob:%s",baseJob.toString()));
//		JobsHandlerService.getInstance().deleteJob(baseJob.getJobCode());
//		logger.info(String.format("StopJobHandler end....baseJobCode:%s",baseJob.getJobCode()));
//		
//	}
//
//}
