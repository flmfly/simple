package simple.core.util.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class UserExclusionStrategy implements ExclusionStrategy {

	public static final UserExclusionStrategy EXCLUSION_STRATEGY = new UserExclusionStrategy();

	private UserExclusionStrategy() {

	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getDeclaringClass().getSimpleName().equals("BaseUser")
				&& (f.getName().equals("password") || f.getName().equals(
						"password1"));
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

}
