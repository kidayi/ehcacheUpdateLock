package org.kidayi.ehcacheUpdateLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class CacheUpdateLock {
	private Logger logger=Logger.getLogger(CacheUpdateLock.class);
	
	private ReentrantLock lock=new ReentrantLock();
	
	private int userCount=1;

	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException{
		return lock.tryLock(timeout, unit);
	}
	
	public void unlock(){
		lock.unlock();
	}
	
	public synchronized int incrementUserCount() {
		this.userCount++;
		return this.userCount;
	}
	
	public synchronized int getUserCount() {
		return this.userCount;
	}
	
	
	public synchronized int decrementUserCount() {
		this.userCount--;
		return this.userCount;
	}
	
	
}
