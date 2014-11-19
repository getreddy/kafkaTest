package com.srini.zktrial;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.KeeperException;

public class ZkTryOut {

	
	public static final String PATH = "/SriniConfig";
	
	
	public void testZkNodes() throws IOException, InterruptedException, KeeperException{
		ActiveKeyValueStore store;
		Random random = new Random();
		
		store = new ActiveKeyValueStore();
		store.connect("localhost");
		
		String value = "Reddy";
		store.write(PATH, value);
		System.out.printf("Set %s to %s\n", PATH, value);
		
		store.join(PATH, "Child2");
		store.join("/SriniConfig/Child2", "Child2_Child1");
		
		//store.write(PATH, "SriniReddy");
		//store.write(PATH, "SriniReddy1");
		
		
	}
	
	
	public static void main(String args[]){
		
		ZkTryOut zkObj = new ZkTryOut();
		
		try {
			zkObj.testZkNodes();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Done...");
	}
}
