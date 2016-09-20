package simple.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import simple.config.annotation.support.TableColumnHandler;
import simple.core.model.Page;
import simple.core.model.TableColumnDesc;

@Service
public class TableColumnService extends HandlerService {

	@SuppressWarnings("unchecked")
	public void process(List<TableColumnDesc> columns, Page page) {
		List<Object> list = page.getList();
		for (Object object : list) {
			Map<String, Object> rowMap = (HashMap<String, Object>) object;
			for (TableColumnDesc tableColumnDesc : columns) {
				String key = tableColumnDesc.getName();
				if (tableColumnDesc.hasHandler()) {
					try {
						TableColumnHandler handler = (TableColumnHandler) super
								.fetchHandler(Class.forName(tableColumnDesc
										.getHandler()));
						rowMap.put(key, handler.handle(rowMap));
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
