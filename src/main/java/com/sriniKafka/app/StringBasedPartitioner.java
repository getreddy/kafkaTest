package com.sriniKafka.app;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class StringBasedPartitioner implements Partitioner{

	public StringBasedPartitioner (VerifiableProperties props) {

    }
	
	static int cnt = 0;
	
	public int partition(Object arg0, int numOfPartitions) {
		int partition = 0;
		
		
		if(arg0 instanceof String){
			String key = (String) arg0;
			partition = Integer.parseInt(key);
			partition = partition % numOfPartitions;
		}
		/*if(arg0 instanceof String){
			String key = (String) arg0;
			int offset = key.lastIndexOf('.');
	        if (offset > 0) {
	        	System.out.println("numOfPartitions: " +numOfPartitions);
	           partition = Integer.parseInt( key.substring(offset+1)) % numOfPartitions;
	        }
		}*/
		/*if(cnt > 4 && cnt <=7)
			partition = 1; 
		System.out.println("partition value 1: " +partition);
		cnt++;*/
		/*if(arg0 != null){
			if(arg0.equals("192.168.125.156")){
				partition = 0;
			}else if(arg0.equals("192.168.125.158")){
				partition = 1;
			}else if(arg0.equals("192.168.125.160")){
				partition = 2;
			}
		}*/
		 long thid = Thread.currentThread().getId();
		System.out.println("ThID: " +thid + " ****Partition******" +partition);
		return partition;
	}

}
