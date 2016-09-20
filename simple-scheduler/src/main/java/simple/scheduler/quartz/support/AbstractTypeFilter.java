package simple.scheduler.quartz.support;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

public class AbstractTypeFilter extends AbstractClassTestingTypeFilter{

	private Class<?> clazz;
	
	public AbstractTypeFilter(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	protected boolean match(ClassMetadata metadata) {
		String superClazz =metadata.getSuperClassName();
		String className=metadata.getClassName();
		try {
			Class cls=Class.forName(className);
			while(!cls.equals(Object.class)){
				if(cls.equals(clazz)){
					return true;
				}
				cls=cls.getSuperclass();
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		if(StringUtils.isNotBlank(superClazz)
				 && superClazz.toLowerCase().equals(clazz.getName().toLowerCase())){
			return true;
		}
		return false;
	}


}
