package simple.base.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;

import simple.base.model.BaseMenu;
import simple.base.vo.Menu;
import simple.core.service.BaseService;

@Service
public class MenuService extends BaseService {

	public List<Menu> getMenu() {
		List<Menu> menus = new ArrayList<Menu>();
		try {
			List<Order> orders = new ArrayList<Order>();
			orders.add(Order.asc("id"));
			List<BaseMenu> menuList = super.find(BaseMenu.class, null, orders);

			Map<Long, Menu> refMap = new LinkedHashMap<Long, Menu>();

			for (BaseMenu menu : menuList) {
				Menu m = new Menu();
				m.setHidden(menu.getHidden());
				m.setCss(menu.getIconCss());
				m.setName(menu.getName());
				m.setCode(menu.getCode());
				if (null != menu.getResource())
					m.setUri(menu.getResource().getUri());
				if (null != menu.getSort()) {
					m.setSort(menu.getSort());
				}
				if (null != menu.getParent())
					m.setPid(menu.getParent().getId());
				refMap.put(menu.getId(), m);
			}

			for (Menu m : refMap.values()) {
				if (null == m.getPid()) {
					menus.add(m);
				} else {
					refMap.get(m.getPid()).setHasChildren(true);
					refMap.get(m.getPid()).addChild(m);
					m.setParentCode(refMap.get(m.getPid()).getCode());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(menus);

		return this.filterHiddenMenu(menus);
	}

	private List<Menu> filterHiddenMenu(final List<Menu> menus) {
		List<Menu> filtered = new ArrayList<Menu>();
		for (Menu menu : menus) {
			if (null == menu.getHidden() || !menu.getHidden()) {
				filtered.add(menu);
			}
		}
		return filtered;
	}
}
