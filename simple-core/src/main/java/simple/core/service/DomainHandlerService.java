package simple.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import simple.config.annotation.SMSSupport;
import simple.config.annotation.Special;
import simple.config.annotation.support.DomainHandler;
import simple.core.model.SpecialData;

@Service
public final class DomainHandlerService extends HandlerService {

	List<Map<String, Object>> getDefaultFilter(String domainName) {
		String handlerName = super.annotionService
				.getDomainFilterHandler(domainName);
		if (null != handlerName) {
			try {

				DomainHandler dvHanlder = (DomainHandler) super
						.fetchHandler(Class.forName(handlerName));
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Map<String, Object> filter = dvHanlder.getFilter();
				if (!filter.isEmpty()) {
					list.add(filter);
				}
				list.addAll(dvHanlder.getFilters());
				return list;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Collections.emptyList();
	}

	SpecialData processSpecial(String domainName, Object domain) {
		Special special = super.annotionService.getSpecial(domainName);
		String handlerName = special.handler();
		SpecialData rtn = new SpecialData();
		rtn.setType(special.type().name());
		if (null != handlerName) {
			try {
				DomainHandler dvHanlder = (DomainHandler) super
						.fetchHandler(Class.forName(handlerName));
				rtn.setData(dvHanlder.processSpecial(domain));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rtn;
	}

	Object getMessage(String domainName, String json) {
		SMSSupport smsSupport = super.annotionService.getSMSSupport(domainName);
		String handlerName = smsSupport.messageHandler();

		if (null != handlerName) {
			try {
				DomainHandler dvHanlder = (DomainHandler) super
						.fetchHandler(Class.forName(handlerName));
				return dvHanlder.getMessage(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
