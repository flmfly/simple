package simple.config.annotation.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomainHandlerAdapter implements DomainHandler {

	public List<Map<String, Object>> getFilters() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	public Map<String, Object> getFilter() {
		// TODO Auto-generated method stub
		return Collections.emptyMap();
	}

	@Override
	public Object processSpecial(Object domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getMessage(String json) {
		// TODO Auto-generated method stub
		return null;
	}

}
