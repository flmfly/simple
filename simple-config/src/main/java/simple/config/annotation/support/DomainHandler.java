package simple.config.annotation.support;

import java.util.List;
import java.util.Map;

public interface DomainHandler {

	public List<Map<String, Object>> getFilters();

	public Map<String, Object> getFilter();

	public Object processSpecial(Object domain);

	public Object getMessage(String json);

}
