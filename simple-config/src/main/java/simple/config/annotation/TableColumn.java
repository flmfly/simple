package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TableColumn {
	/**
	 * replace with @Title
	 */
	@Deprecated
	String title() default "";

	Class<?> type() default String.class;

	String format() default "";

	boolean show() default true;

	String click() default "#";

	int sort() default Integer.MAX_VALUE;

	String handler() default "";
}
