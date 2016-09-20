package simple.jobs.actuator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import simple.core.util.ThreadLocalUtils;

public class MethodActuator implements Actuator {

	private static final String METHOD_LIMIT_CLASS = "simple.jobs.AbstractJobHandler";

	private static final String METHOD_NAME = "handle";

	private static final String METHOD_SET_APPLICATIONCONTEXT = "setApplicationContext";

	private ApplicationContext applicationContext;

	private String name;

	private Logger log = Logger.getLogger(MethodActuator.class);

	public MethodActuator(String name, ApplicationContext applicationContext) {
		this.name = name;
		this.applicationContext = applicationContext;
	}

	public String getName() {
		return name;
	}

	public Object execute(String name, String params) throws Exception {
		log.info("execute method: " + name + params);

		Class<?> clazz = Class.forName(name);
		log.info(clazz.getName());
		Class<?> clazzs = clazz;
		List<Field> fields = new ArrayList<Field>();
		do {
			fields.addAll(Arrays.asList(clazzs.getDeclaredFields()));
			if (clazzs.getName().toLowerCase()
					.equals(METHOD_LIMIT_CLASS.toLowerCase())) {
				break;
			}
			clazzs = clazzs.getSuperclass();
		} while (null != clazzs);
		if (null == clazzs) {
			throw new Exception(
					String.format(
							"Class:%s Unrealized simple.jobs.AbstractJobHandler,please do it",
							name));
		}
		Class<?> superParamClass = ApplicationContext.class;
		Object obj;
		boolean fromSpring = false;
		try {
			// TODO: 检测是否是从Spring容器获得
			if (null != clazz.getAnnotation(Service.class)) {
				obj = this.applicationContext.getBean(clazz);
				fromSpring = true;
			} else if (null != clazz.getAnnotation(Component.class)) {
				Component component = clazz.getAnnotation(Component.class);
				if (StringUtils.isNotEmpty(component.value())) {
					obj = this.applicationContext.getBean(component.value());
				} else {
					obj = this.applicationContext.getBean(clazz);
				}
				fromSpring = true;
			} else {
				obj = clazz.newInstance();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			obj = clazz.newInstance();
		}
		// TODO: 自动填充Handler对象中的属性
		if (!fromSpring) {
			for (Field field : fields) {
				if (field.getAnnotation(Autowired.class) != null) {
					field.setAccessible(true);
					field.set(obj,
							this.applicationContext.getBean(field.getType()));
				} else if (field.getAnnotation(Resource.class) != null) {
					Resource resource = field.getAnnotation(Resource.class);
					field.setAccessible(true);
					if (StringUtils.isNotEmpty(resource.name())) {
						field.set(obj, this.applicationContext.getBean(resource
								.name()));
					}
				}
			}
		}
		
		Method superMethod = clazz.getMethod(METHOD_SET_APPLICATIONCONTEXT,
				superParamClass);
		superMethod.invoke(obj, new Object[] { this.applicationContext });
		Class<?> paramClass = null;
		paramClass = String.class;
		Method method = clazz.getMethod(METHOD_NAME, paramClass);
		try {
			ThreadLocalUtils.clear();
			return method.invoke(obj, new Object[] { params });
		}finally{
			ThreadLocalUtils.clear();
		}
		
	}
}
