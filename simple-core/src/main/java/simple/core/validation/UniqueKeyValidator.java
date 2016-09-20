package simple.core.validation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import simple.core.dao.HibernateBaseDAO;
import simple.core.model.DomainDesc;
import simple.core.service.BaseService;
import simple.core.util.ReflectUtils;
import simple.core.validation.annotation.UniqueKey;

public class UniqueKeyValidator implements
		ConstraintValidator<UniqueKey, Serializable> {

	@Autowired
	private HibernateBaseDAO hibernateBaseDAO;

	@Autowired
	private BaseService baseService;

	private UniqueKey uniqueKey;

	@Override
	public void initialize(UniqueKey uniqueKey) {
		this.uniqueKey = uniqueKey;

	}

	public boolean isValid(Serializable target,
			ConstraintValidatorContext cvContext) {
		if (null == this.hibernateBaseDAO) {
			return true;
		}

		boolean valid = false;

		Map<String, Object> filters = new HashMap();
		Set<String> aotherFilters = new HashSet<String>();
		if (StringUtils.isNotEmpty(uniqueKey.filter())) {
			aotherFilters.add(uniqueKey.filter());
		}
		Class<?> entityClass = target.getClass();
		String[] columnNames = this.uniqueKey.columnNames();

		try {

			Field idField = ReflectUtils.getField(entityClass, "id");
			idField.setAccessible(true);
			{
				Long id = (Long) idField.get(target);
				if (id != null) {
					aotherFilters.add("id !=" + id);
				}
			}
			int i = 0;

			for (; i < columnNames.length; i++) {
				String propertyName = columnNames[i];
				String[] properties = propertyName.split("\\.");
				Object obj = target;
				Object val;
				for (int k = 0; k < properties.length; k++) {
					if (obj == null) {
						break;
					}
					Field propField = ReflectUtils.getField(obj.getClass(),
							properties[k]);
					propField.setAccessible(true);
					obj = propField.get(obj);
				}
				val = obj;
				if (null == val) {
					aotherFilters.add(propertyName + " is null ");
				} else {
					DomainDesc desc = baseService.getDomainDesc(val.getClass()
							.getSimpleName().toLowerCase());
					if (desc == null) {
						filters.put(propertyName, val);
						// if (val instanceof String
						// && StringUtils.isEmpty((String) val)) {
						// aotherFilters.add(propertyName + " is null ");
						// } else {
						// filters.put(propertyName, val);
						// }

					} else {
						Field idSubField = ReflectUtils.getField(
								val.getClass(), "id");
						idSubField.setAccessible(true);
						Long id = (Long) idSubField.get(val);
						if (id == null) {
							aotherFilters.add(propertyName + " is null ");
						} else {
							filters.put(propertyName + ".id",
									idSubField.get(val));
						}
					}
				}
			}

			List<Long> ids = findAllId(entityClass, filters, aotherFilters);
			Iterator<Long> dataIt = ids.iterator();
			if (dataIt.hasNext()) {
				Object obj = dataIt.next();
				if (dataIt.hasNext()) {
					valid = false;
				} else {
					long id = (Long) obj;
					Long sid = (Long) idField.get(target);
					if (null != sid && id == sid.longValue()) {
						valid = true;
					}
				}
			} else {
				valid = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return valid;
	}

	private List<Long> findAllId(Class domainClazz,
			Map<String, Object> filters, Set<String> aotherFilters) {
		List<String> filterStrList = new ArrayList<String>();
		List<Object> parameters = new ArrayList<Object>();

		// TODO more filter type support, only support Number and String
		for (String fk : filters.keySet()) {
			Object fv = filters.get(fk);
			filterStrList.add(fk + "=?");
			parameters.add(fv);
		}

		if (null != aotherFilters) {
			filterStrList.addAll(aotherFilters);
		}
		List<Long> rtList = new ArrayList<Long>();
		List<?> dataList = this.hibernateBaseDAO.findByProperties(domainClazz,
				null, "id", StringUtils.join(filterStrList, " and "), null,
				parameters);
		for (Object object : dataList) {
			rtList.add(Long.parseLong(object.toString()));
		}
		return rtList;
	}
}
