package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RepresentationLayout {

	RepresentationLayoutType view() default RepresentationLayoutType.LIST;

	String id() default "";

	String label() default "";

	String pid() default "";

}
