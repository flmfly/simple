package simple.scheduler.quartz.support;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

public class InterfaceTypeFilter extends AbstractClassTestingTypeFilter{

	private Class<?> clazz;
	
	public InterfaceTypeFilter(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	protected boolean match(ClassMetadata metadata) {
		String[] interClazz =metadata.getInterfaceNames();
		if(null != interClazz && interClazz.length!=0){
			for(String inter : interClazz){
				if(StringUtils.isNotBlank(inter)
						 && inter.toLowerCase().equals(clazz.getName().toLowerCase())){
					return true;
				}
			}
		}
		
		return false;
	}


}
