package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface StandardOperation {

	boolean add() default true;

	boolean delete() default true;

	boolean modify() default true;

	boolean query() default true;

	boolean imp() default true;

	boolean export() default true;

	boolean check() default true;
}
