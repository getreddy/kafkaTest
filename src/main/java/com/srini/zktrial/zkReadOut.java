package com.srini.zktrial;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

public class zkReadOut {
	
	
	
	class ConfigWatcher implements Watcher {
		private ActiveKeyValueStore store;
		
		public ConfigWatcher(String hosts) throws IOException, InterruptedException {
			store = new ActiveKeyValueStore();
			store.connect(hosts);
		}
		
		public void displayConfig() throws InterruptedException, KeeperException {
			String value = store.read(ZkTryOut.PATH, this);
			System.out.printf("Read %s as %s\n", ZkTryOut.PATH, value);
		}
		public void process(WatchedEvent event) {
			if (event.getType() == EventType.NodeDataChanged) {
				try {
					System.out.println("Captured Event...");
					displayConfig();
				} catch (InterruptedException e) {
					System.err.println("Interrupted. Exiting.");
					Thread.currentThread().interrupt();
				} catch (KeeperException e) {
					System.err.printf("KeeperException: %s. Exiting.\n", e);
				}
			}
			
			
		}
	}
	
	public void testReading(){
		
		ConfigWatcher configWatcher;
		try {
			configWatcher = new ConfigWatcher("localhost");
			configWatcher.displayConfig();
			
			Thread.sleep(Long.MAX_VALUE);
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
		
	}		
	
	
	
	public static void main(String args[]){
		zkReadOut zkObj = new zkReadOut();
		zkObj.testReading();
	}
		
	

}
