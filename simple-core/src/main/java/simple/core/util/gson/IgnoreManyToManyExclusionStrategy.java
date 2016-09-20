package simple.core.util.gson;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class IgnoreManyToManyExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return null != f.getAnnotation(ManyToMany.class)
				|| null != f.getAnnotation(OneToMany.class);
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

}
