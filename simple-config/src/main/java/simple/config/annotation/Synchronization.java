package simple.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Synchronization {

	public final static int FULL = 0;
	public final static int DELTA = 1;

	String by() default "";

	int pattern() default Synchronization.FULL;

	String lastUpdateFieldName() default "";

	String synchronizedFiels() default "*";

	boolean cache() default false;

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		Synchronization[] value();
	}
}
