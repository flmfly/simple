package simple.core.annotation;

import simple.config.annotation.StandardOperation;
import simple.core.model.annotation.StandarOperationDesc;

public class StandarOperationAnnoatationHandler implements
		AnnotationHandler<StandardOperation, StandarOperationDesc> {

	@Override
	public StandarOperationDesc handle(StandardOperation a) {
		StandarOperationDesc sod = new StandarOperationDesc();
		sod.setAdd(a.add());
		sod.setDelete(a.delete());
		sod.setExport(a.export());
		sod.setImp(a.imp());
		sod.setModify(a.modify());
		sod.setQuery(a.query());
		sod.setCheck(a.check());
		return sod;
	}

}
