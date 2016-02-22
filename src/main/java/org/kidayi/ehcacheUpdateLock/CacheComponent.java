package org.kidayi.ehcacheUpdateLock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;

public class CacheComponent{
	private Logger logger=Logger.getLogger(CacheComponent.class);
	private CacheManager cacheManager;
	private Cache cache;
	private CacheUpdateLockManager reloadLockManager=new CacheUpdateLockManager();
	
	public CacheComponent(String configAllPath,String cacheName){
		this.cacheManager=CacheManager.newInstance(configAllPath);
		this.cache=cacheManager.getCache(cacheName);
		if(this.cache==null){
			cacheManager.addCache(cacheName);
			this.cache=cacheManager.getCache(cacheName);
		}
	}
	
	public CacheComponent(String cacheName){
		String configPath=ClassLoader.getSystemResource("conf/custom/env/ehcache.xml").getPath();
		this.cacheManager=CacheManager.newInstance(configPath);
		this.cache=cacheManager.getCache(cacheName);
		if(this.cache==null){
			cacheManager.addCache(cacheName);
			this.cache=cacheManager.getCache(cacheName);
		}
	}
	
	public void put(Map<String,Object> kv){
		if(MapUtils.isNotEmpty(kv)){
			for (String key:kv.keySet()) {
				if(key!=null && kv.get(key)!=null){
					put(key, kv.get(key));
				}
			}
		}
	}
	
	public void put(String key,Object value){
		long startTime=System.currentTimeMillis();
		try{
			if(key!=null && value!=null){
				Element e=new Element(key, value);
				cache.put(e);
			}
		}finally{
			logger.info("put finally");
		}
	}
	
	public Object get(String key){
		long startTime=System.currentTimeMillis();
		try{
			Element e=cache.get(key);
			if(e!=null){
				return e.getObjectValue();
			}
			return null;
		}finally{
			logger.info("get finally");
		}
	}
	
	public boolean remove(String key) {
		long startTime = System.currentTimeMillis();
		try {
			return cache.remove(key);
		} finally {
			logger.info("removeo finally");
		}
	}

	public Set<String>  getAllOrPartly(Set<String> keySet,Map<String,Object> valueMapBack){
		Set<String> noValueKeySet=new HashSet<>();
		if(valueMapBack==null){
			return keySet;
		}
		
		if(CollectionUtils.isEmpty(keySet)){
			return noValueKeySet;
		}
		
		for (String key : keySet) {
			Object value=get(key);
			if(value!=null){
				valueMapBack.put(key, value);
			}else{
				noValueKeySet.add(key);
			}
		}
		
		return noValueKeySet;
	}
	
	public <T> T refresh(String cachekey,CacheRefreshRunnable cacheRefreshRunnable,
			long lockTimeoutMilliseconds,Class<T> valueType){
		CacheUpdateLock lock=null;
		try{
			lock=reloadLockManager.getLock(cachekey);
			if(lock.tryLock(lockTimeoutMilliseconds,TimeUnit.MILLISECONDS)){
				try{
					Element e= cache.get(cachekey);
					logger.debug("ehcache in refresh back:"+e);
					if(e!=null && e.getObjectValue()!=null){
						return (T)e.getObjectValue();
					}
					Object value=cacheRefreshRunnable.run();
					logger.debug("load back:"+value);
					if(value!=null){
						this.put(cachekey, value);
						return (T)value;
					}
				}catch(Throwable e){
					logger.error(e);
				}finally{
					lock.unlock();
				}
			}
		}catch(Throwable e){
			logger.error(e);
		}finally{
			reloadLockManager.releaseLock(cachekey);
		}
		return null;
	}
	
	public <T> Map<String,T> refreshBatch(Set<String> noValueKeySet,CacheRefreshBatchRunnable cacheRefreshListRunnable,
			long lockTimeoutMilliseconds,Class<T> valueType){
		CacheUpdateLock lock=null;
		try{
			lock=reloadLockManager.getLock(noValueKeySet);
			if(lock.tryLock(lockTimeoutMilliseconds,TimeUnit.MILLISECONDS)){
				try{
					Map<String,Object> valueMapBack=new HashMap<String, Object>();
					Set<String> loadKeySet=getAllOrPartly(noValueKeySet, valueMapBack);
					if(CollectionUtils.isNotEmpty(loadKeySet)){
						Map loadBackMap=cacheRefreshListRunnable.run(loadKeySet);
						if(MapUtils.isNotEmpty(loadBackMap)){
							this.put(loadBackMap);
							valueMapBack.putAll(loadBackMap);
						}
					}
					return (Map<String,T>)valueMapBack;
				}catch(Throwable e){
					logger.error(e);
				}finally{
					lock.unlock();
				}
			}
		}catch(Throwable e){
			logger.error(e);
		}finally{
			reloadLockManager.releaseLock(noValueKeySet);
		}
		return null;
	}
}
