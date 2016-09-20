package simple.core.service;

import org.springframework.stereotype.Service;

import simple.config.annotation.support.FieldHandler;

@Service
public final class FieldHandlerService extends HandlerService {

	Object onChanged(String domainName, String fieldName, Object domain) {
		String handlerName = super.annotionService.getOnChangedListenerHandler(
				domainName, fieldName);
		if (null != handlerName) {
			try {
				FieldHandler hanlder = (FieldHandler) super.fetchHandler(Class
						.forName(handlerName));
				hanlder.onChanged(domain);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return domain;
	}
}
