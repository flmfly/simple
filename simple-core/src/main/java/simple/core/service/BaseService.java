package simple.core.service;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import simple.config.annotation.AutoFill;
import simple.config.annotation.DataFilter;
import simple.config.annotation.DictField;
import simple.config.annotation.Reference;
import simple.config.annotation.RepresentationLayout;
import simple.config.annotation.Synchronization;
import simple.config.annotation.TreeInfo;
import simple.config.annotation.UpdateStrategy;
import simple.config.annotation.UpdateStrategyType;
import simple.config.annotation.support.OperationHandler.OperationException;
import simple.config.annotation.support.OperationHandler.OperationResult;
import simple.core.dao.HibernateBaseDAO;
import simple.core.model.DomainDesc;
import simple.core.model.DomainMessage;
import simple.core.model.FormField;
import simple.core.model.OneToMany;
import simple.core.model.Page;
import simple.core.model.ReferenceData;
import simple.core.model.SpecialData;
import simple.core.model.TableColumnDesc;
import simple.core.model.annotation.DataFilterDesc;
import simple.core.model.annotation.SearchFieldDesc;
import simple.core.service.listener.DomainChangedListener;
import simple.core.util.GenericUtils;
import simple.core.util.ReflectUtils;
import simple.core.util.gson.HibernateProxyTypeAdapter;
import simple.core.util.gson.IgnoreManyToManyExclusionStrategy;
import simple.core.util.gson.UserExclusionStrategy;

@Service
public class BaseService {

	protected static final Log LOG = LogFactory.getLog(BaseService.class);
	protected static final Gson GSON;
	protected static final Gson GSON_FULL;
	protected static final Gson DEFAULT_GSON;

	static {

		DEFAULT_GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		GSON = new GsonBuilder()
				.setExclusionStrategies(new IgnoreManyToManyExclusionStrategy(),
						UserExclusionStrategy.EXCLUSION_STRATEGY)
				.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).setDateFormat("yyyy-MM-dd HH:mm:ss")
				.create();
		GSON_FULL = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
				.setExclusionStrategies(UserExclusionStrategy.EXCLUSION_STRATEGY)
				.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).create();
	}

	@Autowired
	protected HibernateBaseDAO hibernateBaseDAO;

	@Autowired
	protected DataInitializeService dataInitializeService;

	@Autowired
	protected AnnotationService annotionService;

	@Autowired
	protected DomainChangedService domainChangedService;

	@Autowired
	private DefaultValueService defaultValueService;

	@Autowired
	private AutoFillService autoFillService;

	@Autowired
	private TableColumnService tableColumnService;

	@Autowired
	private ExtentionPointService extentionPointService;

	@Autowired
	private OperationService operationService;

	@Autowired
	private DomainHandlerService domainHandlerService;

	@Autowired
	private FieldHandlerService fieldHandlerService;

	@Autowired
	private HttpSessionService httpSessionService;

	/** The validator. */
	@Autowired
	protected Validator validator;

	@Autowired
	private CriteriaService criteriaService;

	List<Synchronization> getSynchronization(String domainName) {
		return this.annotionService.getSynchronizationMap(domainName);
	}

	String getDomainNameByClass(Class<?> clazz) {
		return this.annotionService.getDomainNameByClass(clazz);
	}

	public List<OneToMany> getOneToMany(String domainName) {
		return this.annotionService.getOneToMany(domainName);
	}

	public List<String> getBusinessKey(String domainName) {
		return this.annotionService.getBusinessKey(domainName);
	}

	public Reference getReferenceByRefName(String domainName, String refName) {
		return this.annotionService.getReferences(domainName, refName);
	}

	protected String getSerializer(String domainName) {
		return this.annotionService.getSerializer(domainName);
	}

	protected boolean hasDomain(String domainName) {
		return this.annotionService.hasDomain(domainName);
	}

	public void evit(Object obj) {
		this.hibernateBaseDAO.evict(obj);
	}

	public Collection<String> insertDomain(String domainName, Object domain, HttpServletRequest request)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException,
			IllegalArgumentException, NoSuchFieldException {
		this.autoFillDomain(domainName, domain, request);
		List<Object> insertList = new ArrayList<Object>();
		String tagsName = this.annotionService.getTagsInsert(domainName);

		if (null != tagsName) {
			String str = (String) this.getValue(domain, tagsName);
			if (null != str) {
				StringTokenizer st = new StringTokenizer(str, ", ");
				while (st.hasMoreTokens()) {
					Object duplicated = this.getDomainClass(domainName).newInstance();
					BeanUtils.copyProperties(domain, duplicated);
					this.setValue(duplicated, tagsName, st.nextToken());
					insertList.add(duplicated);
				}
			} else {
				insertList.add(domain);
			}
		} else {
			insertList.add(domain);
		}

		Set<String> errorSet = new LinkedHashSet<String>();

		// for (Object obj : insertList) {
		// errorSet.addAll(this.validate(domainName, obj));
		// }

		if (!errorSet.isEmpty()) {
		} else {
			for (Object obj : insertList) {
				this.update(obj, true);
			}
		}

		return errorSet;
	}

	@SuppressWarnings("unchecked")
	public List<String> operate(String domainName, String operationCode, String data) {
		List<String> messages = new ArrayList<String>();
		Map<?, ?> dataMap = this.getObjectFromJson(data, Map.class);

		Set<Serializable> idSet = new HashSet<Serializable>();
		for (Object object : (Collection<?>) dataMap.get("ids")) {
			idSet.add(Long.parseLong(object.toString()));
		}

		try {
			OperationResult or = this.operationService.process(this.annotionService.getDomainDesc(domainName),
					operationCode, (Map<String, Object>) dataMap.get("parameters"),
					(List<Object>) this.hibernateBaseDAO.findMultipe(this.getDomainClass(domainName), idSet));
			if (null != or && !or.isSuccess()) {
				messages.addAll(or.getErrorMessages());
			}
		} catch (Exception e) {
			messages.add(e.getMessage());
		}
		if (!messages.isEmpty()) {
			throw new OperationException(messages);
		}
		return messages;
	}

	public List<String> validate(String domainName, Object domain) {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(domain);

		List<String> errorList = new ArrayList<String>();
		List<String> globeErrorList = new ArrayList<String>();

		if (!constraintViolations.isEmpty()) {
			Map<String, String> propErrorMap = new HashMap<String, String>();

			for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
				if ("".equals(constraintViolation.getPropertyPath().toString())) {
					globeErrorList.add(constraintViolation.getMessage());
				} else {
					propErrorMap.put(constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessage());
				}
			}

			errorList.addAll(globeErrorList);

			List<FormField> fields = this.annotionService.getDomainFormDesc(domainName);
			for (FormField formField : fields) {
				String key = formField.getName();
				if (propErrorMap.containsKey(key)) {
					errorList.add(propErrorMap.get(key));
				}
			}
		}
		return errorList;
	}

	public List<String> validateWithPropertyPath(String domainName, Object domain) {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(domain);

		List<String> errorList = new ArrayList<String>();
		List<String> globeErrorList = new ArrayList<String>();

		if (!constraintViolations.isEmpty()) {
			Map<String, String> propErrorMap = new HashMap<String, String>();

			for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
				if ("".equals(constraintViolation.getPropertyPath().toString())) {
					globeErrorList.add(constraintViolation.getMessage());
				} else {
					propErrorMap.put(constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessage());
				}
			}

			errorList.addAll(globeErrorList);

			List<FormField> fields = this.annotionService.getDomainFormDesc(domainName);
			for (FormField formField : fields) {
				String key = formField.getName();
				if (propErrorMap.containsKey(key)) {
					errorList.add("[" + formField.getTitle() + "]" + propErrorMap.get(key));
				}
			}
		}
		return errorList;
	}

	public <T> T getObjectFromJson(String json, Class<T> classOfT) {
		return DEFAULT_GSON.fromJson(json, classOfT);
	}

	public String toJsonStr(Object obj) {
		return GSON.toJson(obj);
	}

	public String toJsonStrWithSubs(Object obj) {
		return GSON_FULL.toJson(obj);
	}

	// public static void main(String[] args) throws NoSuchFieldException,
	// SecurityException {
	// Field field = BaseRole.class.getDeclaredField("users");
	// ParameterizedType pt = (ParameterizedType) field.getGenericType();
	// System.out.println(pt.getActualTypeArguments()[0]);
	// }

	public Object getValue(Object target, String desc) {
		try {
			String[] paths = desc.split("\\.");
			Object targetObj = target;
			Object rtn = null;
			for (int i = 0; i < paths.length; i++) {
				String path = paths[i];
				Field field = ReflectUtils.getField(targetObj.getClass(), path);
				field.setAccessible(true);
				rtn = field.get(targetObj);
				if (null == rtn) {
					break;
				}
				if (i != (paths.length - 1) && rtn instanceof Collection) {
					Collection c = (Collection) rtn;
					String subdesc = StringUtils.join(Arrays.copyOfRange(paths, i + 1, paths.length), ".");
					List list = new ArrayList();
					for (Object o : c) {
						Object rtSub = getValue(o, subdesc);
						if (rtSub == null) {
							continue;
						}
						if (rtSub instanceof Collection) {
							list.addAll((Collection) rtSub);
						} else {
							list.add(rtSub);
						}
					}
					return list;
				} else {
					targetObj = rtn;
				}

			}

			return rtn;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

	}

	private void setValue(Object target, String desc, Object val)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = ReflectUtils.getField(target.getClass(), desc);
		field.setAccessible(true);
		field.set(target, val);
	}

	public Collection<?> getSubList(String domainName, long id, String refName) {
		Class<?> clazz = this.getDomainClass(domainName);

		Object obj = this.get(clazz, id);
		try {
			Field field = ReflectUtils.getField(clazz, refName);
			field.setAccessible(true);
			return (Collection<?>) field.get(obj);
		} catch (Exception e) {
		}

		return Collections.emptySet();
	}

	public Map<String, List<ReferenceData>> getSelectTree(String domainName, long id, String propertyName) {
		Class<?> clazz = this.getDomainClass(domainName);

		Object obj = this.get(clazz, id);

		List<ReferenceData> existList = new ArrayList<ReferenceData>();
		List<ReferenceData> otherList = new ArrayList<ReferenceData>();

		Map<String, List<ReferenceData>> rtn = new HashMap<String, List<ReferenceData>>();
		rtn.put("exist", existList);
		rtn.put("other", otherList);
		try {
			Field field = ReflectUtils.getField(clazz, propertyName);
			field.setAccessible(true);
			Class<?> entryClass = GenericUtils.getCollectionElementGeneric(field);
			TreeInfo treeInfo = entryClass.getAnnotation(TreeInfo.class);
			boolean hasPid = !"".equals(treeInfo.pid());

			Map<Long, ReferenceData> existRefMap = new LinkedHashMap<Long, ReferenceData>();

			Set<?> treeSet = (Set<?>) field.get(obj);
			for (Object entry : treeSet) {
				Long eid = Long.parseLong(String.valueOf(this.getValue(entry, treeInfo.id())));
				String label = this.getValue(entry, treeInfo.label()).toString();
				Long pid = null;
				if (hasPid) {
					pid = Long.parseLong(String.valueOf(this.getValue(entry, treeInfo.pid())));
				}

				ReferenceData pRefData = new ReferenceData();
				pRefData.setId(eid);
				pRefData.setText(label);
				pRefData.setPid(pid);
				existRefMap.put(id, pRefData);
			}
			for (ReferenceData refData : existRefMap.values()) {
				if (null == refData.getPid()) {
					existList.add(refData);
				} else {
					if (null != existRefMap.get(refData.getPid())) {
						existRefMap.get(refData.getPid()).addChild(refData);
					} else {
						existList.add(refData);
					}
				}
			}

			Map<Long, ReferenceData> refMap = new LinkedHashMap<Long, ReferenceData>();
			// get all
			List<?> entryList = this.find(entryClass);
			for (Object entry : entryList) {
				Long eid = Long.parseLong(String.valueOf(this.getValue(entry, treeInfo.id())));
				if (existRefMap.containsKey(eid)) {
					continue;
				}
				String label = this.getValue(entry, treeInfo.label()).toString();
				Long pid = null;
				if (hasPid) {
					Object pidObj = this.getValue(entry, treeInfo.pid());
					if (null != pidObj) {
						pid = Long.parseLong(String.valueOf(pidObj));
					}
				}

				ReferenceData pRefData = new ReferenceData();
				pRefData.setId(eid);
				pRefData.setText(label);
				pRefData.setPid(pid);
				refMap.put(eid, pRefData);
			}

			// remove exists entries
			// for (ReferenceData refData : refMap.values()) {
			// Long pid = refData.getPid();
			// while (null != pid) {
			// if (!refMap.containsKey(pid)) {
			// ReferenceData tmp = existRefMap.get(pid);
			// ReferenceData ntmp = new ReferenceData();
			// ntmp.setId(tmp.getId());
			// ntmp.setPid(tmp.getPid());
			// ntmp.setText(tmp.getText());
			// refMap.put(ntmp.getId(), ntmp);
			// pid = tmp.getId();
			// } else {
			// break;
			// }
			// }
			// }

			for (ReferenceData refData : refMap.values()) {
				if (null == refData.getPid()) {
					otherList.add(refData);
				} else {
					refMap.get(refData.getPid()).addChild(refData);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rtn;
	}

	@SuppressWarnings("unchecked")
	public List<ReferenceData> getTree(String domainName, String propertyName, String propId) {
		List<ReferenceData> rtn = new ArrayList<ReferenceData>();
		try {
			RepresentationLayout layout = this.annotionService.getRepresentationLayout(domainName);
			Class<?> clazz = this.getDomainClass(domainName);

			StringBuffer filter = new StringBuffer();
			String join = null;
			String path = "";
			if (propId != null) {
				int p = propertyName.indexOf('.');
				if (p != -1) {
					String joinName = propertyName.substring(0, p);
					if (Collection.class.isAssignableFrom(ReflectUtils.getField(clazz, joinName).getType())) {
						path = domainName + ".";
						join = " inner join " + path + joinName + " as " + joinName;
					}
				}
				filter.append(propertyName + ".id=" + propId);
			}

			List<String> propertyList = new ArrayList<String>();
			propertyList.add(path + (null == layout ? "id" : layout.id()));
			propertyList.add(path + (null == layout ? "name" : layout.label()));

			boolean hasPid = !"".equals(null == layout ? "" : layout.pid());

			if (hasPid) {
				propertyList.add(layout.pid());
			}

			Map<Long, ReferenceData> refMap = new LinkedHashMap<Long, ReferenceData>();

			DataFilter dataFilter = clazz.getAnnotation(DataFilter.class);
			if (dataFilter != null) {
				Object loginUser = httpSessionService.getLoginUser();
				if (loginUser != null && !StringUtils.equals("admin", httpSessionService.getLoginUserAccount())) {
					if (filter.length() > 0) {
						filter.append(" and ");
					}
					String through = dataFilter.through();
					Class<?> throughClass = null;
					try {
						throughClass = Class.forName(through);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if (!"".equals(through) && null != throughClass) {
						List<Criterion> cs = new ArrayList<Criterion>();
						cs.add(Restrictions.eq(dataFilter.userProperty(), loginUser));
						List<Object> valueList = this.hibernateBaseDAO.find(throughClass, cs);
						if (valueList.isEmpty()) {
						} else {
							Set<Object> valueSet = new HashSet<Object>();
							for (Object obj : valueList) {
								try {
									Object o = this.getValue(obj, dataFilter.valueProperty());
									if (o == null) {
										continue;
									}
									if (o instanceof Collection) {
										valueSet.addAll((Collection) o);
									} else {
										valueSet.add(o);
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							filter.append(dataFilter.by());
							if (valueSet.size() > 0) {
								filter.append(" in (").append(StringUtils.join(valueSet, ",")).append(")");
							} else {
								filter.append(" is nulll ");
							}
						}
					}
				}
			}
			List<Object[]> entityList = (List<Object[]>) this.hibernateBaseDAO.findByProperties(clazz, join,
					StringUtils.join(propertyList, ","), filter.toString(), null, null);
			for (Object[] entity : entityList) {
				Long id = Long.parseLong(String.valueOf(entity[0]));
				String label = String.valueOf(entity[1]);
				Long pid = null;
				if (hasPid && null != entity[2]) {
					pid = Long.parseLong(String.valueOf(entity[2]));
				}

				ReferenceData pRefData = new ReferenceData();
				pRefData.setId(id);
				pRefData.setText(label);
				pRefData.setPid(pid);
				refMap.put(id, pRefData);
			}

			for (ReferenceData refData : refMap.values()) {
				if (null == refData.getPid()) {
					rtn.add(refData);
				} else {
					if (null != refMap.get(refData.getPid())) {
						refMap.get(refData.getPid()).addChild(refData);
					} else {
						rtn.add(refData);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rtn;
	}

	@SuppressWarnings("unchecked")
	public List<ReferenceData> getReference(String domainName, String refName) {
		List<ReferenceData> rtn = new ArrayList<ReferenceData>();
		try {
			Class<?> clazz = ReflectUtils.getField(this.getDomainClass(domainName), refName).getType();
			Reference ref = this.annotionService.getReferences(domainName, refName);

			List<String> propertyList = new ArrayList<String>();
			propertyList.add(ref.id());
			propertyList.add(ref.label());

			boolean hasPid = !"".equals(ref.pid());

			if (hasPid) {
				propertyList.add(ref.pid());
			}

			Field refField = ReflectUtils.getField(this.getDomainClass(domainName), refName);
			StringBuffer filter = new StringBuffer(ref.filter());
			DictField dictField = refField.getAnnotation(DictField.class);
			if (dictField != null) {
				if (filter.length() > 0) {
					filter.append(" and ");
				}
				filter.append("dict.code='").append(dictField.value()).append("'");
			}
			DataFilter dataFilter = refField.getAnnotation(DataFilter.class);

			if (dataFilter != null) {

				Object loginUser = httpSessionService.getLoginUser();
				if (loginUser != null && !StringUtils.equals("admin", httpSessionService.getLoginUserAccount())) {
					if (filter.length() > 0) {
						filter.append(" and ");
					}
					String through = dataFilter.through();
					Class<?> throughClass = null;
					try {
						throughClass = Class.forName(through);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if (!"".equals(through) && null != throughClass) {
						List<Criterion> cs = new ArrayList<Criterion>();
						cs.add(Restrictions.eq(dataFilter.userProperty(), loginUser));
						List<Object> valueList = this.hibernateBaseDAO.find(throughClass, cs);
						if (valueList.isEmpty()) {
						} else {
							Set<Object> valueSet = new HashSet<Object>();
							for (Object obj : valueList) {
								try {
									Object o = this.getValue(obj, dataFilter.valueProperty());
									if (o == null) {
										continue;
									}
									if (o instanceof Collection) {
										valueSet.addAll((Collection) o);
									} else {
										valueSet.add(o);
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							filter.append(dataFilter.by());
							if (valueSet.size() > 0) {
								filter.append(" in (").append(StringUtils.join(valueSet, ",")).append(")");
							} else {
								filter.append(" is nulll ");
							}
						}
					}
				}
			}
			Map<Long, ReferenceData> refMap = new LinkedHashMap<Long, ReferenceData>();
			List<Object[]> entityList = (List<Object[]>) this.hibernateBaseDAO.findByProperties(clazz, propertyList,
					filter.toString());
			for (Object[] entity : entityList) {
				Long id = Long.parseLong(String.valueOf(entity[0]));
				String label = String.valueOf(entity[1]);
				Long pid = null;
				if (hasPid && null != entity[2]) {
					pid = Long.parseLong(String.valueOf(entity[2]));
				}

				ReferenceData pRefData = new ReferenceData();
				pRefData.setId(id);
				pRefData.setText(label);
				pRefData.setPid(pid);
				refMap.put(id, pRefData);
			}

			for (ReferenceData refData : refMap.values()) {
				if (null == refData.getPid()) {
					rtn.add(refData);
				} else {
					refMap.get(refData.getPid()).addChild(refData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rtn;
	}

	public Object getDomainFromPostJosn(String json, String domainName) {
		return DEFAULT_GSON.fromJson(json, this.annotionService.getDomainClass(domainName));
	}

	public Class<?> getDomainClass(String domainName) {
		return this.annotionService.getDomainClass(domainName);
	}

	public List<FormField> getDomainFormDesc(String domainName, HttpServletRequest request) {
		return this.defaultValueService.setDefaultValue(domainName, this.annotionService.getDomainFormDesc(domainName),
				request);
	}

	public DomainDesc getDomainDesc(String domainName) {
		return this.annotionService.getDomainDesc(domainName);
	}

	public List<FormField> getDomainFormDesc(String domainName) {
		return this.annotionService.getDomainFormDesc(domainName);
	}

	public List<SearchFieldDesc> getDomainSearchDesc(String domainName) {
		return this.annotionService.getDomainSearchDesc(domainName);
	}

	public List<TableColumnDesc> getDomainTableDesc(String domainName) {
		return this.annotionService.getDomainTableDesc(domainName);
	}

	public Map<Field, AutoFill> getAutoFill(String domainName) {
		return this.annotionService.getAutoFill(domainName);
	}

	public void autoFillDomain(String domainName, Object domain, HttpServletRequest request)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.autoFillService.autoFillDomain(domainName, domain, request);
	}

	public void save(Object entity) {
		this.save(entity, false);
	}

	@SuppressWarnings("unchecked")
	public void save(Object entity, boolean isValidate) {
		try {
			// get the entity class
			Class<?> clazz = entity.getClass();

			Field entityIdField = ReflectUtils.getField(clazz, "id");
			entityIdField.setAccessible(true);

			Map<OneToMany, Set<?>> subSetMap = new HashMap<OneToMany, Set<?>>();

			Map<OneToMany, UpdateStrategy> updateStrategyMap = new HashMap<OneToMany, UpdateStrategy>();

			// if has any one to many properties
			for (OneToMany oneToMany : this.annotionService.getOneToMany(clazz.getSimpleName().toLowerCase())) {
				// get property filed
				Field subField = ReflectUtils.getField(clazz, oneToMany.getFieldName());
				subField.setAccessible(true);

				boolean isManyToMany = (null == oneToMany.getSubFieldName());

				if (!isManyToMany) {
					// pick up the sub data
					// subSetMap.put(oneToMany, (Set<?>) subField.get(entity));
					Set<?> subSet = (Set<?>) subField.get(entity);
					if (subSet != null) {
						subSetMap.put(oneToMany, new HashSet(subSet));
					} else {
						subSetMap.put(oneToMany, new HashSet(0));
					}
					// remove them from entity
					UpdateStrategy us = subField.getAnnotation(UpdateStrategy.class);
					if (null != us) {
						updateStrategyMap.put(oneToMany, us);
					}
				} else {
					Field idField = ReflectUtils.getField(oneToMany.getType(), "id");
					idField.setAccessible(true);
					Set<Object> subSet = (Set<Object>) subField.get(entity);
					Set<Object> tepSet = new HashSet<Object>();

					tepSet.addAll(subSet);
					subSet.clear();

					for (Object obj : tepSet) {
						Long id = (Long) idField.get(obj);
						if (id != null) {
							subSet.add(this.hibernateBaseDAO.get(oneToMany.getType(), id));
						} else {
							subSet.add(obj);
						}
					}
				}
			}
			// 数据保存先校验数据有效性，主要是在接口过来的数据时。
			if (isValidate) {
				List<String> errorList = this.validate(clazz.getSimpleName().toLowerCase(), entity);
				if (!errorList.isEmpty()) {
					throw new Exception(StringUtils.join(errorList, ";"));
				}
			}

			for (OneToMany oneToMany : this.annotionService.getOneToMany(clazz.getSimpleName().toLowerCase())) {
				Field subField = ReflectUtils.getField(clazz, oneToMany.getFieldName());
				subField.setAccessible(true);
				boolean isManyToMany = (null == oneToMany.getSubFieldName());
				if (!isManyToMany) {
					subField.set(entity, null);
				}
			}
			// save the entity, this will set the id automatically if does not
			// set
			this.hibernateBaseDAO.save(entity);
			this.hibernateBaseDAO.flush();
			for (OneToMany o2m : subSetMap.keySet()) {
				// id field
				Field idField = ReflectUtils.getField(o2m.getType(), "id");
				idField.setAccessible(true);

				// the field that entity store in sub class
				Field entityField = ReflectUtils.getField(o2m.getType(), o2m.getSubFieldName());
				entityField.setAccessible(true);

				Map<Long, Object> updateMap = new HashMap<Long, Object>();
				Set<?> subSet = subSetMap.get(o2m);
				// 防止空指针错误
				if (subSet == null) {
					subSet = new HashSet(0);
				}
				for (Object object : subSet) {
					entityField.set(object, entity);
					Object id = idField.get(object);
					if (null != id) {
						updateMap.put((Long) id, object);
					}
				}

				UpdateStrategy us = updateStrategyMap.get(o2m);
				UpdateStrategyType strategyType = UpdateStrategyType.OVERWRITE;
				if (us != null) {
					strategyType = us.value();
				}
				boolean delta = (strategyType == UpdateStrategyType.DELTA);
				Map<String, Object> queryMap = new LinkedHashMap<String, Object>();
				queryMap.put(o2m.getSubFieldName() + ".id", ((Long) entityIdField.get(entity)).longValue());
				if (!updateMap.isEmpty()) {
					if (delta) {
						queryMap.put("id", updateMap.keySet().toArray());
					} else {
						Map<String, Object> notinval = new LinkedHashMap<String, Object>();
						notinval.put("notinval", updateMap.keySet().toArray());
						queryMap.put("id", notinval);
					}
				}

				if (delta && !updateMap.isEmpty()) {
					List<?> dataList = criteriaService.list(o2m.getType(), queryMap);
					for (Object object : dataList) {
						Long id = (Long) idField.get(object);
						if (updateMap.containsKey(id)) {
							this.hibernateBaseDAO.evict(object);
							this.copyProperties(object.getClass(), object, updateMap.get(id));
						}
					}
				} else if (!delta) {
					List<?> dataList = criteriaService.list(o2m.getType(), queryMap);
					for (Object object : dataList) {
						this.hibernateBaseDAO.delete(object);
					}
				}
				// String hql = "from " + o2m.getType().getName() + " where "
				// + o2m.getSubFieldName() + ".id="
				// + ((Long) entityIdField.get(entity)).longValue();
				// List<?> dataList = this.hibernateBaseDAO.findByHql(
				// o2m.getType(), hql);
				// // Map<Long, Object> dataMap = new HashMap<Long, Object>();
				// for (Object object : dataList) {
				// // dataMap.put((Long) idField.get(object), object);
				// Long id = (Long) idField.get(object);
				// if (updateMap.containsKey(id)) {
				// this.hibernateBaseDAO.evict(object);
				// if (delta) {
				// this.copyProperties(object.getClass(), object,
				// updateMap.get(id));
				// }
				// } else if (!delta) {
				// this.hibernateBaseDAO.delete(object);
				// }
				// }
				for (Object object : subSet) {
					this.save(object, isValidate);
				}
				// afterSave能得到子表数据
				this.hibernateBaseDAO.evict(entity);
				Field subField = ReflectUtils.getField(clazz, o2m.getFieldName());
				subField.setAccessible(true);
				subField.set(entity, subSet);
			}

			// TODO: handle sub table data change
			for (DomainChangedListener domainChangedListener : domainChangedService.getDomainChangedListenerList()) {
				domainChangedListener.applyChange(DomainChangedListener.DOMAIN_UPDATED, entity);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// TransactionAspectSupport.currentTransactionStatus()
			// .setRollbackOnly();
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	private void copyProperties(Class<?> clazz, Object source, Object target)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		for (Field field : fields) {
			if ("id".equals(field.getName()) || Modifier.toString(field.getModifiers()).indexOf("static") != -1) {
				continue;
			}
			field.setAccessible(true);
			Object obj = field.get(source);
			Object targetObj = field.get(target);
			if (null != obj && null == targetObj) {
				field.set(target, obj);
			}
		}
	}

	public void update(Object entity) {
		update(entity, false);
	}

	public void update(Object entity, boolean isValidate) {
		extentionPointService.beforeSave(entity);

		this.save(entity, isValidate);

		extentionPointService.afterSave(entity);
	}

	public <T> T get(Class<T> clazz, Serializable id) {
		T t = (T) this.hibernateBaseDAO.get(clazz, id);
		extentionPointService.afterFetch(t);
		return t;
	}

	public <T> void delete(Class<T> clazz, Serializable id) {
		Object entity = this.hibernateBaseDAO.get(clazz, id);
		extentionPointService.beforeDelete(entity);
		this.hibernateBaseDAO.delete(entity);
		extentionPointService.afterDelete(entity);
		for (DomainChangedListener domainChangedListener : domainChangedService.getDomainChangedListenerList()) {
			domainChangedListener.applyChange(DomainChangedListener.DOMAIN_DELETED, entity);
		}
	}

	public <T> void deleteAll(Class<T> clazz, String json) {
		String[] ids = json.split(",");
		for (Object id : ids) {
			this.delete(clazz, Long.parseLong(String.valueOf(id)));
		}
	}

	public <T> List<T> find(Class<T> clazz) {
		return this.hibernateBaseDAO.findByGeneric(clazz, null);
	}

	public List<Object> find(Class<?> clazz, List<Criterion> criterions) {
		return this.hibernateBaseDAO.find(clazz, criterions);
	}

	public <T> List<T> find(Class<T> clazz, List<Criterion> criterions, List<Order> orders) {
		return this.hibernateBaseDAO.find(clazz, criterions, orders);
	}

	public <T> Page findPage(Class<T> clazz, Page page, Criteria criteria) {
		return this.hibernateBaseDAO.findPage(clazz, page, criteria);
	}

	public <T> Page findPage(Class<T> clazz, Page page, Criteria criteria, List<TableColumnDesc> columns) {
		return this.hibernateBaseDAO.findPage(clazz, page, criteria, columns);
	}

	public Long count(String hql) {
		return this.hibernateBaseDAO.count(hql);
	}

	public Long count(String hql, List<Object> param) {
		return this.hibernateBaseDAO.count(hql, param);
	}

	public Page getPage(String domainName, int pageNumber, int pageSize, String sort, String json, Object loginUser) {
		Class<?> clazz = this.getDomainClass(domainName);

		Map<String, Object> queryMap = GSON_FULL.fromJson(json, LinkedHashMap.class);

		// Object entity = this.getDomainFromPostJosn(json, domainName);
		Page page = new Page();
		page.setExample(queryMap);
		page.setPageNumber(pageNumber);
		page.setPageSize(pageSize);

		if ("activeness".equals(domainName)) {
			LinkedHashMap<?, ?> example = (LinkedHashMap<?, ?>) page.getExample();
			if (null == example.get("orderTime")) {
				return page;
			}

			Map<?, ?> m = (Map<?, ?>) example.get("orderTime");

			Criteria c = this.hibernateBaseDAO.getCriteria(clazz);
			c.createCriteria("cust", "cust");

			ProjectionList pList = Projections.projectionList();
			pList.add(Projections.groupProperty("orderCityName").as("orderCityName"));
			pList.add(Projections.groupProperty("orderCountyName").as("orderCountyName"));
			pList.add(Projections.groupProperty("cust.custName").as("custName"));
			pList.add(Projections.alias(Projections.rowCount(), "puchaseTimes"));
			pList.add(Projections.alias(Projections.sum("quantity"), "puchaseNumber"));

			c.setProjection(pList);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				if (m.containsKey("lval")) {
					c.add(Restrictions.ge("orderTime", sdf.parse((String) (m.get("lval")))));
				}
				if (m.containsKey("rval")) {
					c.add(Restrictions.le("orderTime", sdf.parse((String) (m.get("rval")))));
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (example.containsKey("orderCityName")
					&& !"".equals(((Map<?, ?>) example.get("orderCityName")).get("val"))) {
				c.add(Restrictions.like("orderCityName", ((Map<?, ?>) example.get("orderCityName")).get("val")));
			}
			if (example.containsKey("orderCountyName")
					&& !"".equals(((Map<?, ?>) example.get("orderCountyName")).get("val"))) {
				c.add(Restrictions.like("orderCountyName", ((Map<?, ?>) example.get("orderCountyName")).get("val")));
			}

			page.setTotal(c.list().size());

			c = this.hibernateBaseDAO.getCriteria(clazz);
			c.createCriteria("cust", "cust");

			c.setProjection(pList);

			try {
				if (m.containsKey("lval")) {
					c.add(Restrictions.ge("orderTime", sdf.parse((String) (m.get("lval")))));
				}
				if (m.containsKey("rval")) {
					c.add(Restrictions.le("orderTime", sdf.parse((String) (m.get("rval")))));
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (example.containsKey("orderCityName")
					&& !"".equals(((Map<?, ?>) example.get("orderCityName")).get("val"))) {
				c.add(Restrictions.like("orderCityName", ((Map<?, ?>) example.get("orderCityName")).get("val")));
			}
			if (example.containsKey("orderCountyName")
					&& !"".equals(((Map<?, ?>) example.get("orderCountyName")).get("val"))) {
				c.add(Restrictions.like("orderCountyName", ((Map<?, ?>) example.get("orderCountyName")).get("val")));
			}

			if (page.getPageSize() > 0) {
				c.setFirstResult((page.getPageNumber() - 1) * page.getPageSize());
				c.setMaxResults(page.getPageSize());
			}
			c.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

			// String sql = "select orderCityName,orderCountyName,cust.custName,
			// count(1) as puchaseTimes, sum(quantity) from Activeness";
			//
			// sql = sql + " where orderTime between " + "'2016-07-01 00:00:00'"
			// + " and " + "'2016-07-02 23:59:59'";
			// sql = sql + " group by
			// orderCityName,orderCountyName,cust.custName";
			//
			// List<Object[]> list = (List<Object[]>)
			// this.hibernateBaseDAO.findByHql(clazz, sql);
			// List<Object> finalList = new ArrayList<Object>();
			// for (Object[] objects : list) {
			// Map<String, Object> map = new LinkedHashMap<String, Object>();
			// map.put("custName", objects[0]);
			// map.put("orderCityName", objects[1]);
			// map.put("orderCountyName", objects[2]);
			// map.put("puchaseTimes", objects[3]);
			// map.put("puchaseNumber", objects[4]);
			// finalList.add(map);
			// }

			page.setList(c.list());

			return page;
		}

		DomainDesc dd = this.annotionService.getDomainDesc(domainName);

		if (StringUtils.isNotEmpty(sort)) {
			page.setSort(sort);
		} else if (StringUtils.isNotEmpty(dd.getDefaultSort())) {
			page.setSort(dd.getDefaultSort());
		} else {
			// page.setSort("");
		}

		List<TableColumnDesc> columns = this.getDomainTableDesc(domainName);
		List<Criterion> criterions = new ArrayList<Criterion>();
		Criteria criteria = this.criteriaService.getCriteria(clazz, domainName);
		try {
			// queryMap.putAll(this.domainHandlerService
			// .getDefaultFilter(domainName));
			List<Map<String, Object>> querys = new ArrayList<Map<String, Object>>();
			querys.add(queryMap);
			querys.addAll(this.domainHandlerService.getDefaultFilter(domainName));
			for (Map<String, Object> map : querys) {
				Map<String, Object> query = criteriaService.createQuery(clazz, map, criteria);
				criterions.addAll(criteriaService.getSearchCriterions(domainName, query));
			}
			if (StringUtils.isNotEmpty(page.getSort())) {
				String[] sorts = page.getSort().split(",");
				Map<String, Object> query = new HashMap<String, Object>();
				for (String s : sorts) {
					if (s.indexOf("_") == -1) {
						if (s.startsWith("-")) {
							query.put(s.substring(1), "");
						} else {
							query.put(s, "");
						}
					}
				}
				criteriaService.createQuery(clazz, query, criteria);
			}
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error(e.getMessage(), e);
			page.setTotal(0);
			page.setList(Collections.EMPTY_LIST);
			return page;
		}

		if (null != dd && loginUser != null && !StringUtils.equals("admin", httpSessionService.getLoginUserAccount())) {
			DataFilterDesc dfd = dd.getDataFilter();
			if (null != dfd) {
				String by = dfd.getBy();
				String through = dfd.getThrough();
				Class<?> throughClass = null;
				try {
					throughClass = Class.forName(through);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (!"".equals(through) && null != throughClass) {
					List<Criterion> cs = new ArrayList<Criterion>();
					cs.add(Restrictions.eq(dfd.getUserProperty(), loginUser));
					List<Object> valueList = this.hibernateBaseDAO.find(throughClass, cs);
					if (valueList.isEmpty()) {
						page.setTotal(0);
						page.setList(Collections.emptyList());
						return page;
					} else {
						Set<Object> valueSet = new HashSet<Object>();
						for (Object obj : valueList) {
							try {
								Object o = this.getValue(obj, dfd.getValueProperty());
								if (o == null) {
									continue;
								}
								if (o instanceof Collection) {
									valueSet.addAll((Collection) o);
								} else {
									valueSet.add(o);
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						Map<String, Object> cond = new HashMap<String, Object>();
						cond.put(by, valueSet);
						Map<String, Object> query = criteriaService.createQuery(clazz, cond, criteria);
						criterions.addAll(criteriaService.getSearchCriterions(domainName, query));
					}
				} else {
					criterions.add(Restrictions.eq(by, loginUser));
				}
			}
		}

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (null == columns) {
			this.findPage(clazz, page, criteria);
		} else {
			boolean hasHandler = false;
			for (TableColumnDesc column : columns) {
				if (column.hasHandler()) {
					hasHandler = true;
					break;
				}
			}
			this.findPage(clazz, page, criteria, columns);

			if (hasHandler) {
				this.tableColumnService.process(columns, page);
			}

			if (this.annotionService.hasCustomOperation(domainName)) {
				try {
					this.operationService.processDisable(dd, page);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return page;
	}

	public long getIdWithCriterions(String domainName, Map<String, Object> filters, Set<String> aotherFilters) {
		Class<?> domainClazz = this.getDomainClass(domainName);
		if (aotherFilters.isEmpty()) {
			List<?> dataList = criteriaService.list(domainClazz, filters, "id");
			if (!dataList.isEmpty()) {
				return Long.parseLong(dataList.get(0).toString());
			}
			return -1;
		}

		List<String> pList = new ArrayList<String>();
		pList.add("id");

		List<String> filterStrList = new ArrayList<String>();

		// TODO more filter type support, only support Number and String
		for (String fk : filters.keySet()) {
			Object fv = filters.get(fk);
			if (fv instanceof Number) {
				filterStrList.add(fk + "=" + fv);
			} else if (fv instanceof String) {
				filterStrList.add(fk + "='" + fv + "'");
			}
		}

		if (null != aotherFilters) {
			filterStrList.addAll(aotherFilters);
		}
		if (filterStrList.isEmpty()) {
			return -1;
		}
		List<?> dataList = this.hibernateBaseDAO.findByProperties(domainClazz, pList,
				StringUtils.join(filterStrList, " and "));

		if (!dataList.isEmpty()) {
			return Long.parseLong(dataList.get(0).toString());
		}
		return -1;
	}

	public long getIdWithCriterions(String domainName, Map<String, Object> filters) {
		return this.getIdWithCriterions(domainName, filters, null);
	}

	@SuppressWarnings("unused")
	private List<Criterion> getExampleCriterions(String domainName, Object example)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		List<Criterion> criterions = new ArrayList<Criterion>();

		for (Field field : ReflectUtils.getDeclaredFields(example.getClass())) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);

			Object val = field.get(example);
			if (null != val) {
				String refName = field.getName();
				Reference ref = this.annotionService.getReferences(domainName, refName);
				if (null != ref) {
					Field refIdField = ReflectUtils.getField(val.getClass(), ref.id());
					refIdField.setAccessible(true);
					criterions.add(Restrictions.eq(refName + "." + refIdField.getName(), refIdField.get(val)));
				} else if (val instanceof String) {
					criterions.add(Restrictions.like(refName, "%" + String.valueOf(val) + "%"));
				} else {
					criterions.add(Restrictions.eq(refName, val));
				}
			}
		}
		return criterions;
	}

	public void applyDictChange(String dictCode) {
		this.dataInitializeService.applyDictChange(dictCode);
	}

	public Object processFieldValueChanged(String domainName, String fieldName, Object domain) {
		return this.fieldHandlerService.onChanged(domainName, fieldName, domain);
	}

	public SpecialData processSpecial(String domainName, String query) {
		return this.domainHandlerService.processSpecial(domainName, GSON_FULL.fromJson(query, Map.class));
	}

	public DomainMessage operateMessage(String domainName, String json) {
		return (DomainMessage) this.domainHandlerService.getMessage(domainName, json);
	}

	public Reference getReferences(String domainName, String refName) {
		// TODO Auto-generated method stub
		return this.annotionService.getReferences(domainName, refName);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T> T doNewTransaction(Callback<T> call) {
		if (call == null) {
			return null;
		}
		return call.doAction();
	}

	public static interface Callback<T> {
		T doAction();
	}

}
