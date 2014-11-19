package com.srini.zktrial;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.framework.CuratorFramework;

public class TaskToPerform {

	CuratorFramework client;
	
   private final AtomicBoolean inUse = new AtomicBoolean(false);

   public void setClient(CuratorFramework client){
	   this.client = client;
   }
   
   public void   perform(String path, int val) throws Exception{
      
	   // in a real application this would be accessing/manipulating a shared resource
       if ( !inUse.compareAndSet(false, true) ){
    	   throw new IllegalStateException("Needs to be used by one client at a time");
       }
       try{
		   // perform some task...
    	   System.out.println("Task Value: " +val);
    	   byte[] nodeVal = client.getData().forPath(path);
    	   String nodeSVal = "";
    	   if(nodeVal != null)
    		   nodeSVal = new String(nodeVal);
    	   System.out.println("Node value Read: " +nodeSVal);
    	   client.setData().forPath(path, new Integer(val).toString().getBytes());
    	   
	       Thread.sleep((long)(3 * Math.random()));
	   }
	   finally
	   {
		   inUse.set(false);
	   }
	}	
}
