package simple.core.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.Subcriteria;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import simple.config.annotation.Domain;
import simple.core.model.Page;
import simple.core.model.TableColumnDesc;
import simple.core.util.ReflectUtils;

@Repository
@SuppressWarnings("unchecked")
public class HibernateBaseDAO {

	@Autowired
	private SessionFactory sessionFactory;

	public void deleteWithHql(String hql) {
		this.getSession().createQuery(hql).executeUpdate();
	}

	public void save(Object entity) {
		this.getSession().saveOrUpdate(entity);
	}

	public void flush() {
		this.getSession().flush();
	}

	public void refresh(Object object) {
		this.getSession().refresh(object);
	}

	public Object merge(Object object) {
		return this.getSession().merge(object);
	}

	public void evict(Object obj) {
		this.getSession().evict(obj);
	}

	public void update(Object entity) {
		this.getSession().saveOrUpdate(entity);
	}

	public <T> T get(Class<T> clazz, Serializable id) {
		return (T) this.getSession().get(clazz, id);
	}

	public void delete(Object entity) {
		this.getSession().delete(entity);
	}

	public <T> void delete(Class<T> clazz, Serializable id) {
		Session session = this.getSession();
		session.delete(session.get(clazz, id));
	}

	public <T> List<T> findByHql(Class<T> clazz, String hql) {
		return this.getSession().createQuery(hql).list();
	}

	public List<Object> find(Class<?> clazz, List<Criterion> criterions) {
		return this.createCriteria(clazz, criterions, null).list();
	}

	public <T> List<T> findByGeneric(Class<T> clazz, List<Criterion> criterions) {
		return this.createCriteria(clazz, criterions, null).list();
	}

	public Criteria getCriteria(Class<?> clazz) {
		return this.getSession().createCriteria(clazz);
	}

	public Criteria getCriteria(Class<?> clazz, String alias) {
		return this.getSession().createCriteria(clazz, alias);
	}

	public <T> List<T> find(Class<T> clazz, List<Criterion> criterions,
			List<Order> orders) {
		return this.createCriteria(clazz, criterions, orders).list();
	}

	public Page findPage(Class<?> clazz, Page page, Criteria criteria) {
		return this.findPage(clazz, page, criteria, null);
	}

	// public <T> List<T> findByProperties(Class<T> clazz,
	// List<String> propertyList) {
	// Criteria criteria = this.getSession().createCriteria(clazz);
	//
	// ProjectionList pList = null;
	// if (null != propertyList && !propertyList.isEmpty()) {
	// pList = Projections.projectionList();
	// for (String pName : propertyList) {
	// pList.add(Projections.property(pName).as(pName));
	// }
	// }
	//
	// criteria.setProjection(pList);
	// criteria.setResultTransformer(Transformers.aliasToBean(clazz));
	// return criteria.list();
	// }

	public <T> List<T> findMultipe(Class<T> clazz, Collection<Serializable> ids) {
		Criterion[] criterions = new Criterion[ids.size()];

		Iterator<Serializable> iterator = ids.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			criterions[i] = Restrictions.eq("id", iterator.next());
			i++;
		}
		return this.getSession().createCriteria(clazz)
				.add(Restrictions.or(criterions)).list();
	}

	public <T> List<T> findByProperties(Class<T> clazz,
			List<String> propertyList) {
		return this.findByProperties(clazz, propertyList, null);
	}

	public <T> List<T> findByProperties(Class<T> clazz, String fields) {
		return this.findByProperties(clazz, fields, null, null, null);
	}

	public <T> List<T> findByProperties(Class<T> clazz, String fields,
			String filter, ResultTransformer transformer,
			List<Object> parameters) {
		return this.findByProperties(clazz, null, fields, filter, null, null);
	}

	public <T> List<T> findByProperties(Class<T> clazz, String join,
			String fields, String filter, ResultTransformer transformer,
			List<Object> parameters) {
		StringBuffer hql = new StringBuffer("select ");
		hql.append(fields);
		hql.append(" from ");
		hql.append(clazz.getName());
		if (StringUtils.isNotEmpty(join)) {
			hql.append(" as ").append(clazz.getSimpleName().toLowerCase());
			hql.append(join).append(" ");
		}
		if (null != filter && !"".equals(filter)) {
			hql.append(" where ");
			hql.append(filter);
		}
		Domain domain = clazz.getAnnotation(Domain.class);
		if (domain != null && StringUtils.isNotEmpty(domain.defaultSort())) {
			String[] sorts = domain.defaultSort().split(",");
			for (int i = 0; i < sorts.length; i++) {
				String s = sorts[i];
				if (s.startsWith("-")) {
					sorts[i] = s.substring(1) + " desc";
				}
			}
			hql.append(" order by  ");
			hql.append(StringUtils.join(sorts, ","));
		}
		Query query = this.getSession().createQuery(hql.toString());

		if (null != parameters && !parameters.isEmpty()) {
			for (int i = 0; i < parameters.size(); i++) {
				query.setParameter(i, parameters.get(i));
			}
		}

		if (null != transformer) {
			query.setResultTransformer(transformer);
		}
		return query.list();
	}

	public <T> List<T> findByProperties(Class<T> clazz, String fields,
			String filter) {
		return this.findByProperties(clazz, fields, filter, null, null);
	}

	public <T> List<T> findByProperties(Class<T> clazz, String fields,
			String filter, List<Object> parameters) {
		return this.findByProperties(clazz, fields, filter, null, parameters);
	}

	public <T> List<T> findByProperties(Class<T> clazz,
			List<String> propertyList, String filter) {
		return this.findByProperties(clazz,
				StringUtils.join(propertyList, ","), filter);
	}

	public List<Object> findList(Class<?> clazz, int pageNumber, int pageSize,
			String sort, Criterion... criterions) {
		Criteria criteria = this.getSession().createCriteria(clazz);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (null != sort && !"".equals(sort)) {
			if (sort.startsWith("-")) {
				criteria.addOrder(Order.desc(sort.substring(1, sort.length())));
			} else {
				criteria.addOrder(Order.asc(sort));
			}
		}

		if (pageSize > 0) {
			criteria.setFirstResult((pageNumber - 1) * pageSize);
			criteria.setMaxResults(pageSize);
		}

		return criteria.list();
	}

	public Page findPage(Class<?> clazz, Page page, Criteria criteria,
			List<TableColumnDesc> columns) {
		// String className = clazz.getSimpleName().toLowerCase();
		// Criteria criteria = this.getSession().createCriteria(clazz,
		// className);
		//
		// // if (null != page.getExample()) {
		// // criteria.add(Example.create(page.getExample()));
		// // }
		//
		// for (Criterion criterion : criterions) {
		// criteria.add(criterion);
		// }

		criteria.setProjection(Projections.rowCount());
		page.setTotal((Long) criteria.uniqueResult());

		ProjectionList pList = null;
		if (null != columns && !columns.isEmpty()) {
			pList = Projections.projectionList();
			List<TableColumnDesc> projectionColumns = new ArrayList<TableColumnDesc>();
			for (TableColumnDesc columnDesc : columns) {
				String fieldPath = columnDesc.getName();
				if (columnDesc.hasHandler()
						&& isTransientField(clazz, fieldPath.split("_"))) {
					continue;
				}

				projectionColumns.add(columnDesc);
			}
			this.fillProjectionListFromTableColumnDefine(pList,
					projectionColumns, criteria);
		}

		criteria.setProjection(pList);
		String sort = page.getSort();
		if (null != sort && !"".equals(sort)) {
			String[] sorts = sort.split(",");
			for (String s : sorts) {
				boolean desc = false;
				if (s.startsWith("-")) {
					s = s.substring(1);
					desc = true;

				}
				String[] array = s.split("\\.");
				String sortProperty = array[array.length - 1];
				if (array.length > 1) {
					sortProperty = StringUtils.join(
							ArrayUtils.subarray(array, 0, array.length - 1),
							"_") + "." + sortProperty;
				}
				if (desc) {
					criteria.addOrder(Order.desc(sortProperty));
				} else {
					criteria.addOrder(Order.asc(sortProperty));
				}
			}
		}

		if (page.getPageSize() > 0) {
			criteria.setFirstResult((page.getPageNumber() - 1)
					* page.getPageSize());
			criteria.setMaxResults(page.getPageSize());
		}
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		page.setList(criteria.list());

		return page;
	}

	private boolean isTransientField(Class<?> clazz, String[] fields) {
		// TODO Auto-generated method stub
		if (clazz == null || fields == null || fields.length == 0) {
			return true;
		}
		try {
			Class cls = clazz;
			for (String f : fields) {
				Field field = ReflectUtils.getField(cls, f);
				if (field.getAnnotation(Transient.class) != null) {
					return true;
				}
				cls = field.getType();
			}
			return false;
		} catch (Exception e) {
		}
		return true;
	}

	private void fillProjectionListFromTableColumnDefine(ProjectionList pList,
			List<TableColumnDesc> columns, Criteria criteria) {
		Set<String> criteriaPathSet = new HashSet<String>();

		Iterator<Subcriteria> itm = ((CriteriaImpl) criteria)
				.iterateSubcriteria();
		while (itm.hasNext()) {
			Subcriteria sub = itm.next();
			criteriaPathSet.add(sub.getAlias());
			// if(alias.equals(sub.getAlias()) || path.equals(sub.getPath())){
			// return true;
			// }
		}
		// return false;

		for (TableColumnDesc tableColumnDesc : columns) {
			String fieldPath = tableColumnDesc.getName();
			if (null != tableColumnDesc.getImageGallery()) {
				if (tableColumnDesc.getImageGallery().getIsArray()) {
					pList.add(Projections.id().as(fieldPath));
				} else {
					pList.add(Projections.property(
							criteria.getAlias() + "." + fieldPath)
							.as(fieldPath));
				}
			} else if (fieldPath.indexOf("_") != -1) {
				String criteriaPath = fieldPath.substring(0,
						fieldPath.lastIndexOf("_"));

				this.createCriteriasForPath(criteriaPath, criteriaPathSet,
						criteria);

				pList.add(Projections.property(
						criteriaPath
								+ "."
								+ fieldPath.substring(fieldPath
										.lastIndexOf("_") + 1)).as(fieldPath));
			} else {
				pList.add(Projections.property(
						criteria.getAlias() + "." + fieldPath).as(fieldPath));
			}
		}

	}

	private void createCriteriasForPath(String criteriaPath,
			Set<String> criteriaPathSet, Criteria criteria) {
		List<String> pathList = new ArrayList<String>();
		for (String path : criteriaPath.split("_")) {
			pathList.add(path);
			String currPath = StringUtils.join(pathList, "_");
			if (!criteriaPathSet.contains(currPath)) {
				criteria.createAlias(StringUtils.join(pathList, "."), currPath,
						JoinType.LEFT_OUTER_JOIN);
				criteriaPathSet.add(currPath);
			}
		}
	}

	public Long count(String hql) {
		return (Long) this.getSession().createQuery(hql).uniqueResult();
	}

	public Long count(String hql, List<Object> param) {
		Query q = this.getSession().createQuery(hql);
		if (param != null && param.size() > 0) {
			for (int i = 0; i < param.size(); i++) {
				q.setParameter(i, param.get(i));
			}
		}
		return (Long) q.uniqueResult();
	}

	private Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}

	private <T> Criteria createCriteria(Class<T> clazz,
			List<Criterion> criterions, List<Order> orders) {
		Criteria criteria = getSession().createCriteria(clazz);
		if (null != criterions) {
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
		}

		if (null != orders) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}
		return criteria;
	}

	public Object queryUniqueById(String hql, Long id) {
		Query query = this.getSession().createQuery(hql);
		query.setParameter("id", id);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.uniqueResult();
	}

	public void clear() {
		// TODO Auto-generated method stub
		this.getSession().clear();
	}
}
