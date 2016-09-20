//package simple.scheduler.quartz.service;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import simple.core.service.ISpecialOperationHanler;
//import simple.scheduler.quartz.model.BaseJob;
//
//public class StartJobHandler implements ISpecialOperationHanler<BaseJob>{
//
//	private static final Logger logger = Logger.getLogger(StartJobHandler.class);
//	
//	@Autowired
//	private BaseJobService baseJobService;
//	
//	public static final String PARAM_DELIMITER = "~~";
//	@Override
//	public void handle(BaseJob baseJob) throws Exception {
//		if (StringUtils.isBlank(baseJob.getCronExpr())) {
//			throw new Exception("no such cron can do.");
//		}
//		
//		logger.info(String.format("start baseJob:%s", baseJob.toString()));
//		JobsHandler.getInstance().addJob(baseJob);
//		
//		logger.info(String.format("end baseJob:%s", baseJob.toString()));
//	}
//
//}
