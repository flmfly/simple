package simple.core.service;

import java.lang.reflect.Field;
import java.rmi.NoSuchObjectException;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.config.annotation.UpdateImport;
import simple.config.annotation.UpdateImport.MulOperation;
import simple.core.dao.HibernateBaseDAO;
import simple.core.util.ReflectUtils;

@Service
public class ImportService extends BaseService {

	@Autowired
	protected HibernateBaseDAO hibernateBaseDAO;

	public List<Object> findDictItemProperties(Class clazz,
			List<String> propertyList, String filter) {
		return (List<Object>) hibernateBaseDAO.findByProperties(clazz,
				propertyList, filter);
	}

	public Object getByBusinessKey(Object entity, List<String> bkList)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchObjectException {
		if (entity == null) {
			return null;
		}
		Class clazz = entity.getClass();
		UpdateImport updateImport = (UpdateImport) clazz
				.getAnnotation(UpdateImport.class);

		MulOperation operation = MulOperation.err;
		if (updateImport != null) {
			operation = updateImport.operation();
		}
		Field idField = ReflectUtils.getField(clazz, "id");
		idField.setAccessible(true);
		Criteria criteria = hibernateBaseDAO.getCriteria(clazz);
		if (operation == MulOperation.err) {
			criteria.setFirstResult(0);
			criteria.setMaxResults(2);
		} else {
			criteria.addOrder(Order.asc("id"));
		}
		this.getCriterionsFromExample("", criteria, clazz,
				this.getBusinessKeyObject(entity, bkList), null, false);
		if (!((CriteriaImpl) criteria).iterateExpressionEntries().hasNext()) {
			return null;
		}
		List<?> objectList = criteria.list();
		if (objectList.size() > 1) {
			if (operation == MulOperation.err) {
				throw new NoSuchObjectException("按照业务主键查询出多条数据！");
			} else if (operation == MulOperation.first) {
				return objectList.get(0);
			} else if (operation == MulOperation.last) {
				return objectList.get(objectList.size() - 1);
			} else {
				return null;
			}
		} else if (objectList.size() == 1) {
			return objectList.get(0);
		} else {
			return null;
		}
	}

	private Object getBusinessKeyObject(Object oragin, List<String> bkList) {
		Object rtn = null;
		try {
			rtn = oragin.getClass().newInstance();
			for (String bk : bkList) {
				StringTokenizer tokenizer = new StringTokenizer(bk, ".");
				String fieldName = tokenizer.nextToken();
				Field field = ReflectUtils.getField(oragin.getClass(),
						fieldName);
				field.setAccessible(true);
				if (tokenizer.hasMoreTokens()) {
					Object target = field.getType().newInstance();
					field.set(rtn, target);
					Object srcTarget = field.get(oragin);
					while (tokenizer.hasMoreTokens()) {
						String fName = tokenizer.nextToken();
						Field f = ReflectUtils.getField(target.getClass(),
								fName);
						f.setAccessible(true);
						srcTarget = f.get(srcTarget);
						if (tokenizer.hasMoreTokens()) {
							Object tmp = f.getType().newInstance();
							f.set(target, tmp);
							target = tmp;
						} else {
							f.set(target, srcTarget);
						}
					}
				} else {
					field.set(rtn, field.get(oragin));
				}
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rtn;
	}

	private void getCriterionsFromExample(String filedPath, Criteria criteria,
			Class<?> clazz, Object entity, String sort, boolean desc)
			throws IllegalArgumentException, IllegalAccessException {
		for (Field field : ReflectUtils.getDeclaredFields(clazz)) {
			String currentFieldPath = field.getName();
			if (!"".equals(filedPath)) {
				currentFieldPath = filedPath + "." + currentFieldPath;
			}

			if (currentFieldPath.equals(sort)) {
				if (desc) {
					criteria.addOrder(Order.desc(field.getName()));
				} else {
					criteria.addOrder(Order.asc(field.getName()));
				}
			}

			boolean drillDown = false;
			if (null != sort && sort.startsWith(currentFieldPath)) {
				drillDown = true;
			}

			field.setAccessible(true);
			Object val = null;
			if (null != entity) {
				val = field.get(entity);
			}
			// if (null != val) {
			// drillDown = true;
			// }

			JoinColumn jcol = field.getAnnotation(JoinColumn.class);

			if (null != val) {
				Column col = field.getAnnotation(Column.class);
				if (null != col) {
					criteria.add(Restrictions.eq(field.getName(), val));
				}
			}
			if (null != jcol && (drillDown || null != val)) {
				Criteria subCriteria = criteria.createCriteria(field.getName());
				this.getCriterionsFromExample(currentFieldPath, subCriteria,
						field.getType(), val, sort, desc);
			}
			if (null != val) {
				javax.persistence.OneToMany otmcol = field
						.getAnnotation(javax.persistence.OneToMany.class);
				if (otmcol != null) {
					Collection collection = (Collection) val;
					if (collection.isEmpty()) {
						continue;
					}
					Criteria subCriteria = criteria.createCriteria(field
							.getName());
					for (Object sub : collection) {
						this.getCriterionsFromExample(currentFieldPath,
								subCriteria, sub.getClass(), sub, sort, desc);
					}
				}
			}
		}
	}
}
