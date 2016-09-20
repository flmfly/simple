package simple.jobs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import simple.jobs.IJobHandler;
import simple.jobs.utils.JobMethod;


public class InterfaceJob implements IJobHandler<Boolean> {

	
	private static final Log LOG = LogFactory.getLog(InterfaceJob.class);

	@Override
	@JobMethod(jobMethodDesc = "")
	public Boolean handle(String param) throws Exception {
		LOG.info("I want to do it!");
		LOG.info((String)param.toString());
		return null;
	}


	@Override
	public String jobDesc() throws Exception {
		// TODO Auto-generated method stub
		return "该次job用于测试";
	}

	@Override
	public String jobMehtodParamDesc() throws Exception {
		// TODO Auto-generated method stub
		return "该次job方法参数使用'~~'拆分";
	}

	
	
}
