package simple.base.service;

import java.util.List;

import jxl.common.Logger;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import simple.base.model.BaseDictItem;
import simple.core.service.BaseService;

@Service
public class DictItemService extends BaseService{

	private Logger logger =Logger.getLogger(DictItemService.class);
	
	public BaseDictItem getBaseDictItemByDictCodeAndDictItemCode(String dictCode,String dictItemCode){
		Criteria criteria = super.hibernateBaseDAO.getCriteria(BaseDictItem.class);
		criteria.createAlias("dict","dictAlias");
		criteria.add(Restrictions.eq("code", dictItemCode));
		criteria.add(Restrictions.eq("dictAlias.code", dictCode));
		List<BaseDictItem> baseDictItemList = criteria.list();
		if(!baseDictItemList.isEmpty()){
			return baseDictItemList.iterator().next();
		}
		 return null;
	}
	
	public List<BaseDictItem> getBaseDictItemListByDictCode(String dictCode){
		Criteria criteria = super.hibernateBaseDAO.getCriteria(BaseDictItem.class);
		criteria.createAlias("dict","dictAlias");
		criteria.add(Restrictions.eq("dictAlias.code", dictCode));
		List<BaseDictItem> baseDictItemList = criteria.list();
		if(!baseDictItemList.isEmpty()){
			return baseDictItemList;
		}
		 return null;
	}
	
	public BaseDictItem getBaseDictItemByDictCodeAndDictItemName(String dictCode,String dictItemName){
		Criteria criteria = super.hibernateBaseDAO.getCriteria(BaseDictItem.class);
		criteria.createAlias("dict","dictAlias");
		criteria.add(Restrictions.eq("name", dictItemName));
		criteria.add(Restrictions.eq("dictAlias.code", dictCode));
		List<BaseDictItem> baseDictItemList = criteria.list();
		if(!baseDictItemList.isEmpty()){
			return baseDictItemList.iterator().next();
		}
		 return null;
	}
}