package simple.scheduler.quartz.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import simple.core.service.BaseService;
import simple.jobs.AbstractJobHandler;
import simple.jobs.model.JobHandler;
import simple.scheduler.quartz.support.AbstractTypeFilter;
import simple.scheduler.quartz.support.InterfaceTypeFilter;

public class JobHandlerInitService  implements InitializingBean{
	
	@Autowired
	private BaseService baseService;

	private static List<Class<?>> classList = new ArrayList<Class<?>>();
	
	private static final String JOB_DESC ="jobDesc";
	
	private static final String JOB_MEHTODPARAM_DESC ="jobMehtodParamDesc";
	
	private String packageName;
	
	public JobHandlerInitService(String packageName) {
		this.packageName = packageName;
	}

	public static List<Class<?>> getClassList() {
		return classList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try{
			init();
			checkOutJobHandler(classList);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private void init(){

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);

		scanner.addIncludeFilter(new AbstractTypeFilter(AbstractJobHandler.class));

		for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
			Class<?> clazz;
			try {
				clazz = Class.forName(bd.getBeanClassName());
				classList.add(clazz);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void checkOutJobHandler(List<Class<?>> classList) throws Exception {
		List<JobHandler> jobHandlerList = baseService.find(JobHandler.class,
				null, null);
		
		if(null == classList){
			if (null != jobHandlerList && !jobHandlerList.isEmpty()) {
				for(JobHandler jobHandler : jobHandlerList){
					jobHandler.setState(false);
					baseService.save(jobHandler);
				}
			} 
			return;
		}
		List<JobHandler> jobHandlerExsitList = new ArrayList<JobHandler>();
		for(Class<?> clazz : classList){
			Object obj = clazz.newInstance();
			Method jobDescMethod = clazz.getMethod(JOB_DESC);
			Method jobMethodParamMethod = clazz
					.getMethod(JOB_MEHTODPARAM_DESC);
			if (null != jobHandlerList && !jobHandlerList.isEmpty()) {
				boolean flag = false;
				 for(JobHandler jobHandler : jobHandlerList){
					 if(jobHandler.getJobHandlerPath().toLowerCase().equals(clazz.getName().toLowerCase())){
						 jobHandler
							.setJobHandlerDesc((String) jobDescMethod.invoke(obj));
						jobHandler.setMethodParamDesc((String) jobMethodParamMethod
								.invoke(obj));
						jobHandler.setState(true);
						baseService.save(jobHandler);
						jobHandlerExsitList.add(jobHandler);
						flag =true;
						break;
					 }
				 }
				 if(!flag){
					 JobHandler jobHandler = new JobHandler();
						jobHandler
								.setJobHandlerDesc((String) jobDescMethod.invoke(obj));
						jobHandler.setMethodParamDesc((String) jobMethodParamMethod
								.invoke(obj));
						jobHandler.setJobHandlerPath(clazz.getName());
						jobHandler.setState(true);
						baseService.save(jobHandler);
				 }
				
			} else {
				JobHandler jobHandler = new JobHandler();
				jobHandler
						.setJobHandlerDesc((String) jobDescMethod.invoke(obj));
				jobHandler.setMethodParamDesc((String) jobMethodParamMethod
						.invoke(obj));
				jobHandler.setJobHandlerPath(clazz.getName());
				jobHandler.setState(true);
				baseService.save(jobHandler);
			}
		}

		 if(null != jobHandlerList && !jobHandlerList.isEmpty()){
			 jobHandlerList.removeAll(jobHandlerExsitList);
			 for(JobHandler jobHandler :jobHandlerList){
				 jobHandler.setState(false);
				 baseService.save(jobHandler);
			 }
		 }
	}

	

}
