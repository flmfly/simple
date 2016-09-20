package simple.core.validation;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import simple.core.util.ReflectUtils;
import simple.core.validation.annotation.FieldCombination;

public class FieldCombinationValidator implements
		ConstraintValidator<FieldCombination, Serializable> {

	private String[] fieldNames;

	private String[] fieldRules;

	@Override
	public void initialize(FieldCombination constraintAnnotation) {
		this.fieldNames = constraintAnnotation.fieldNames();
		this.fieldRules = constraintAnnotation.fieldRules();
	}

	@Override
	public boolean isValid(Serializable target,
			ConstraintValidatorContext context) {

		Class<?> entityClass = target.getClass();

		try {
			for (int i = 0; i < fieldRules.length; i++) {
				String rule = fieldRules[i];
				String[] ruleStr = rule.split(",");
				if (null == ruleStr) {
					return true;
				}
				boolean flag = true;
				for (int j = 0; j < ruleStr.length; j++) {
					Field propField = ReflectUtils.getField(entityClass,
							fieldNames[Integer.valueOf(ruleStr[j]) - 1]);
					propField.setAccessible(true);
					Object val = propField.get(target);
					if (null == val) {
						flag = false;
						break;
					}
				}
				if (flag) {
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
