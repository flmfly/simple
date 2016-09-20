package simple.core.service;

import static simple.config.Constants.WEBSERVICE_WRITE_OPERATION_DELETE;
import static simple.config.Constants.WEBSERVICE_WRITE_OPERATION_INSERT;
import static simple.config.Constants.WEBSERVICE_WRITE_OPERATION_INSERTORUPDATE;
import static simple.config.Constants.WEBSERVICE_WRITE_OPERATION_INSERT_BEFOR_DELETE;
import static simple.config.Constants.WEBSERVICE_WRITE_OPERATION_UPDATE;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.Subcriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.config.annotation.support.Serializer;
import simple.core.model.DomainDesc;
import simple.core.model.OneToMany;
import simple.core.model.webservice.Request;
import simple.core.model.webservice.Response;
import simple.core.util.GsonBuilderUtil;
import simple.core.util.ReflectUtils;

@Service
public class WebserviceService extends BaseService {

	@Autowired
	ExtentionPointService extentionPointService;

	@Autowired
	private CriteriaService criteriaService;

	private Log logger = LogFactory.getLog(WebserviceService.class);

	public String toJson(String domainName, Object entity) {
		String serializer = super.getSerializer(domainName);

		if (null != serializer) {
			try {
				Object obj = Class.forName(serializer).newInstance();
				Serializer ser = (Serializer) obj;
				return ser.serialize(entity);
			} catch (Exception e) {
				return "{}";
			}
		} else {
			return GsonBuilderUtil.getDefaultGsonBuilder().create()
					.toJson(entity);
		}
	}

	public Request getRequest(String json) {
		return GSON.fromJson(json, Request.class);
	}

	@SuppressWarnings("unchecked")
	public Response processReadRequest(Response response) throws Exception {
		String domainName = response.getFunc();
		String sort = response.getPage().getSort();
		int num = response.getPage().getNum();
		int size = response.getPage().getSize();
		Class clazz = this.getDomainClass(domainName);
		if (StringUtils.isEmpty(sort)) {
			DomainDesc domainDesc = this.getDomainDesc(domainName);
			sort = domainDesc.getDefaultSort();
		}
		Criteria criteria = this.hibernateBaseDAO
				.getCriteria(clazz, domainName);
		Map queryMap;
		if (response.getQuery() instanceof Map) {
			queryMap = (Map) response.getQuery();
		} else {
			String json = GSON.toJson(response.getQuery());
			queryMap = GSON.fromJson(json, Map.class);
		}
		queryMap = transQuery("", clazz, queryMap);
		Map<String, Object> query = criteriaService.createQuery(clazz,
				queryMap, criteria);
		List<Criterion> criterions = criteriaService.getSearchCriterions(
				domainName, query);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (num >= 0) {
			if (size <= 0) {
				size = 20;
			}
			CriteriaImpl criteriaImpl = ((CriteriaImpl) criteria);

			Criteria totalCriteria = this.hibernateBaseDAO.getCriteria(clazz,
					domainName);
			Iterator<Subcriteria> iterator = criteriaImpl.iterateSubcriteria();
			while (iterator.hasNext()) {
				Subcriteria subcriteria = iterator.next();
				totalCriteria.createAlias(subcriteria.getPath(),
						subcriteria.getAlias(), subcriteria.getJoinType());
			}
			for (Criterion criterion : criterions) {
				totalCriteria.add(criterion);
			}

			totalCriteria.setProjection(Projections.rowCount());
			response.getPage().setTotal((Long) totalCriteria.uniqueResult());
			long pageTotal = response.getPage().getTotal() / size;
			if (response.getPage().getTotal() % size != 0) {
				pageTotal++;
			}
			response.getPage().setPageTotal(pageTotal);

			// criteria.
			criteria.setMaxResults(size);
			criteria.setFirstResult(num * size);
			// response.getPage().setTotal();
		}
		List<Object> list = new ArrayList<Object>();
		try {
			if (StringUtils.isNotEmpty(sort)) {
				String[] sorts = sort.split(",");
				Map<String, Object> sortMap = new HashMap<String, Object>();
				for (int i = 0; i < sorts.length; i++) {
					String s = sorts[i].trim();
					boolean desc = false;
					if (s.startsWith("-")) {
						desc = true;
						s = s.substring(1);
					}
					if (s.indexOf('.') != -1) {
						sortMap.put(s, "");
					}
					if (StringUtils.isEmpty(s)) {
						continue;
					}
					if (desc) {
						criteria.addOrder(Order.desc(s));
					} else {
						criteria.addOrder(Order.asc(s));
					}
				}
				criteriaService.createQuery(clazz, sortMap, criteria);
			}
			list = criteria.list();
		} catch (Exception e) {
			// TODO: handle exception
			Throwable ex = e;
			while (ex != null) {
				Throwable r = ex.getCause();
				if (r == null || e.equals(r)) {
					break;
				}
				ex = r;
			}
			if (ex.getMessage().indexOf("ORA-01792") != -1) {// oracle 超过1000列
				criteria.setProjection(Projections.property("id"));
				List ids = criteria.list();
				for (Object id : ids) {
					list.add(this.get(clazz, (Serializable) id));
				}
			} else {
				throw e;
			}
		}
		for (Object obj : list) {
			extentionPointService.afterFetch(obj);
		}
		response.setData(list);
		return response;
	}

	private Map<String, Object> transQuery(final String path, Class clazz,
			Map<String, Object> queryMap) {
		// TODO Auto-generated method stub
		if (clazz == null) {
			return Collections.EMPTY_MAP;
		}
		Map rtMap = new HashMap();
		try {
			String prex = path;
			if (StringUtils.isEmpty(prex)) {
				prex = "";
			} else {
				prex += ".";
			}
			for (String key : queryMap.keySet()) {
				Object object = queryMap.get(key);
				if (key.indexOf('.') != -1) {
					rtMap.put(prex + key, object);
				} else {
					Field field = ReflectUtils.getField(clazz, key);
					if (object instanceof Map) {
						rtMap.putAll(transQuery(prex + key, field.getType(),
								(Map) object));
					} else {
						rtMap.put(prex + key, object);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			rtMap.clear();
			if (StringUtils.isNotEmpty(path)) {
				rtMap.put(path, queryMap);
			}
		}
		return rtMap;
	}

	/**
	 * 提供Job查询返回Json字符串，避免Session Close 问题.
	 * 
	 * @param response
	 * @return
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public String processReadRequestToJson(Response response) throws Exception {
		response = processReadRequest(response);
		return toJson(response.getFunc(), response);
	}

	private void delete(Response response) throws Exception {
		try {
			String domainName = response.getFunc();
			Class<?> clazz = this.getDomainClass(domainName);
			Field idField = ReflectUtils.getField(clazz, "id");
			idField.setAccessible(true);
			for (Object object : response.getData()) {
				Object entity = this.getObjectFromJson(GSON.toJson(object),
						clazz);
				Long id = (Long) idField.get(entity);
				List<String> bkList = super.getBusinessKey(entity.getClass()
						.getSimpleName().toLowerCase());
				if (null == id && null != bkList) {
					this.fillIdWithBusinessKey(entity, bkList,
							WEBSERVICE_WRITE_OPERATION_DELETE);
					id = (Long) idField.get(entity);
				}
				if (null != id) {
					super.delete(clazz, id);
				} else {
					throw new NoSuchObjectException(
							"no record found with business key for delete!");
				}
			}
		} catch (NoSuchObjectException e) {
			throw e;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw e;
			} else {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

	}

	public Response updateWriteRequest(Response response,
			HttpServletRequest request) throws Exception {
		try {
			Integer operation = response.getOperation();

			if (null == operation) {
				operation = WEBSERVICE_WRITE_OPERATION_UPDATE;
			}

			if (operation == WEBSERVICE_WRITE_OPERATION_DELETE) {
				this.delete(response);
				return response;
			} else if (operation == WEBSERVICE_WRITE_OPERATION_INSERT_BEFOR_DELETE) {
				operation = 2;
				try {
					this.delete(response);
				} catch (NoSuchObjectException e) {

				}
			}
			String domainName = response.getFunc();
			Class<?> clazz = this.getDomainClass(domainName);
			List<Object> dataList = response.getData();

			boolean hasError = false;
			Field idField = ReflectUtils.getField(clazz, "id");
			idField.setAccessible(true);
			if (null != dataList) {
				List<List<String>> validation = new ArrayList<List<String>>();
				List<Object> savedList = new ArrayList<Object>(dataList.size());
				for (Object object : dataList) {
					Object entity = this.getObjectFromJson(GSON.toJson(object),
							clazz);
					this.autoFillDomain(domainName, entity, request);
					Long id = (Long) idField.get(entity);
					List<String> bkList = super.getBusinessKey(entity
							.getClass().getSimpleName().toLowerCase());
					if (null == id && null != bkList
							&& operation != WEBSERVICE_WRITE_OPERATION_INSERT) {
						this.fillIdWithBusinessKey(entity, bkList, operation);
					}

					for (OneToMany oneToMany : super.getOneToMany(entity
							.getClass().getSimpleName().toLowerCase())) {
						// get property filed
						Field subField = ReflectUtils.getField(clazz,
								oneToMany.getFieldName());
						subField.setAccessible(true);
						if (operation != WEBSERVICE_WRITE_OPERATION_INSERT) {
							boolean isManyToMany = (null == oneToMany
									.getSubFieldName());

							if (!isManyToMany) {
								// pick up the sub data
								List<String> subBkList = super
										.getBusinessKey(oneToMany.getType()
												.getSimpleName().toLowerCase());
								if (null != subBkList) {
									Set<?> subSet = (Set<?>) subField
											.get(entity);
									Field parentField = ReflectUtils.getField(
											oneToMany.getType(),
											oneToMany.getSubFieldName());
									parentField.setAccessible(true);
									for (Object sub : subSet) {
										parentField.set(sub, entity);
										this.fillIdWithBusinessKey(sub,
												subBkList,
												WEBSERVICE_WRITE_OPERATION_INSERTORUPDATE);
										parentField.set(sub, null);
									}
								}
							}
						}
					}

					id = (Long) idField.get(entity);

					if (id != null) {
						Object obj = hibernateBaseDAO.get(clazz, id);
						hibernateBaseDAO.evict(obj);
						copyProperties(clazz, entity, obj);
						entity = obj;
					}

					// List<String> errorList = super.validate(domainName,
					// entity);
					// if (!errorList.isEmpty()) {
					// validation.add(errorList);
					// hasError = true;
					// } else {
					// validation.add(Collections.<String> emptyList());
					// }
					savedList.add(entity);
				}

				if (hasError) {
					response.setValidation(validation);
				} else {
					for (Object entity : savedList) {
						super.update(entity, true);
					}
				}
				response.setData(savedList);
			}
			return response;
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	private void fillIdWithBusinessKey(Object entity, List<String> bkList,
			Integer operation) throws Exception {
		Class clazz = entity.getClass();
		Field idField = ReflectUtils.getField(clazz, "id");
		idField.setAccessible(true);
		Criteria criteria = super.hibernateBaseDAO.getCriteria(clazz);

		Map<String, Object> queryMap = this
				.getBusinessKeyObject(entity, bkList);
		queryMap = transQuery("", clazz, queryMap);
		Map<String, Object> query = criteriaService.createQuery(clazz,
				queryMap, criteria);
		List<Criterion> criterions = criteriaService.getSearchCriterions(clazz
				.getSimpleName().toLowerCase(), query);
		logger.info("fillIdWithBusinessKey queryMap:" + query.toString());
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		criteria.setFirstResult(0);
		criteria.setMaxResults(2);
		List<?> objectList = criteria.list();
		if (objectList.size() > 1) {
			throw new NoSuchObjectException("[" + StringUtils.join(bkList, ",")
					+ "]too many records(" + objectList.size()
					+ ") found with business key!");
		} else if (objectList.size() == 1) {
			Object databaseObj = objectList.get(0);
			hibernateBaseDAO.evict(databaseObj);
			idField.set(entity, idField.get(databaseObj));
			if (operation != WEBSERVICE_WRITE_OPERATION_DELETE) {
				for (String bk : bkList) {
					String fieldName = new StringTokenizer(bk, ".").nextToken();
					Field field = ReflectUtils.getField(entity.getClass(),
							fieldName);
					field.setAccessible(true);
					field.set(entity, field.get(databaseObj));
				}

			}
		} else if (objectList.isEmpty()
				&& operation != WEBSERVICE_WRITE_OPERATION_INSERTORUPDATE) {
			throw new NoSuchObjectException("[" + StringUtils.join(bkList, ",")
					+ "]no record found with business key!");

		}
	}

	private Map<String, Object> getBusinessKeyObject(Object oragin,
			List<String> bkList) {
		Map<String, Object> rtn = new HashMap<String, Object>();
		for (String bk : bkList) {
			StringTokenizer tokenizer = new StringTokenizer(bk, ".");
			Object val = oragin;
			while (tokenizer.hasMoreTokens()) {
				String fName = tokenizer.nextToken();
				if (val != null) {
					try {
						Field field = ReflectUtils.getField(val.getClass(),
								fName);
						field.setAccessible(true);
						val = field.get(val);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					break;
				}
			}
			if (val == null) {
				Map<String, Object> value = new HashMap<String, Object>();
				value.put("isnullval", true);
				rtn.put(bk, value);
			} else {
				rtn.put(bk, val);
			}
		}
		return rtn;
	}

	private void copyProperties(Class<?> clazz, Object source, Object target)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		for (Field field : fields) {
			if ("id".equals(field.getName())
					|| Modifier.toString(field.getModifiers())
							.indexOf("static") != -1) {
				continue;
			}
			field.setAccessible(true);
			Object obj = field.get(source);
			if (null != obj) {
				field.set(target, obj);
			}
		}
	}

	public static class NoSuchObjectException extends RuntimeException {

		public NoSuchObjectException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public NoSuchObjectException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public NoSuchObjectException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public NoSuchObjectException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

	}
}
