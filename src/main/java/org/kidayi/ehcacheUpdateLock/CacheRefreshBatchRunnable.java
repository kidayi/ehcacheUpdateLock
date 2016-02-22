package org.kidayi.ehcacheUpdateLock;

import java.util.Map;
import java.util.Set;


public interface CacheRefreshBatchRunnable{
	public Map<String,Object> run(Set<String> loadKeySet);
}
