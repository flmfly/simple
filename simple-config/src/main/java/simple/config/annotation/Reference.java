package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Reference {

	String id() default "id";

	String label() default "name";

	String viewFields() default "";

	String pid() default "";

	ReferenceType type() default ReferenceType.SINGLE_TREE;

	String depend() default "";

	String dependAssociateField() default "";
	
	String filter() default "";
	
	boolean editable() default false;
}
