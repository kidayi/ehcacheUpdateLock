package org.kidayi.ehcacheUpdateLock;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CacheUpdateLockManager {
	private Logger logger=Logger.getLogger(CacheUpdateLockManager.class);
	
	private Map<Object,CacheUpdateLock> lockMap=new HashMap<Object, CacheUpdateLock>();
	
	public synchronized CacheUpdateLock getLock(Object key){
		CacheUpdateLock lock=lockMap.get(key);
		if(lock==null){
			lock=new CacheUpdateLock();
			lockMap.put(key, lock);
		}else{
			lock.incrementUserCount();
		}
		return lock;
	}
	
	public synchronized void releaseLock(Object key){
		CacheUpdateLock lock=lockMap.get(key);
		if(lock!=null){
			int useCount=lock.decrementUserCount();
			if(useCount<1){
				lockMap.remove(key);
			}
		}
	}
}
