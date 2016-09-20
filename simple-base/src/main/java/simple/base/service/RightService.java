package simple.base.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import simple.base.model.BaseMenu;
import simple.base.model.BaseRole;
import simple.base.model.BaseUser;
import simple.base.vo.Menu;
import simple.core.model.FormField;
import simple.core.model.RepresentationType;
import simple.core.service.BaseService;

@Service
public class RightService extends BaseService {

	private static final String SUPER_USER_NAME = "admin";

	@SuppressWarnings("unchecked")
	public Collection<BaseMenu> getMenusByUser(Long userId) {

		List<BaseRole> rols = super.hibernateBaseDAO
				.getCriteria(BaseRole.class).createAlias("users", "u")
				.add(Restrictions.eq("u.id", userId)).list();
		Map<String, BaseMenu> menuMap = new HashMap<String, BaseMenu>();
		for (BaseRole baseRole : rols) {
			for (BaseMenu baseMenu : baseRole.getMenus()) {
				menuMap.put(baseMenu.getCode(), baseMenu);
			}
		}
		return menuMap.values();
	}

	public List<Menu> getMenuTree(Collection<BaseMenu> menuList) {
		List<Menu> menus = new ArrayList<Menu>();
		try {
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
					Menu parent=refMap.get(m.getPid());
					if(parent==null){
						continue;
					}
					parent.setHasChildren(true);
					parent.addChild(m);
					m.setParentCode(parent.getCode());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(menus);

		return this.filterHiddenMenu(menus);
	}

	public Collection<BaseMenu> getMenu(BaseUser user) {
		if (SUPER_USER_NAME.equals(user.getAccount())) {
			List<Order> orders = new ArrayList<Order>();
			orders.add(Order.asc("id"));
			return super.find(BaseMenu.class, null, orders);
		} else {
			return this.getMenusByUser(user.getId());
		}
	}

	public Set<String> getDomainRight(Collection<BaseMenu> menuList) {
		Set<String> domainList = new HashSet<String>();
		for (BaseMenu menu : menuList) {
			if (null != menu.getResource()
					&& null != menu.getResource().getUri()) {
				StringTokenizer st = new StringTokenizer(menu.getResource()
						.getUri(), "/");

				if (st.hasMoreTokens()) {
					String firstToken = st.nextToken();
					if ("list".equals(firstToken) || "tree".equals(firstToken)
							|| "spec".equals(firstToken)) {
						if (st.hasMoreTokens()) {
							String domainName = st.nextToken();
							domainList.add(domainName);
							if (super.hasDomain(domainName)) {
								domainList.addAll(this
										.getSubDomainNameSet(domainName));
							}
						}
					}
				}
			}
		}
		return domainList;
	}

	private void fillSubDomainNameSet(Set<String> subDomainNameSet,
			String domainName, int deep) {
//		if (deep > 3) {
//			return;
//		}
		List<FormField> fields = super.getDomainFormDesc(domainName);
		if (null == fields) {
			return;
		}
		for (FormField formField : fields) {
			if (RepresentationType.TAB.equals(formField.getType().getView())
					|| RepresentationType.REFERENCE.equals(formField.getType()
							.getView())) {
				String subDomainName = formField.getType().getRefDomainName();
				if (!subDomainNameSet.contains(subDomainName)) {
					subDomainNameSet.add(subDomainName);
					fillSubDomainNameSet(subDomainNameSet, subDomainName,
							deep + 1);
				}
			}
		}
	}

	private Set<String> getSubDomainNameSet(String domainName) {
		Set<String> subDomainNameSet = new HashSet<String>();
		fillSubDomainNameSet(subDomainNameSet, domainName, 1);
		return subDomainNameSet;
	}

	// private Set<String> getSubDomainNameSet(String domainName) {
	// Set<String> subDomainNameSet = new HashSet<String>();
	// List<FormField> fields = super.getDomainFormDesc(domainName);
	// if (null == fields) {
	// return subDomainNameSet;
	// }
	// for (FormField formField : fields) {
	// if (RepresentationType.TAB.equals(formField.getType().getView())) {
	// subDomainNameSet.add(formField.getType().getRefDomainName());
	// subDomainNameSet.addAll(this.getSubDomainNameSet(formField
	// .getType().getRefDomainName()));
	// } else if (RepresentationType.REFERENCE.equals(formField.getType()
	// .getView())) {
	// subDomainNameSet.add(formField.getType().getRefDomainName());
	// }
	// }
	//
	// return subDomainNameSet;
	// }

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
