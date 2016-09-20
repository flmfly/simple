package simple.jobs;


public interface IJobHandler<T> {

	/**
	 * 用于job执行。
	 * @param param
	 * @return
	 * @throws Exception
	 */
	 public T handle(String param) throws Exception;
	 
	 public String jobDesc() throws Exception;
	 
	 
	 public String jobMehtodParamDesc() throws Exception;
	 
	 
}
