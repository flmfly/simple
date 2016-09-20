package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationParameter {

	OperationParameterType type() default OperationParameterType.INPUT;

	String title() default "";

	String value() default "";

	String code();
}
