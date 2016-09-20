package simple.scheduler.quartz.support;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.config.annotation.support.OperationHandler;
import simple.scheduler.quartz.model.BaseJob;
import simple.scheduler.quartz.service.JobsHandlerService;

@Component
public class BaseJobRunningHandler implements OperationHandler{

	@Autowired
	private JobsHandlerService jobsHandlerService;
	
	@Override
	public boolean disabled(Object domain) {
		return false;
	}

	@Override
	public OperationResult handle(Map<String, Object> parameters,
			List<Object> domains) {
		OperationResult result = new OperationResult();
		BaseJob baseJob = (BaseJob)domains.get(0);
		try {
			if(!jobsHandlerService.checkSchedulerExsit(baseJob.getJobCode())){
				result.addErrorMessage("请先启动该任务再立即执行!");
				return result;
			}
			jobsHandlerService.triggerJob(baseJob.getJobCode());
			result.setSuccess(true); 
		} catch (Exception e) {
			result.addErrorMessage("立即执行任务报错，任务编码为:"+baseJob.getJobCode()+",错误:"+e.getMessage());
		}
		
		return result;
	}

}
