package simple.core.util;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import simple.core.util.gson.HibernateProxyTypeAdapter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

public final class GsonBuilderUtil {

	private static final Set<String> EXCLUDE_PROPERTY_NAMES = new HashSet<String>();

	static {
		EXCLUDE_PROPERTY_NAMES.add("lockFlag");
		EXCLUDE_PROPERTY_NAMES.add("createTime");
		// EXCLUDE_PROPERTY_NAMES.add("updateTime");
	}

	private GsonBuilderUtil() {
	}

	public static GsonBuilder getDefaultGsonBuilder() {
		return new GsonBuilder()
		// .setExclusionStrategies(new IgnoreManyToManyExclusionStrategy())
				.setExclusionStrategies(new ExclusionStrategy() {

					private Map<Class<?>, Class<?>> oneToManyClassMap = new HashMap<Class<?>, Class<?>>();
					private Map<Class<?>, Class<?>> manyToOneClassMap = new HashMap<Class<?>, Class<?>>();

					public boolean shouldSkipField(FieldAttributes f) {
						Class<?> fieldClass = f.getDeclaredClass();
						boolean oneToMany = null != f
								.getAnnotation(ManyToMany.class)
								|| null != f.getAnnotation(OneToMany.class);
						boolean manyToOne = null != f
								.getAnnotation(ManyToOne.class);

						if (oneToMany) {
							ParameterizedType pt = (ParameterizedType) f
									.getDeclaredType();
							oneToManyClassMap.put(f.getDeclaringClass(),
									(Class<?>) pt.getActualTypeArguments()[0]);
							fieldClass = (Class<?>) pt.getActualTypeArguments()[0];
						}

						if (manyToOne) {
							manyToOneClassMap.put(f.getDeclaringClass(),
									f.getDeclaredClass());
						}

						if (oneToMany && manyToOneClassMap.containsKey(fieldClass)) {
							return true;
						}
						if(manyToOne && oneToManyClassMap.containsKey(fieldClass)){
							return true;
						}

						// if (null == toBePutToMap) {
						// if (currentOperationIgnore
						// && nestedClassMap.containsKey(fieldClass)) {
						// return true;
						// }
						// }
						// else{
						// if(toBePutToMap != f.getDeclaredClass()){
						// return true;
						// }
						// }
						//
						// if (nestedClassMap.containsKey(f.getDeclaredClass()))
						// {
						// return true;
						// }

						return false;
					}

					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				}, new ExclusionStrategy() {

					public boolean shouldSkipField(FieldAttributes f) {
						return EXCLUDE_PROPERTY_NAMES.contains(f.getName());
					}

					public boolean shouldSkipClass(Class<?> clazz) {
						return clazz.getSimpleName().equals(
								"BaseUser");
					}
				})
				.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
				.setDateFormat("yyyy-MM-dd HH:mm:ss");
	}

}
