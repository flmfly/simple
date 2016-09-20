package simple.core.support;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.config.annotation.DictField;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.support.AutoFillHandler;
import simple.core.service.BaseService;
import simple.core.util.ReflectUtils;
import simple.core.util.ThreadLocalUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class BusinessKeyAutoFillHandler implements AutoFillHandler {
	private static final Log LOG = LogFactory
			.getLog(BusinessKeyAutoFillHandler.class);

	// private static final Gson GSON = GsonBuilderUtil.getDefaultGsonBuilder()
	// .create();
	private static final Gson GSON = new GsonBuilder().setDateFormat(
			"yyyy-MM-dd HH:mm:ss").create();
	@Autowired
	private BaseService baseService;

	@Override
	public void handle(Field field, Object target, HttpServletRequest request)
			throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		Object obj = field.get(target);
		if (obj == null || obj instanceof HibernateProxy) {
			return;
		}
		Field idField;
		try {
			Class cls = obj.getClass();
			idField = ReflectUtils.getField(cls, "id");
			idField.setAccessible(true);
		} catch (Exception e) {
			// TODO: handle exception
			throw new IllegalArgumentException(e);
		}
		if (idField.get(obj) != null) {
			return;
		}
		Map<String, Object> filters = new HashMap();
		Set<String> aotherFilters = new HashSet<String>();
		String json = GSON.toJson(obj);
		Map<String, Object> refData = GSON.fromJson(json, HashMap.class);
		try {
			fillFilters("", filters, refData, obj);
			if (filters.isEmpty()) {
				field.set(target, null);
				return;
			}
			DictField dictField = field.getAnnotation(DictField.class);
			if (dictField != null) {
				filters.put("dict.code", dictField.value());
			}
			Reference reference = field.getAnnotation(Reference.class);
			if (reference != null) {
				if (StringUtils.isNotEmpty(reference.filter())) {
					aotherFilters.add(reference.filter());
				}
				if (StringUtils.isNotEmpty(reference.depend())) {
					String[] fields = reference.depend().split("\\.");
					Object depend = target;
					Field dependField = null;
					Object pdepend = null;
					for (String f : fields) {
						if (depend == null) {
							dependField = ReflectUtils.getField(
									dependField.getType(), f);
							continue;
						}
						dependField = ReflectUtils.getField(depend.getClass(),
								f);
						dependField.setAccessible(true);
						pdepend = depend;
						depend = dependField.get(pdepend);
					}
					if (depend != null) {
						Field idDependField = ReflectUtils.getField(
								depend.getClass(), "id");
						idDependField.setAccessible(true);
						Object dependId = idDependField.get(depend);
						if (dependId == null) {
							this.handle(dependField, pdepend, request);
							depend = dependField.get(pdepend);
						}
					}
					if (depend != null) {
						Field idDependField = ReflectUtils.getField(
								depend.getClass(), "id");
						idDependField.setAccessible(true);
						filters.put(reference.dependAssociateField(),
								idDependField.get(depend));
						// aotherFilters.add(reference.dependAssociateField()
						// + ".id=" + idDependField.get(depend));
					} else {
						RepresentationField fieldRepresentationField = field
								.getAnnotation(RepresentationField.class);
						RepresentationField dependFieldRepresentationField = null;
						if (dependField != null) {
							dependFieldRepresentationField = dependField
									.getAnnotation(RepresentationField.class);
						}
						StringBuffer msg = new StringBuffer();
						if (fieldRepresentationField != null) {
							msg.append(fieldRepresentationField.title());
						}
						msg.append("缺少依赖");
						if (dependFieldRepresentationField != null) {
							msg.append(dependFieldRepresentationField.title());
						}
						msg.append("!");
						throw new RuntimeException(msg.toString());
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		String cacheKey = target.getClass().getName() + "#" + field.getName()
				+ "#" + json + "#" + StringUtils.join(aotherFilters, ",");
		{
			Long id = ThreadLocalUtils.get(cacheKey);
			if (id != null) {
				idField.set(obj, id);
				return;
			}
		}
		long id = baseService.getIdWithCriterions(obj.getClass()
				.getSimpleName().toLowerCase(), filters, aotherFilters);
		if (id != -1) {
			idField.set(obj, id);
			ThreadLocalUtils.set(cacheKey, id);
		} else {
			LOG.warn("no commandNo find");
			field.set(target, null);
			NotNull notNull = field.getAnnotation(NotNull.class);
			if (notNull != null) {
				Set<String> keySet = new HashSet<String>(filters.keySet());
				for (String key : keySet) {
					if (key.endsWith("dict.code")) {
						filters.remove(key);
					}
				}
				throw new RuntimeException("("
						+ StringUtils.join(filters.values(), ",") + ")"
						+ notNull.message());
			}

		}

	}

	private void fillFilters(String filterKey, Map filters,
			Map<String, Object> refData, Object obj) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Class cls = obj.getClass();
		for (String key : refData.keySet()) {
			Field f = ReflectUtils.getField(cls, key);
			f.setAccessible(true);
			Object o = refData.get(key);
			String _filterKey;

			if (StringUtils.isNotEmpty(filterKey)) {
				_filterKey = filterKey + "." + key;
			} else {
				_filterKey = key;
			}
			DictField dictField = f.getAnnotation(DictField.class);
			if (dictField != null) {
				if (StringUtils.isNotEmpty(_filterKey)) {
					filters.put(_filterKey + ".dict.code", dictField.value());
				} else {
					filters.put("dict.code", dictField.value());
				}
			}
			Object value = f.get(obj);
			if (o instanceof Map) {
				fillFilters(_filterKey, filters, (Map) o, value);
			} else {
				filters.put(_filterKey, value);
			}
		}
	}
}
