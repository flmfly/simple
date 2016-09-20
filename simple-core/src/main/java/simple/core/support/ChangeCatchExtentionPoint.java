package simple.core.support;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.config.annotation.support.ExtentionPoint;
import simple.core.service.BaseService;
import simple.core.util.ReflectUtils;

@Component
public class ChangeCatchExtentionPoint implements ExtentionPoint {
	@Autowired
	protected BaseService baseService;

	private ThreadLocal<Object> origin = new ThreadLocal<Object>();

	@Override
	public void beforeSave(Object entity) {
		try {
			Field idField = ReflectUtils.getField(entity.getClass(), "id");
			if (null != idField) {
				idField.setAccessible(true);
				Serializable idObj = (Serializable) idField.get(entity);
				if (null != idObj) {
					Object obj = this.baseService.get(entity.getClass(), idObj);
					this.baseService.evit(obj);
					origin.set(obj);
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void afterSave(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeFetch(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterFetch(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterDelete(Object entity) {
		// TODO Auto-generated method stub

	}

	protected boolean isUpdate() {
		return null != this.origin.get();
	}

	protected boolean isAdd() {
		return null == this.origin.get();
	}

	protected Object getOrigin() {
		return this.origin.get();
	}
}
