package org.kidayi.ehcacheUpdateLock;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CacheReloadLockManager {
	private Logger logger=Logger.getLogger(CacheReloadLockManager.class);
	
	private Map<Object,CacheReloadLock> lockMap=new HashMap<Object, CacheReloadLock>();
	
	public synchronized CacheReloadLock getLock(Object key){
		CacheReloadLock lock=lockMap.get(key);
		if(lock==null){
			lock=new CacheReloadLock();
			lockMap.put(key, lock);
		}else{
			lock.incrementUserCount();
		}
		return lock;
	}
	
	public synchronized void releaseLock(Object key){
		CacheReloadLock lock=lockMap.get(key);
		if(lock!=null){
			int useCount=lock.decrementUserCount();
			if(useCount<1){
				lockMap.remove(key);
			}
		}
	}
}
