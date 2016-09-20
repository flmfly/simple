package simple.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateImport {
	/**
	 * 
	 *err : 报错
	 *first : 按照id升序取第一条更新  
	 *last : 按照id升序取最后一条更新
	 */
	public enum MulOperation {
		err, first, last//TODO: all
	};

	String by() default "";

	MulOperation operation() default MulOperation.err;
}
