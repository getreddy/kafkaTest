package com.srini.zktrial;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import java.util.concurrent.TimeUnit;

public class SharedLockResource {

	private final InterProcessMutex lock;
	private final TaskToPerform resource;
	private final String clientName;
	private int val = 0;
	private String path = "";
	private CuratorFramework client = null;
	
	public SharedLockResource(CuratorFramework client, String lockPath, TaskToPerform resource, String clientName){
		this.resource = resource;
		this.clientName = clientName;
		path = lockPath;
		this.client = client;
		lock = new InterProcessMutex(client, lockPath);
	}
	
	public void setValue(int val){
		this.val = val;
	}
	
	public void	doWork(long time, TimeUnit unit) throws Exception{
		
		if ( !lock.acquire(time, unit) ){
			throw new IllegalStateException(clientName + " could not acquire the lock");
		}
		try{
			System.out.println(clientName + " has the lock");
			resource.setClient(client);
			resource.perform(path, val);
		}finally{
			System.out.println("=====" +clientName + " releasing the lock=====");
			lock.release(); 
		}
			
	}
	
}
