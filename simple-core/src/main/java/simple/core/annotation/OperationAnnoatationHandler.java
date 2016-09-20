package simple.core.annotation;

import simple.config.annotation.Operation;
import simple.config.annotation.OperationParameter;
import simple.core.model.annotation.OperationDesc;

public class OperationAnnoatationHandler implements
		AnnotationHandler<Operation, OperationDesc> {

	@Override
	public OperationDesc handle(Operation a) {
		OperationDesc od = new OperationDesc();
		od.setCode(a.code());
		od.setName(a.name());
		od.setIconStyle(a.iconStyle());
		od.setMulti(a.multi());
		od.setHandler(a.handler().getName());
		od.setTarget(a.target().name());

		for (OperationParameter operationParameter : a.parameters()) {
			od.addParameterAnnotation(operationParameter);
		}
		return od;
	}

}
