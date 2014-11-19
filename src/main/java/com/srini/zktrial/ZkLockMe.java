package com.srini.zktrial;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

public class ZkLockMe {

	
	private static final String     PATH = "/srinipath/locks";
	TaskToPerform resource1 = new TaskToPerform();
	
	
	
	class ConsumeData implements Callable<Void>{

		int index;
		public ConsumeData(int index){
			this.index = index;
		}
		
		public Void call() throws Exception {
			CuratorFramework  client = CuratorFrameworkFactory.newClient("localhost", new ExponentialBackoffRetry(1000, 3));
			try{
				client.start();
				SharedLockResource lockMe = new SharedLockResource(client, PATH, resource1, "Client " + index);
				//client.create().forPath(PATH, "".getBytes());
				lockMe.setValue(index);
				lockMe.doWork(10, TimeUnit.SECONDS);
				
				
			}catch(Throwable ex){
				ex.printStackTrace();
			}finally{
				CloseableUtils.closeQuietly(client);
			}
			return null;
		}
		
	}
	
	public void testLock(){
		ExecutorService	service = Executors.newFixedThreadPool(3);
		
		for(int in = 0; in<3; in++){
			ConsumeData cObj = new ConsumeData(in);
			service.submit(cObj);
		}
		
		
	}
	
	public static void main(String args[]){
		
		ZkLockMe obj = new ZkLockMe();
		obj.testLock();
		
		System.out.println("Done playing with lock..");
		
	}
}
