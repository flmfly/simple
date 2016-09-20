package simple.core.annotation;

import simple.config.annotation.DataFilter;
import simple.core.model.annotation.DataFilterDesc;

public class DataFilterAnnoatationHandler implements
		AnnotationHandler<DataFilter, DataFilterDesc> {

	@Override
	public DataFilterDesc handle(DataFilter a) {
		DataFilterDesc df = new DataFilterDesc();
		df.setBy(a.by());
		df.setThrough(a.through());
		df.setUserProperty(a.userProperty());
		df.setValueProperty(a.valueProperty());
		return df;
	}

}
