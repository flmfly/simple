package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Attachment {

	String url();

	String fileName();
	
	String size();

	// default 2MB
	long maxSize() default 2097152;

	String type() default "";

	String desc() default "";

	long height() default 0;

	long width() default 0;
}
