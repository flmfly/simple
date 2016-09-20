package simple.core.service;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import simple.config.annotation.AutoFill;
import simple.config.annotation.support.AutoFillHandler;
import simple.core.util.ReflectUtils;

@Service
public class AutoFillService extends HandlerService {

	public void autoFillDomain(String domainName, Object domain,
			HttpServletRequest request) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Map<Field, AutoFill> autoFills = annotionService
				.getAutoFill(domainName);

		for (Iterator<Field> iterator = autoFills.keySet().iterator(); iterator
				.hasNext();) {
			Field field = iterator.next();
			AutoFill autoFill = autoFills.get(field);

			switch (autoFill.trigger()) {
			case NULL:
				field.setAccessible(true);
				Object val = field.get(domain);
				if (null != val) {
					continue;
				}
				break;
			case NOTNULL:

				break;
			case ALWAYS:

				break;
			default:
				break;
			}
			field.setAccessible(true);

			AutoFillHandler handler = (AutoFillHandler) super
					.fetchHandler(autoFill.handler());
			handler.handle(field, domain, request);
		}
		// TODO:递归autofill
		Field[] fields = ReflectUtils.getDeclaredFields(domain.getClass());
//		Field[] fields = domain.getClass().getDeclaredFields();
		for (Field field : fields) {
//			if (field.getAnnotation(ManyToOne.class) != null) {
//				field.setAccessible(true);
//				Object obj = field.get(domain);
//				if (obj == null) {
//					continue;
//				}
//				autoFillDomain(field.getType().getSimpleName().toLowerCase(),
//						obj, request);
//			} else 
			if (field.getAnnotation(OneToMany.class) != null
					|| field.getAnnotation(ManyToMany.class) != null) {
				field.setAccessible(true);
				Collection collection = (Collection) field.get(domain);
				if (collection == null || collection.isEmpty()) {
					continue;
				}
				for (Object obj : collection) {
					if (obj == null) {
						continue;
					}
					autoFillDomain(
							obj.getClass().getSimpleName().toLowerCase(), obj,
							request);
				}
			}
		}
	}

}
