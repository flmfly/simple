package simple.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import simple.config.annotation.support.OperationHandler;
import simple.config.annotation.support.OperationHandler.OperationResult;
import simple.core.model.DomainDesc;
import simple.core.model.Page;
import simple.core.model.annotation.OperationDesc;

@Service
public class OperationService extends HandlerService {

	public OperationResult process(DomainDesc dd, String operationCode,
			Map<String, Object> parameters, List<Object> domainList)
			throws Exception {
		List<OperationDesc> operationList = dd.getOperation();
		String handlerClass = null;
		for (OperationDesc operationDesc : operationList) {
			if (operationCode.equals(operationDesc.getCode())) {
				handlerClass = operationDesc.getHandler();
				break;
			}
		}

		if (null != handlerClass) {
			OperationHandler handler = (OperationHandler) super
					.fetchHandler(Class.forName(handlerClass));
			return handler.handle(parameters, domainList);
		}

		return null;
	}

	public void processDisable(DomainDesc dd, Page page) throws Exception {
		List<Object> list = page.getList();
		Map<OperationHandler, OperationDesc> handlers = new HashMap<OperationHandler, OperationDesc>();
		for (OperationDesc od : dd.getOperation()) {
			handlers.put((OperationHandler) super.fetchHandler(Class.forName(od
					.getHandler())), od);
		}

		for (Object object : list) {
			@SuppressWarnings("unchecked")
			Map<String, Object> row = (Map<String, Object>) object;
			for (OperationHandler handler : handlers.keySet()) {
				boolean disabled = handler.disabled(object);
				OperationDesc od = handlers.get(handler);
				if (disabled) {
					row.put(od.getCode(), disabled);
				}
			}
		}
	}
}
