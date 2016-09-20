package simple.jobs;

import org.springframework.context.ApplicationContext;

public abstract class AbstractJobHandler<T>{
	
	/**
	 * 用于job执行。
	 * @param param
	 * @return
	 * @throws Exception
	 */
	 public abstract T handle(String param) throws Exception;
	 
	 public abstract String jobDesc() throws Exception;
	 
	 public abstract String jobMehtodParamDesc() throws Exception;

	 public abstract void setApplicationContext(ApplicationContext applicationContext);
	 
	 
	 
}
