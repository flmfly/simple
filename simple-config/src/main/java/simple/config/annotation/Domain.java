package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Domain {
	String value() default "";

	String defaultSort() default "";

	String defaultFilterHandler() default "";

	boolean batch() default false;
}
