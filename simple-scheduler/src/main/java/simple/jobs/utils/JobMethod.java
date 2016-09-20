package simple.jobs.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD,ElementType.TYPE,ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface JobMethod {

	 String jobMethodDesc();
	 
//	 String jobClassDesc();
}
