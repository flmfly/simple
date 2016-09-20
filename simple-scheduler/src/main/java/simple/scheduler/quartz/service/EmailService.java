package simple.scheduler.quartz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.MultiPartEmail;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simple.base.model.BaseDict;
import simple.base.model.BaseDictItem;
import simple.core.service.BaseService;
import simple.jobs.utils.Constants;
import simple.jobs.utils.EmailEnum;

@Service
public class EmailService{

	@Autowired
	private BaseService baseService;
	
	@SuppressWarnings("unchecked")
	public  List<BaseDictItem> findUniqueBaseDictItemByCode(String dictCode)throws Exception{
		
		if(StringUtils.isBlank(dictCode)){
			return Collections.EMPTY_LIST;
		}
		List<Criterion> criterions = new ArrayList<Criterion>();
		criterions.add(Restrictions.eq("code", dictCode));
		List<BaseDict> baseDictList = baseService.find(BaseDict.class, criterions,null);
		if(!baseDictList.isEmpty()){
			List<Criterion> criterionlist = new ArrayList<Criterion>();
			criterionlist.add(Restrictions.eq("dict", baseDictList.iterator().next()));
			List<BaseDictItem> baseDictItemList =baseService.find(BaseDictItem.class, criterionlist,null);
			if(!baseDictItemList.isEmpty()){
				return baseDictItemList;
			}
		}
		return Collections.EMPTY_LIST;
	}
	@SuppressWarnings("deprecation")
	public  Email getMultiPartEmail() throws Exception{
		MultiPartEmail email = new MultiPartEmail();
		List<BaseDictItem> baseDictItemList = this.findUniqueBaseDictItemByCode(Constants.EMAIL_CONFIG);
		String emailUserName ="";
		String emailPassword ="";
		if(!baseDictItemList.isEmpty()){
			Iterator<BaseDictItem> baseDictItemIt = baseDictItemList.iterator();
			while(baseDictItemIt.hasNext()){
				BaseDictItem baseDictItem = baseDictItemIt.next();
				if(EmailEnum.EMAILHOSTNAME.name().equals(baseDictItem.getCode().toUpperCase())){
					email.setHostName(baseDictItem.getRemark());
					continue;
				}
				if(EmailEnum.EMAILSMPTPORT.name().equals(baseDictItem.getCode().toUpperCase())){
					email.setSmtpPort(Integer.valueOf(baseDictItem.getRemark()));
					continue;
			    }
				if(StringUtils.isNotBlank(baseDictItem.getCode()) && 
						(EmailEnum.EMAILFROM.name().equals(baseDictItem.getCode().toUpperCase()))){
					email.setFrom(baseDictItem.getRemark());
					continue;
			    }
				if(EmailEnum.EMAILUSERNAME.name().equals(baseDictItem.getCode().toUpperCase())){
					emailUserName =baseDictItem.getRemark();
					continue;
			    }
				if(StringUtils.isNotBlank(baseDictItem.getCode()) && 
						(EmailEnum.EMAILPASSWORD.name().equals(baseDictItem.getCode().toUpperCase()))){
					emailPassword = baseDictItem.getRemark();
					continue;
			    }
		}
		if (StringUtils.isNotBlank(emailUserName)) {
			email.setAuthenticator(new DefaultAuthenticator(emailUserName, emailPassword));
		}
		email.setTLS(true);
		
		return email;
	}
		 return null;
	}
	
}
