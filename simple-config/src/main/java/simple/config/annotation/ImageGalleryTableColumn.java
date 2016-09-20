package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageGalleryTableColumn {
	String field();

	String url();
	
	String fileNameProperty() default ""; 

	boolean isArray() default false;

	boolean isFileStyle() default false;
}
