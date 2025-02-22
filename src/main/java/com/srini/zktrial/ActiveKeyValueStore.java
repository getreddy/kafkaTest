package com.srini.zktrial;

import java.nio.charset.Charset;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;


public class ActiveKeyValueStore extends ConnectionWatcher {
	private  final Charset CHARSET = Charset.forName("UTF-8");
	
	public ActiveKeyValueStore(){
		
	}
	public void write(String path, String value) throws InterruptedException,
	KeeperException {
		Stat stat = zk.exists(path, false);
		if (stat == null) {
			zk.create(path, value.getBytes(CHARSET), Ids.OPEN_ACL_UNSAFE,
			CreateMode.PERSISTENT);
		} else {
			zk.setData(path, value.getBytes(CHARSET), -1);
		}
	}
	
	public void write(String path, byte[] value) throws InterruptedException,
	KeeperException {
		Stat stat = zk.exists(path, false);
		if (stat == null) {
			zk.create(path, value, Ids.OPEN_ACL_UNSAFE,
			CreateMode.PERSISTENT);
		} else {
			zk.setData(path, value, -1);
		}
	}
	
	public byte[] readValue(String path, Watcher watcher) throws InterruptedException,
	KeeperException {
		byte[] data = zk.getData(path, watcher, null/*stat*/);
		return data;
	}
	
	
	public String read(String path, Watcher watcher) throws InterruptedException,
	KeeperException {
		byte[] data = zk.getData(path, watcher, null/*stat*/);
		return new String(data, CHARSET);
	}
	
	public void join(String groupName, String memberName) throws KeeperException,
	InterruptedException {
		String path = groupName + "/" + memberName;
		String createdPath = zk.create(path, null/*data*/, Ids.OPEN_ACL_UNSAFE,
		CreateMode.PERSISTENT);
		System.out.println("Created " + createdPath);
	}
	
}


