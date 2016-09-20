package simple.base.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.base.model.BaseDictItem;
import simple.config.annotation.support.ExtentionPoint;
import simple.core.service.BaseService;

@Component
public class BaseDictItemExtentionPoint implements ExtentionPoint {

	@Autowired
	private BaseService baseService;

	@Override
	public void beforeSave(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterSave(Object entity) {
		BaseDictItem di = (BaseDictItem) entity;
		this.baseService.applyDictChange(di.getDict().getCode());
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
		BaseDictItem di = (BaseDictItem) entity;
		this.baseService.applyDictChange(di.getDict().getCode());
	}

}
