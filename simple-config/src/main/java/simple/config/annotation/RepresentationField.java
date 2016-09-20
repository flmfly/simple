package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RepresentationField {

	/**
	 * replace with @Title
	 */
	@Deprecated
	String title() default "";

	RepresentationFieldType view() default RepresentationFieldType.INPUT;

	int sort() default Integer.MAX_VALUE;

	boolean disable() default false;

	boolean visable() default true;

	String defaultVal() default "";

	String placeholder() default "";

	FormGroup group() default @FormGroup(code = "basic", title = "基本信息");

	String onChangedListener() default "";

	/**
	 * Deprecated at 4/7/2015<br>
	 * See @SearchField
	 */
	@Deprecated
	boolean isSearchField() default false;

}
