package simple.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Operation {

	String name();

	String code();

	OperationTarget target() default OperationTarget.SELECTED;

	boolean multi() default false;

	String iconStyle() default "fa fa-at";

	Class<?> handler();

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		Operation[] value();
	}

	OperationParameter[] parameters() default {};
}
