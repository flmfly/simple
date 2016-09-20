package simple.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import simple.core.service.listener.DomainChangedListener;

@Service
public class DomainChangedService {

	private List<DomainChangedListener> domainChangedListenerList = new ArrayList<DomainChangedListener>();

	public void registDomainChangedListener(
			DomainChangedListener domainChangedListener) {
		domainChangedListenerList.add(domainChangedListener);
	}

	public void removeDomainChangedListener(
			DomainChangedListener domainChangedListener) {
		domainChangedListenerList.remove(domainChangedListener);
	}

	public List<DomainChangedListener> getDomainChangedListenerList() {
		return domainChangedListenerList;
	}
}
