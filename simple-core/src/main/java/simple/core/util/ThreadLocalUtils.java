package simple.core.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtils {
	private static ThreadLocal<Map> localCache = new ThreadLocal<Map>();

	private static Map getCache() {
		Map map = localCache.get();
		if (map == null) {
			synchronized (localCache) {
				if (map == null) {
					localCache.set(new HashMap());
				}
			}
		}
		return localCache.get();
	}

	public static <T> T get(String key) {
		Map map = getCache();
		return (T) map.get(key);
	}

	public static void set(String key, Object value) {
		Map map = getCache();
		map.put(key, value);
	}

	public static void clear() {
		Map map = getCache();
		map.clear();
	}
	
	public static boolean isExist(String key){
		Map map = getCache();
		return map.containsKey(key);
	}
}
