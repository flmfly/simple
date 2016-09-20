package simple.scheduler.quartz.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.core.service.BaseService;
import simple.scheduler.quartz.model.BaseJob;


/**
 *
 */
@Service
public class BaseJobService implements InitializingBean{
	
   @Autowired
   private BaseService baseService;
   
   @Autowired
   private  JobsHandlerService  jobsHandler;
   
   
	private static final Logger logger = Logger.getLogger(BaseJobService.class);

	
	public Long getBaseJobIdByJobCode(String jobCode) throws Exception{
		return findUniqueBaseJobByJobCode(null,jobCode).getId();
	}
	
	/**
	 * 通过状态标示获取任务列表。
	 * @return List<BaseJob>
	 * @throws Exception 
	 */
	public List<BaseJob> dealAllJobs(Boolean state) throws Exception {
	
		List<Criterion> criterions = new ArrayList<Criterion>();
		if(null !=state){
		criterions.add(Restrictions.eq("state", true));
		}
		List<BaseJob> jobs = baseService.find(BaseJob.class, criterions,null);
		if (!jobs.isEmpty()) {
			return jobs;
		}
		return null;
	}
	/**
	 * 获取单个任务
	 * @throws Exception
	 */
	public BaseJob findUniqueBaseJobByJobCode(Boolean state,String jobCode) throws Exception{
		List<BaseJob> baseJobList = dealAllJobs(state);
		if(!baseJobList.isEmpty()){
			return baseJobList.iterator().next();
		}
		return null;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		initAllJob();
	}

	private void initAllJob() {
		
		logger.info("BaseJob init start ...");

		List<BaseJob> list = new ArrayList<BaseJob>();
		try {
			list = this.dealAllJobs(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null != list && list.size() !=0){
		for (BaseJob baseJob : list) {
			if (StringUtils.isBlank(baseJob.getCronExpr())) {
				continue;
			}
			if (!baseJob.getState() || StringUtils.isBlank(baseJob.getCronExpr())) {
				baseJob.setCronExpr("0 0 0 1 1 ? 2099");
		    }
			try {
				jobsHandler.addJob(baseJob);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("BaseJob init end. [" + (null == list || list.size() == 0 ? 0 :list.size()) + " jobs.]");
    	}
		
		
	}

	
}
