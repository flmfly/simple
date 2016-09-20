package simple.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class HandlerService {

	@Autowired
	protected SpringContextHelper springContextHelper;

	@Autowired
	protected AnnotationService annotionService;

	protected Object fetchHandler(Class<?> clazz)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (null != clazz.getAnnotation(Component.class)) {
			return springContextHelper.getBean(clazz);
		} else {
			return clazz.newInstance();
		}
	}

}
