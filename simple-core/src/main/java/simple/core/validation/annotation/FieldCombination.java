package simple.core.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import simple.core.validation.FieldCombinationValidator;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldCombinationValidator.class)
@Documented
public @interface FieldCombination {

	/**
	 * 复合校验的所有字段.
	 * @return
	 */
	String[] fieldNames();
	
	/**
	 * 规则例如rule=={"0,2","1,3"}
	 * 例子可以解释为：数组第0和第1个元素必填可以  或者  第1和第3个元素必填可以。其他情况报错。
	 * 或者 这个词很重要撒。
	 * @return
	 */
	String[] fieldRules();
	
	String message() default "{FieldCombination.message}";
	

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	
	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		FieldCombination[] value();
	}
}
