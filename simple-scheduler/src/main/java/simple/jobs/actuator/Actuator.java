package simple.jobs.actuator;

import org.apache.log4j.Logger;


/**
 * 
 */
public interface Actuator {

	static final Logger log = Logger.getLogger(Actuator.class);

	Object execute(String name, String params) throws Exception;

	String getName();
}
