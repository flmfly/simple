package simple.core.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import simple.config.annotation.DefaultValue;
import simple.config.annotation.support.DefaultValueHandler;
import simple.core.model.FormField;

@Service
public class DefaultValueService extends HandlerService {

	public List<FormField> setDefaultValue(String domainName,
			List<FormField> fields, HttpServletRequest request) {
		try {
			Map<String, DefaultValue> rfMap = annotionService
					.getDefaultValues(domainName);
			if (null != rfMap) {
				for (FormField field : fields) {
					DefaultValue dv = rfMap.get(field.getName());
					if (null != dv) {
						DefaultValueHandler dvHanlder = (DefaultValueHandler) super
								.fetchHandler(dv.handler());
						field.setDefaultVal(dvHanlder.handle(null, request));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fields;
	}
}
