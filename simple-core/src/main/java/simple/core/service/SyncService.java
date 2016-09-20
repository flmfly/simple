package simple.core.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import simple.config.annotation.Synchronization;
import simple.core.model.SyncRequest;
import simple.core.model.SyncResult;
import simple.core.service.listener.DomainChangedListener;

@Service
public class SyncService extends BaseService {

	private static final Logger LOG = LoggerFactory
			.getLogger(SyncService.class);

	private Map<String, Synchronization> syncMap = new HashMap<String, Synchronization>();

	private static Map<String, List<Map<String, Object>>> cache = new HashMap<String, List<Map<String, Object>>>();

	private static Map<String, String> fingerprintMap = new HashMap<String, String>();

	private Boolean domainChangedListenerRegisted = false;

	// @PostConstruct
	// public void init() {
	//
	// }

	@SuppressWarnings("unchecked")
	public SyncResult sync(String domainName, SyncRequest syncRequest) {
		SyncResult result = new SyncResult();
		String syncKey = domainName + "_";
		Class<?> clazz = super.getDomainClass(domainName);
		String filter = null;
		Map<?, ?> by = syncRequest.getBy();

		if (null != by && !by.isEmpty()) {
			List<String> keyList = new ArrayList<String>();
			keyList.addAll((Collection<? extends String>) by.keySet());
			Collections.sort(keyList);

			syncKey = syncKey + StringUtils.join(keyList, "_");
			Set<String> filterSet = new HashSet<String>(by.size());

			for (Iterator<?> iterator = by.keySet().iterator(); iterator
					.hasNext();) {
				String key = (String) iterator.next();
				Object val = by.get(key);
				if (val instanceof Number) {
					Number valNumber = (Number) val;
					if (isInteger(valNumber)) {
						filterSet.add(key + "=" + valNumber.longValue());
					} else {
						filterSet.add(key + "=" + val.toString());
					}
				} else if (val instanceof String) {
					filterSet.add(key + "='" + val.toString() + "'");
				}
			}

			filter = StringUtils.join(filterSet, " and ");
		}

		if (!syncMap.containsKey(syncKey)) {
			this.processDomainSync(domainName);
		}
		List<Map<String, Object>> dataList = null;
		Synchronization synchronization = syncMap.get(syncKey);
		Date lastUpdateDate = new Date();
		String cacheKey = domainName + "_" + filter;
		if (null != synchronization
				&& !"".equals(synchronization.lastUpdateFieldName())) {
			if(null !=syncRequest.getLastUpdateTime()){
			filter = filter + " and " + synchronization.lastUpdateFieldName()
					+ ">=?";
			this.fetchDataList(clazz, filter, synchronization,syncRequest.getLastUpdateTime());
			}else{
				dataList = this.fetchDataList(clazz, filter, synchronization,null);
			}
		} else {
			// fingerprint matched
			if (null != fingerprintMap.get(cacheKey)
					&& fingerprintMap.get(cacheKey).equals(
							syncRequest.getFingerprint())) {
				LOG.info(cacheKey + ": fingerprint matched, igonre!");
			} else { // fingerprint not matched

				if (null != synchronization) {

					if (synchronization.cache()) {
						dataList = cache.get(cacheKey);
					}

					if (null == dataList) {
						dataList = this.fetchDataList(clazz, filter,
								synchronization, null);

						if (synchronization.cache()) {
							cache.put(cacheKey, dataList);
							this.checkDomainChangedListener();
						}
						LOG.info(cacheKey + ": from database!");
					} else {
						LOG.info(cacheKey + ": from cache!");
					}
				} else {
					syncMap.put(syncKey, null);
				}
			}

//			if (null != dataList) {
//			}
		}

		if (null == dataList) {
			result.setIgnore(true);
		}else{
			try {
				String md5 = new String(DigestUtils.md5Digest(DEFAULT_GSON
						.toJson(dataList).getBytes("utf-8")), "utf-8");
				fingerprintMap.put(cacheKey, md5);
				this.checkDomainChangedListener();
				result.setFingerprint(md5);

				if (null != syncRequest.getFingerprint()
						&& syncRequest.getFingerprint().equals(md5)) {
					result.setIgnore(true);
				} else {
					result.setDataList(dataList);
					result.setIgnore(false);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		result.setLastUpdateTime(lastUpdateDate);

		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> fetchDataList(Class<?> clazz,
			String filter, Synchronization synchronization, Date lastUpdateDate) {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

		List<Object> paremeters = null;

		if (null != lastUpdateDate) {
			paremeters = new ArrayList<Object>();
			paremeters.add(lastUpdateDate);
		}

		List<Object[]> list = (List<Object[]>) this.hibernateBaseDAO
				.findByProperties(clazz, synchronization.synchronizedFiels(),
						filter, paremeters);
		String[] fields = synchronization.synchronizedFiels().split(",");

		for (Object[] row : list) {
			Map<String, Object> rowMap = new HashMap<String, Object>();

			Map<String, Map<String, Object>> mapContainer = new HashMap<String, Map<String, Object>>();
			mapContainer.put("#root#", rowMap);

			for (int i = 0; i < fields.length; i++) {
				if (fields[i].indexOf(".") != -1) {
					this.processFieldVal("#root#." + fields[i], row[i],
							mapContainer);
				} else {
					rowMap.put(fields[i], row[i]);
				}
			}
			dataList.add(rowMap);
			mapContainer.clear();
		}
		return dataList;
	}

	private void checkDomainChangedListener() {
		synchronized (domainChangedListenerRegisted) {
			if (!domainChangedListenerRegisted) {
				super.domainChangedService
						.registDomainChangedListener(new DomainChangedListener() {

							@Override
							public void onDomainChanged(ChangeInfo changeInfo) {
								Class<?> domainClass = changeInfo.getDomain()
										.getClass();
								String domainName = getDomainNameByClass(domainClass);
								List<Synchronization> syncList = getSynchronization(domainName);

								// TODO: add process with by value changed
								if (null != syncList) {
									for (String key : getOutOfDateCacheKey(domainName)) {
										cache.remove(key);
									}

									for (String key : getOutOfDateFingerprintKey(domainName)) {
										fingerprintMap.remove(key);
									}
								}
							}

							private Set<String> getOutOfDateCacheKey(
									String domainName) {
								String keyPrefix = domainName + "_";
								Set<String> result = new HashSet<String>();
								for (String key : cache.keySet()) {
									if (key.startsWith(keyPrefix)) {
										result.add(key);
									}
								}

								return result;
							}

							private Set<String> getOutOfDateFingerprintKey(
									String domainName) {
								String keyPrefix = domainName + "_";
								Set<String> result = new HashSet<String>();
								for (String key : fingerprintMap.keySet()) {
									if (key.startsWith(keyPrefix)) {
										result.add(key);
									}
								}

								return result;
							}
						});
				domainChangedListenerRegisted = true;
			}
		}
	}

	private void processFieldVal(String field, Object val,
			Map<String, Map<String, Object>> mapContainer) {
		String copyField = field;
		Object copyVal = val;
		while (copyField.contains(".")) {
			int index = copyField.lastIndexOf(".");
			String mapKey = copyField.substring(0, index);
			String valField = copyField
					.substring(index + 1, copyField.length());

			Map<String, Object> map = mapContainer.get(mapKey);
			if (null == map) {
				map = new HashMap<String, Object>();
				mapContainer.put(mapKey, map);
			}

			map.put(valField, copyVal);
			copyVal = map;
			copyField = mapKey;
		}
	}

	private void processDomainSync(String domainName) {
		List<Synchronization> syncList = super.getSynchronization(domainName);
		if (null != syncList) {
			for (Synchronization sync : syncList) {
				String[] bys = sync.by().split(",");
				Arrays.sort(bys);
				String syncKey = domainName + "_" + StringUtils.join(bys, "_");
				syncMap.put(syncKey, sync);
			}
		}
	}

	public void onSyncDomainChanged(Object domain) {

	}

	private boolean isInteger(Number number) {
		return number.longValue() - number.doubleValue() == 0;
	}

}
