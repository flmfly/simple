package simple.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchField {

	RepresentationFieldType view() default RepresentationFieldType.INPUT;

	int sort() default Integer.MAX_VALUE;

	String defaultVal() default "";

	boolean isRange() default false;

	boolean canFuzzy() default false;

	String path() default "";

	String title() default "";

	@Target({ ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		SearchField[] value();
	}

}
