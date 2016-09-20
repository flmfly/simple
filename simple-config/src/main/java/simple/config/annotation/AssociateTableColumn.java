package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AssociateTableColumn {
	/**
	 * replace with @Label
	 */
	@Deprecated
	String titles();

	String columns();

	String types() default "";

	String formats() default "";

	String shows() default "";

	String clicks() default "";

	String sorts() default "";

	String handlers() default "";

}
