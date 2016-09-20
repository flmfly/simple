package simple.scheduler.quartz.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.base.model.BaseDict;
import simple.base.model.BaseDictItem;
import simple.base.model.BaseMenu;
import simple.base.model.BaseResource;
import simple.core.service.BaseService;

@Service
public class JobDBInitService implements InitializingBean{

	

	@Autowired
	private BaseService baseService;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.updateDB();
	}
	
	private void updateDB() {
	    //初始化jobType类型
	 this.saveBaseDictItem("jobType", "任务类型", "job任务的类型", "method", "方法", "用类的方法来处理job");
	 //
	 this.saveBaseDictItem("emailConfige", "发送邮箱配置", "配置发送邮箱的服务器", "emailHostName", "邮箱smtp地址", "smtp.126.com");
	 
	 this.saveBaseDictItem("emailConfige", "发送邮箱配置", "配置发送邮箱的服务器", "emailSmptPort", "邮箱smtp端口", "25");
	 
	 this.saveBaseDictItem("emailConfige", "发送邮箱配置", "配置发送邮箱的服务器", "emailUserName", "邮箱用户名", "cnpl2015@126.com");
	 
	 this.saveBaseDictItem("emailConfige", "发送邮箱配置", "配置发送邮箱的服务器", "emailPassword", "邮箱密码", "cnpl20150101");
	 
	 this.saveBaseDictItem("emailConfige", "发送邮箱配置", "配置发送邮箱的服务器", "emailFrom", "发送者邮箱", "cnpl2015@126.com");
	  
	 BaseMenu bm = new BaseMenu();
		bm.setCode("job");
		bm.setName("任务管理");
		bm.setIconCss("fa fa-tasks");
		bm.setSort(-9999);
		bm = this.insertIfNotExist(bm, "code", bm.getCode());
		this.saveMenu("basejob", "任务维护", "/list/basejob", "basejob",
				"fa fa-clipboard", 10, bm);
		this.saveMenu("basejoblog", "任务日志维护", "/list/basejoblog", "basejoblog",
				"fa fa-clipboard", 20, bm);
		this.saveMenu("jobhandler", "任务帮助类维护", "/list/jobhandler", "jobhandler",
				"fa fa-clipboard", 30, bm);
	 
	}
	
	private void saveMenu(String code, String name, String uri, String mcode,
			String css, int sort, BaseMenu parent) {
		BaseResource br = new BaseResource();
		br.setCode(code);
		br.setName(name);
		br.setUri(uri);
		br = this.insertIfNotExist(br, "code", br.getCode());

		BaseMenu bm = new BaseMenu();
		bm.setCode(mcode);
		bm.setName(name);
		bm.setIconCss(css);
		bm.setParent(parent);
		bm.setResource(br);
		bm.setSort(sort);
		bm = this.insertIfNotExist(bm, "code", bm.getCode());
	}
	
	private void saveBaseDictItem(String dictCode,String dictName,String dictRemark,
			String dictItemCode,String dictItemName,String dictItemRemark){
		BaseDict  baseDict = new BaseDict();
		baseDict.setCode(dictCode);
		baseDict.setName(dictName);
		baseDict.setRemark(dictRemark);
		baseDict =this.insertIfNotExist(baseDict, "code", dictCode);
		
		BaseDictItem baseDictItem = new BaseDictItem();
		baseDictItem.setCode(dictItemCode);
		baseDictItem.setName(dictItemName);
		baseDictItem.setRemark(dictItemRemark);
		baseDictItem.setDict(baseDict);
		baseDictItem =this.insertIfNotExist(baseDictItem, "code", dictItemCode);
		
		
		
	}
	
	@SuppressWarnings("unchecked")
	private <T> T insertIfNotExist(T t, String param, String val) {
		List<Criterion> criterions = new ArrayList<Criterion>();
		criterions.add(Restrictions.eq(param, val));
		List<T> result = (List<T>) baseService.find(t.getClass(), criterions);
		if (result.isEmpty()) {
			this.baseService.save(t);
		} else {
			return result.get(0);
		}

		return t;

	}

}
