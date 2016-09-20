package simple.core.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.Subcriteria;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.config.annotation.Reference;
import simple.core.dao.HibernateBaseDAO;
import simple.core.model.FormField;
import simple.core.util.GenericUtils;
import simple.core.util.ReflectUtils;

/**
 * **** 条件的基本格式为
 * {"字段(支持点的多级属性，不能为空)":{"查询的方式(不能为空)":"值","fuzzy(是否模糊查询，缺省为false，只对字符串生效)"
 * :"true/false","or(是否为or连接，缺省为false)":"true/false"}}
 * 
 * 查询方式					操作符 
 * val						= 
 * neval					！= 
 * lval						> 
 * rval						< 
 * inval					in 
 * notinval					not in
 * isnullval(true/false) 	is null/is not null
 * 
 * ---- fuzzy(true)
 * 可以和val,neval组合进行模糊查询 fuzzy(true)+val like '%value%' fuzzy(true)+neval not
 * like '%value%' ---- or(false) 按照and连接查询（默认查询方式） or(true) 按照or连接查询 如：
 * 查询用户名称等于“test”的用户信息 {name:{val:"test"}}或者{name:"test"} 注:只有val的情况下可以简写
 * 
 * 查询用户名称含有“test”的所有用户信息 {name:{val:"test",fuzzy:true}}
 * 
 * 
 * 查询员工名称为“test”用户信息 {"employee.name":{val:"test"}}或者{"employee.name":"test"}
 * 
 * 查询员工名称为“test”和"test1"用户信息
 * {"employee.name":{inval:["test","test1"]}}或者{name:["test","test1"]}
 * 注:只有inval的情况下可以简写
 * 
 * 查询职位为“操作司机”，类型为“承运商”的所有用户信息
 * {"employee.position.name":{val:"操作司机"},"type.name":{val:"承运商"}}
 * 或者{"employee.position.name":"操作司机","type.name":"承运商"}
 */
@Service
public class CriteriaService {

	private final static Log LOGGER = LogFactory.getLog(CriteriaService.class);

	@Autowired
	protected HibernateBaseDAO hibernateBaseDAO;

	@Autowired
	private AnnotationService annotionService;

	protected static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public <T> T first(Class<T> clazz, Map<String, Object> queryMap) {
		return (T) first(clazz, queryMap, null);
	}

	/**
	 * 按照好条件查询返回第一个满足条件的数据
	 * 
	 * @param clazz
	 * @param queryMap
	 * @param properties
	 * @return
	 */
	public Object first(Class clazz, Map<String, Object> queryMap,
			String[] properties) {
		Criteria criteria = getCriteria(clazz, queryMap);
		setProjection(clazz, criteria, null, properties);
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return criteria.uniqueResult();
	}

	public <T> T uniqueResult(Class<T> clazz, Map<String, Object> queryMap) {
		return (T) uniqueResult(clazz, queryMap, null);
	}

	/**
	 * 按照条件查询，得到唯一的数据，如果查询的数据超过一个就抛出错误
	 * 
	 * @param clazz
	 * @param queryMap
	 * @param properties
	 * @return
	 */
	public Object uniqueResult(Class clazz, Map<String, Object> queryMap,
			String[] properties) {
		Criteria criteria = getCriteria(clazz, queryMap);
		setProjection(clazz, criteria, null, properties);
		criteria.setFirstResult(0);
		criteria.setMaxResults(2);
		List list = criteria.list();
		if (list.isEmpty()) {
			return null;
		} else if (list.size() == 1) {
			Object rtObj = list.get(0);
			if (rtObj instanceof HibernateProxy) {
				HibernateProxy proxy = (HibernateProxy) rtObj;
				LazyInitializer initializer = proxy
						.getHibernateLazyInitializer();

				rtObj = initializer.getSession().immediateLoad(
						initializer.getEntityName(),
						initializer.getIdentifier());

			}
			return rtObj;
		} else {
			throw new RuntimeException("所查询的数据不唯一");
		}

	}

	/**
	 * 按照条件查询返回符合条件的数据集合
	 * 
	 * @param clazz
	 * @param queryMap
	 * @param properties
	 * @return
	 */
	public List list(Class clazz, Map<String, Object> queryMap,
			String... properties) {
		Criteria criteria = getCriteria(clazz, queryMap);
		setProjection(clazz, criteria, null, properties);
		return criteria.list();
	}

	/**
	 * 按照条件查询返回符合条件的数据集合,数据集合比较大的时候，适合使用这种方式
	 * 
	 * @param clazz
	 * @param queryMap
	 * @param properties
	 * @return
	 */
	public Iterator iterator(Class clazz, Map<String, Object> queryMap,
			String... properties) {
		Criteria criteria = getCriteria(clazz, queryMap);
		setProjection(clazz, criteria, null, properties);
		return new CacheIterator(criteria);
	}

	public Criteria getCriteria(Class clazz) {
		return hibernateBaseDAO.getCriteria(clazz);
	}

	public Criteria getCriteria(Class clazz, String alias) {
		return hibernateBaseDAO.getCriteria(clazz, alias);
	}

	public Criteria getCriteria(Class clazz, Map<String, Object> queryMap) {
		Criteria criteria = hibernateBaseDAO.getCriteria(clazz);
		queryMap = transQuery("", clazz, queryMap);
		Map<String, Object> query = createQuery(clazz, queryMap, criteria);
		List<Criterion> criterions = getSearchCriterions(clazz.getSimpleName()
				.toLowerCase(), query);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		return criteria;
	}

	public void setSorts(Class<?> clazz, Criteria criteria, String sort,
			String... sorts) throws Exception {
		List<String> sortList = new ArrayList<String>();
		if (StringUtils.isNotEmpty(sort)) {
			sortList.add(sort);
		}
		if (sorts != null && sorts.length > 0) {
			sortList.addAll(Arrays.asList(sorts));
		}
		if (sortList.isEmpty()) {
			return;
		}
		Map<String, Object> query = new HashMap<String, Object>();
		for (String s : sortList) {
			boolean desc = false;
			if (s.startsWith("-")) {
				s = s.substring(1);
				desc = true;
			}
			query.put(s, "");
			String[] array = s.split("\\.");
			String sortProperty = array[array.length - 1];
			if (array.length > 1) {
				sortProperty = StringUtils.join(
						ArrayUtils.subarray(array, 0, array.length - 1), "_")
						+ "." + sortProperty;
			}
			if (desc) {
				criteria.addOrder(Order.desc(sortProperty));
			} else {
				criteria.addOrder(Order.asc(sortProperty));
			}
		}
		createQuery(clazz, query, criteria);
	}

	public void setProjection(Class<?> clazz, Criteria criteria,
			String property, String... properties) {
		List<String> propertyList = new ArrayList<String>();
		if (StringUtils.isNotEmpty(property)) {
			propertyList.add(property);
		}
		if (properties != null && properties.length > 0) {
			propertyList.addAll(Arrays.asList(properties));
		}
		if (propertyList.isEmpty()) {
			return;
		}
		Map<String, Object> query = new HashMap<String, Object>();
		ProjectionList projection = Projections.projectionList();
		for (String s : propertyList) {
			query.put(s, "");
			String[] array = s.split("\\.");
			String sortProperty = array[array.length - 1];
			if (array.length > 1) {
				sortProperty = StringUtils.join(
						ArrayUtils.subarray(array, 0, array.length - 1), "_")
						+ "." + sortProperty;
			}
			projection.add(Projections.property(sortProperty));
		}
		criteria.setProjection(projection);
		createQuery(clazz, query, criteria);
	}

	public Map<String, Object> createQuery(Class<?> clazz,
			Map<String, Object> queryMap, Criteria criteria) {
		try {
			Map<String, Object> query = new LinkedHashMap<String, Object>();
			for (String key : queryMap.keySet()) {
				if (StringUtils.isBlank(key)) {
					continue;
				}
				Object val = queryMap.get(key);
				if (val == null) {
					continue;
				}
				String[] keys = key.split("\\.");
				int end = keys.length - 1;
				String path = "";
				Class<?> cls = clazz;
				for (int i = 0; i < end; i++) {
					String s = keys[i];
					Field field = ReflectUtils.getField(cls, s);
					
					if(Collection.class.isAssignableFrom(field.getType())){
						List<FormField> fields=this.annotionService.getDomainFormDesc(cls.getSimpleName().toLowerCase());
						boolean isFind=false;
						if(fields!=null){
							
							for (FormField f : fields) {
								if(StringUtils.equals(f.getName(), s)){
									isFind=true;
									cls=this.annotionService.getDomainClass(f.getType().getRefDomainName()) ;
									break;
								}
							}
						}
						if(!isFind){
							cls=null;
						}
					}else{
						cls = field.getType();
					}
					path = createAlias(criteria,
							(StringUtils.isEmpty(path) ? "" : path + ".") + s);
				}
				if(cls == null){
					continue;
				}
				String dName = cls.getSimpleName().toLowerCase();
				String queryKey = null;

				String referenceFieldName = keys[end];
				Reference ref = this.annotionService.getReferences(dName,
						referenceFieldName);

				if (ref != null) {
					Field field = ReflectUtils
							.getField(cls, referenceFieldName);
					cls = field.getType();
					path = createAlias(criteria,
							(StringUtils.isEmpty(path) ? "" : path + ".")
									+ referenceFieldName);
					queryKey = path + "." + ref.id();
					field = ReflectUtils.getField(cls, ref.id());

					Object value = null;
					if (val instanceof Map) {
						Map<String,Object> valMap = (Map) val;
						if (valMap.containsKey(ref.id())) {
							value = dataConvter(field.getType(),
									valMap.get(ref.id()));
						}else{
							for (String tmpKey : valMap.keySet()) {
								try {
									Field subField = ReflectUtils.getField(
											cls, tmpKey);
									String tmpQueryKey = path + "." + tmpKey;
									Map newVal = new HashMap();
									newVal.put(
											"val",
											dataConvter(subField.getType(),
													valMap.get(tmpKey)));
									query.put(tmpQueryKey, newVal);
								} catch (NoSuchFieldException e) {
									// ignore
								}
							}
						}
					} else if (cls.isInstance(val)) {
						field.setAccessible(true);
						value = field.get(val);
					} else {
						value = dataConvter(field.getType(), val);
					}
					if (value != null) {
						Map newVal = new HashMap();
						newVal.put("val", value);
						query.put(queryKey, newVal);
					}
				} else {
					Field field = ReflectUtils.getField(cls, keys[end]);
					Class<?> fieldClass = field.getType();
					Map newVal = new HashMap();
					queryKey = path + (StringUtils.isEmpty(path) ? "" : ".")
							+ keys[end];

					if (val instanceof Map) {
						Map<String, Object> valMap = (Map) val;
						for (String k : valMap.keySet()) {
							if (!k.endsWith("val")) {
								newVal.put(k, valMap.get(k));
								continue;
							}
							Object fieldVal = valMap.get(k);
							if (fieldVal == null
									|| (fieldVal instanceof String && StringUtils
											.isEmpty((String) fieldVal))) {
								continue;
							}
							if (k.equals("inval") || k.equals("notinval")) {
								if (fieldVal instanceof Collection) {
									Collection collection = (Collection) fieldVal;
									List list = new ArrayList();
									for (Object object : collection) {
										Object value = dataConvter(fieldClass,
												object);
										if (value != null) {
											list.add(value);
										}
									}
									if (list.isEmpty()) {
										continue;
									}
									newVal.put(k, list);
								} else if (fieldVal.getClass().isArray()) {
									Object[] collection = (Object[]) fieldVal;
									List list = new ArrayList();
									for (Object object : collection) {
										Object value = dataConvter(fieldClass,
												object);
										if (value != null) {
											list.add(value);
										}
									}
									if (list.isEmpty()) {
										continue;
									}
									newVal.put(k, list);
								}
							} else if (k.equals("isnullval")) {
								newVal.put(k,
										dataConvter(Boolean.class, fieldVal));
							} else {
								Object value = dataConvter(fieldClass, fieldVal);
								if (value != null) {
									newVal.put(k, value);
								}
							}
						}
					} else if (val instanceof Collection) {
						String k = "inval";
						Collection collection = (Collection) val;
						if (collection.isEmpty()) {
							continue;
						}
						List list = new ArrayList();
						for (Object object : collection) {
							Object value = dataConvter(fieldClass, object);
							if (value != null) {
								list.add(value);
							}
						}
						if (list.isEmpty()) {
							continue;
						}
						newVal.put(k, list);
					} else if (val.getClass().isArray()) {
						String k = "inval";
						Object[] collection = (Object[]) val;
						if (collection.length == 0) {
							continue;
						}
						List list = new ArrayList();
						for (Object object : collection) {
							Object value = dataConvter(fieldClass, object);
							if (value != null) {
								list.add(value);
							}
						}
						if (list.isEmpty()) {
							continue;
						}
						newVal.put(k, list);
					} else {
						String k = "val";
						Object value = dataConvter(fieldClass, val);
						if (value != null) {
							newVal.put(k, value);
						}
					}
					if (!newVal.isEmpty() && null != queryKey) {
						if ((newVal.containsKey("notinval")
								|| newVal.containsKey("neval") || newVal
									.containsKey("isnullval"))
								&& queryKey.indexOf('.') != -1) {
							String alias = queryKey.substring(0,
									queryKey.indexOf('.'));
							changeAliasToLeftJoin(criteria, alias);
						}
						query.put(queryKey, newVal);
					}
				}

			}
			return query;
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public List<Criterion> getSearchCriterions(String domainName,
			Map<String, Object> queryMap) {
		try {
			List<Criterion> criterions = new ArrayList<Criterion>();
			List<Criterion> orCriterions = new ArrayList<Criterion>();
			for (Object key : queryMap.keySet()) {
				List<Criterion> subCriterions = new ArrayList<Criterion>();
				Map<String, Object> val = (Map<String, Object>) queryMap
						.get(key);
				String fieldName = (String) key;
				boolean fuzzy = false;
				if (val.containsKey("fuzzy")) {
					fuzzy = BooleanUtils.toBoolean(String.valueOf(val
							.get("fuzzy")));
				}
				boolean or = false;
				if (val.containsKey("or")) {
					or = BooleanUtils.toBoolean(String.valueOf(val.get("or")));
				}
				if (val.containsKey("val")) {
					Object fieldVal = val.get("val");
					if (fieldVal != null
							|| (fieldVal instanceof String && StringUtils
									.isEmpty((String) fieldVal))) {
						if (fieldVal instanceof String
								&& StringUtils.isNotEmpty((String) fieldVal)) {
							if (fuzzy) {
								subCriterions.add(Restrictions.like(fieldName,
										"%" + fieldVal + "%"));
							} else {
								subCriterions.add(Restrictions.eq(fieldName,
										fieldVal));
							}
						} else if (!(fieldVal instanceof String)) {
							subCriterions.add(Restrictions.eq(fieldName,
									fieldVal));
						}
					}
				}
				if (val.containsKey("lval") || val.containsKey("rval")) {
					Object lfieldVal = val.get("lval");
					Object rfieldVal = val.get("rval");

					if (lfieldVal != null) {
						if (lfieldVal instanceof String
								&& StringUtils.isNotEmpty((String) lfieldVal)) {
							subCriterions.add(Restrictions.ge(fieldName,
									lfieldVal));
						} else {
							subCriterions.add(Restrictions.ge(fieldName,
									lfieldVal));
						}
					}
					if (rfieldVal != null) {
						if (rfieldVal instanceof String
								&& StringUtils.isNotEmpty((String) rfieldVal)) {
							subCriterions.add(Restrictions.le(fieldName,
									rfieldVal));
						} else {
							subCriterions.add(Restrictions.le(fieldName,
									rfieldVal));
						}
					}
				}
				if (val.containsKey("inval")) {
					Object inFieldval = val.get("inval");
					if (inFieldval != null && inFieldval instanceof Collection) {
						Collection collection = (Collection) inFieldval;
						if (!collection.isEmpty()) {
							// subCriterions.add(Restrictions
							// .in(fieldName, collection));
							Criterion[] crits = new Criterion[collection.size()];
							int i = 0;
							for (Object object : collection) {
								crits[i] = Restrictions.eq(fieldName, object);
								i++;
							}
							subCriterions.add(Restrictions.and(Restrictions
									.or(crits)));
						}
					}
				}
				if (val.containsKey("notinval")) {
					Object inFieldval = val.get("notinval");
					if (inFieldval != null && inFieldval instanceof Collection) {
						Collection collection = (Collection) inFieldval;
						if (!collection.isEmpty()) {
							// subCriterions.add(Restrictions.not(Restrictions.in(
							// fieldName, collection)));
							Criterion[] crits = new Criterion[collection.size()];
							int i = 0;
							for (Object object : collection) {
								crits[i] = Restrictions.ne(fieldName, object);
								i++;
							}
							// subCriterions.add(Restrictions.and(crits));
							subCriterions.add(Restrictions.or(
									Restrictions.and(crits),
									Restrictions.isNull(fieldName)));
						}
					}
				}
				if (val.containsKey("neval")) {
					Object notFieldval = val.get("neval");
					if (fuzzy) {
						if (notFieldval instanceof String) {
							subCriterions.add(Restrictions.not(Restrictions
									.like(fieldName, "%" + notFieldval + "%")));
						}
					} else {
						// subCriterions.add(Restrictions.ne(fieldName,
						// notFieldval));
						subCriterions.add(Restrictions.or(
								Restrictions.ne(fieldName, notFieldval),
								Restrictions.isNull(fieldName)));
					}

				}
				if (val.containsKey("isnullval")) {
					Boolean isnullFieldval = (Boolean) val.get("isnullval");
					if (isnullFieldval) {
						subCriterions.add(Restrictions.isNull(fieldName));
					} else {
						subCriterions.add(Restrictions.isNotNull(fieldName));
					}
				}

				if (!subCriterions.isEmpty()) {
					if (or) {
						// if (or && !criterions.isEmpty()) {
						// List<Criterion> tmp = new ArrayList<Criterion>(
						// criterions);
						// criterions.clear();
						// criterions
						// .add(Restrictions.or(Restrictions.and(tmp
						// .toArray(new Criterion[0])),
						// Restrictions.and(subCriterions
						// .toArray(new Criterion[0]))));
						orCriterions.add(Restrictions.and(subCriterions
								.toArray(new Criterion[0])));
					} else {
						criterions.addAll(subCriterions);
					}
				}
			}
			if (!orCriterions.isEmpty()) {
				criterions.add(Restrictions.or(orCriterions
						.toArray(new Criterion[0])));
			}
			return criterions;
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public String createAlias(Criteria criteria, String path) {
		CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
		Iterator<Subcriteria> it = criteriaImpl.iterateSubcriteria();
		boolean find = false;
		String alias = path.replaceAll("\\.", "_");
		while (!find && it.hasNext()) {
			Subcriteria subcriteria = it.next();
			find = subcriteria.getAlias().equals(alias);
		}
		if (!find) {
			criteria.createAlias(path, alias, JoinType.INNER_JOIN);
		}
		return alias;
	}

	private Map<String, Object> transQuery(final String path, Class clazz,
			Map<String, Object> queryMap) {
		// TODO Auto-generated method stub
		if (clazz == null) {
			return Collections.EMPTY_MAP;
		}
		Map rtMap = new LinkedHashMap();
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
//			LOGGER.error(e.getMessage(), e);
			rtMap.clear();
			if (StringUtils.isNotEmpty(path)) {
				rtMap.put(path, queryMap);
			}
		}
		return rtMap;
	}

	private String changeAliasToLeftJoin(Criteria criteria, String alias) {
		CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
		Iterator<Subcriteria> it = criteriaImpl.iterateSubcriteria();
		boolean find = false;
		boolean ischange = false;
		String path = null;
		while (!find && it.hasNext()) {
			Subcriteria subcriteria = it.next();
			path = subcriteria.getPath();
			find = subcriteria.getAlias().equals(alias);
			if (find) {
				ischange = subcriteria.getJoinType() != JoinType.LEFT_OUTER_JOIN;
				if (ischange) {
					it.remove();
				}
				break;
			}
		}
		if (ischange) {
			criteria.createAlias(path, alias, JoinType.LEFT_OUTER_JOIN);
		}
		return alias;
	}

	private Object dataConvter(Class type, Object object) {
		if (type == null || object == null) {
			return null;
		}
		if (type.equals(String.class)) {
			String str = object.toString();
			if (StringUtils.isEmpty(str)) {
				str = null;
			}
			return str;
		} else if (Number.class.isAssignableFrom(type)) {
			Number number;
			if (object instanceof Number) {
				number = (Number) object;
			} else if (NumberUtils.isNumber(object.toString())) {
				number = new BigDecimal(object.toString());
			} else {
				return null;
			}

			if (type.equals(Double.class) || type.equals(double.class)) {
				return number.doubleValue();
			} else if (type.equals(Integer.class) || type.equals(int.class)) {
				return number.intValue();
			} else if (type.equals(Long.class) || type.equals(long.class)) {
				return number.longValue();
			} else if (type.equals(Short.class) || type.equals(short.class)) {
				return number.shortValue();
			}
			return null;
		} else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
			if (object instanceof Boolean) {
				return (Boolean) object;
			}
			return BooleanUtils.toBoolean(object.toString());
		} else if (type.equals(Date.class) || Date.class.isAssignableFrom(type)) {
			if (object instanceof Date) {
				return object;
			} else {
				try {
					return SDF.parse(object.toString());
				} catch (Exception e) {
					// TODO: handle exceptionm
					return null;
				}
			}
		} else if (type.isEnum()) {
			try {
				Object[] enums = type.getEnumConstants();
				if (NumberUtils.isNumber(object.toString())) {
					int i = new BigDecimal(object.toString()).intValue();
					for (Object obj : enums) {
						Enum e = (Enum) obj;
						if (e.ordinal() == i) {
							return e;
						}
					}
				} else {
					String name = object.toString();
					for (Object obj : enums) {
						Enum e = (Enum) obj;
						if (StringUtils.equals(e.name(), name)) {
							return e;
						}
					}
				}
				return null;
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}

		} else {
			return null;
		}
	}

	private class CacheIterator implements Iterator {

		private final Criteria criteria;

		private final List cache = new ArrayList();

		private final static int CACHE_SIZE = 100;

		private boolean isComplete = false;

		private int curSize = 0;

		private CacheIterator(Criteria criteria) {
			this.criteria = criteria;
			hasNext();
		}

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			if (!isComplete && cache.isEmpty()) {
				criteria.setFirstResult(curSize);
				criteria.setMaxResults(curSize + CACHE_SIZE);
				curSize = curSize + CACHE_SIZE;
				cache.addAll(criteria.list());
				isComplete = cache.size() < CACHE_SIZE;
				hibernateBaseDAO.flush();
				hibernateBaseDAO.clear();
			}
			return !cache.isEmpty();
		}

		@Override
		public Object next() {
			// TODO Auto-generated method stub
			if (hasNext()) {
				return cache.remove(0);
			} else {
				return null;
			}
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			throw new RuntimeException("remove not supported");
		}

	}
}
