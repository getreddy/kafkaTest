package com.sriniKafka.app;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

public class SimpleConsumerTest {

	
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }	
	
	public static void main(String args[]){
		
		long maxReads = 4;
		String topic = "mytopic";
	    int partition = 0;
	    List<String> seeds = new ArrayList<String>();
	    seeds.add("192.168.125.156");
	    int port = 9092;
	    
	    SimpleConsumerTest example = new SimpleConsumerTest();	
	    try {
            example.run(maxReads, topic, partition, seeds, port);
        } catch (Exception e) {
            System.out.println("Oops:" + e);
             e.printStackTrace();
        }
	    System.out.println("Done with SimpleConsumer..");
	}
	
	
	
	// store the last offset in zookeeper
	public long readMsgsFromOffset(CuratorFramework client, String a_topic, int a_partition, List<String> a_seedBrokers, int a_port,  int numOfMsgsToRead) throws Exception{

		String Path = "/SriniConsumerTest"  + "/" +a_topic + "offsetsLock";
		InterProcessMutex lock = new InterProcessMutex(client, Path);
		
		String clientThName = "SriniConsumerTest " + Thread.currentThread().getId();
		if ( !lock.acquire(100, TimeUnit.SECONDS) ){
			throw new IllegalStateException(clientThName + " could not acquire the lock");
		}
		
		long readOffset = 0;
		String clientName = "";
		try{
			// read the offset from the path
			byte[] nodeVal = client.getData().forPath(Path);
	    	String nodeSVal = "0";
	    	if(nodeVal.length > 0)
	    		nodeSVal = new String(nodeVal);
	    	
	    	readOffset = Long.parseLong(nodeSVal);
			
			PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
	        if (metadata == null) {
	            System.out.println("Can't find metadata for Topic and Partition. Exiting");
	            return readOffset;
	        }
	        
	        String leadBroker = metadata.leader().host();
	        clientName = "clientThName" + a_topic + "_" + a_partition;
	        
	        System.out.println("Lead Broker Name: " +leadBroker);
	        System.out.println("ClientName: " +clientName);
			
			FetchRequest req = new FetchRequestBuilder()
		        .clientId(clientName)
		        .addFetch(a_topic, a_partition, readOffset, 100000) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
		        .build();
			
			SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
			
			//long readOffset1 = getLastOffset(consumer,a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(), clientName);
			
		    FetchResponse fetchResponse = consumer.fetch(req);
		    int numErrors = 0;
		    if (fetchResponse.hasError()) {
		    	numErrors++;
		        // Something went wrong!
		        short code = fetchResponse.errorCode(a_topic, a_partition);
		        System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
		        consumer.close();
		        consumer = null;
		        return readOffset;
		    }
		    
		    long numRead = 0;
		    //long readOffset = fromOffset;
	        for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
	        	
	        	//readOffset = messageAndOffset.nextOffset();
	        	long currentOffset = messageAndOffset.offset();
	            if (currentOffset < readOffset) {
	                System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
	                continue;
	            }
	            
	            readOffset = messageAndOffset.nextOffset();
	            System.out.println("offset stat: " +readOffset);
	            ByteBuffer payload = messageAndOffset.message().payload();
	
	            byte[] bytes = new byte[payload.limit()];
	            payload.get(bytes);
	            MsgClass msgObj = null;
	        	String keyS = "";
	    		try {
					msgObj = (MsgClass) toObject(bytes);
					//if(keyMsg != null)
						//keyS = (String) toObject(keyMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
	            
	            
	            //System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
	            System.out.println(String.valueOf(messageAndOffset.offset()) + ": "  + msgObj.getMessage());
	            numRead++;
	            if(numRead >= numOfMsgsToRead){
	            	// set the offset in the zNode
	            	client.setData().forPath(Path, new Long(readOffset).toString().getBytes());
	            	break;
	            }
	        }
	        
	        if (consumer != null) consumer.close();
        
		}finally{
			System.out.println("=====" +clientName + " releasing the lock=====");
			lock.release(); 
		}
        return readOffset;
	}
    
	public void run(long a_maxReads, String a_topic, int a_partition, List<String> a_seedBrokers, int a_port) throws Exception {
		
		CuratorFramework  client = CuratorFrameworkFactory.newClient("localhost", new ExponentialBackoffRetry(1000, 3));
		try{
			client.start();
			long currentoffset = readMsgsFromOffset(client, a_topic, a_partition, a_seedBrokers, a_port, 3);
			System.out.println("Read the first batch..offset: " +currentoffset);
			
			currentoffset = readMsgsFromOffset(client, a_topic, a_partition, a_seedBrokers, a_port, 3);
			System.out.println("Read the Second batch..offset: " +currentoffset);
			
		}catch(Throwable ex){
			ex.printStackTrace();
		}finally{
			CloseableUtils.closeQuietly(client);
		}
		
		/*long currentoffset = readMsgsFromOffset(a_topic, a_partition, a_seedBrokers, a_port, 0, 3);
		
		System.out.println("Current offset: " +currentoffset);
		
		currentoffset = readMsgsFromOffset(a_topic, a_partition, a_seedBrokers, a_port, currentoffset, 3);
		
		System.out.println("Current offset: " +currentoffset);*/
		
		/*PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
        if (metadata == null) {
            System.out.println("Can't find metadata for Topic and Partition. Exiting");
            return;
        }
        
        String leadBroker = metadata.leader().host();
        String clientName = "Client_" + a_topic + "_" + a_partition;
        
        System.out.println("Lead Broker Name: " +leadBroker);
        System.out.println("ClientName: " +clientName);
        
        
        SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
        long readOffset = getLastOffset(consumer,a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(), clientName);
        
        System.out.println("ReadOffset: " +readOffset);
        
        int numErrors = 0;
        while (a_maxReads > 0) {
        	System.out.println("While reading..");
            if (consumer == null) {
                consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
            }
            
            FetchRequest req = new FetchRequestBuilder()
	            .clientId(clientName)
	            .addFetch(a_topic, a_partition, readOffset, 100000) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
	            .build();
	            
            FetchResponse fetchResponse = consumer.fetch(req);
            if (fetchResponse.hasError()) {
            	numErrors++;
                // Something went wrong!
                short code = fetchResponse.errorCode(a_topic, a_partition);
                System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                consumer.close();
                consumer = null;
                break;
            }
            
            long numRead = 0;
            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
                long currentOffset = messageAndOffset.offset();
                if (currentOffset < readOffset) {
                    System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                    continue;
                }
                
                if(readOffset == 4)
                	break;
                readOffset = messageAndOffset.nextOffset();
                System.out.println("offset stat: " +readOffset);
                ByteBuffer payload = messageAndOffset.message().payload();
 
                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                MsgClass msgObj = null;
            	String keyS = "";
        		try {
					msgObj = (MsgClass) toObject(bytes);
					//if(keyMsg != null)
						//keyS = (String) toObject(keyMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
                
                
                //System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
                System.out.println(String.valueOf(messageAndOffset.offset()) + ": "  + msgObj.getMessage());
                numRead++;
                a_maxReads--;
            }
 
            
            
            
            
            if (numRead == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
            
            
            
        }
        
        if (consumer != null) consumer.close();*/
        
	}
	
	 public static long getLastOffset(SimpleConsumer consumer, String topic, int partition,
             long whichTime, String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new 
										HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
				requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
		OffsetResponse response = consumer.getOffsetsBefore(request);
		
		if (response.hasError()) {
			System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition) );
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	 }
	 
	private PartitionMetadata findLeader(List<String> a_seedBrokers, int a_port, String a_topic, int a_partition) {
        PartitionMetadata returnMetaData = null;
        loop:
        for (String seed : a_seedBrokers) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup");
 
                List<String> topics = Collections.singletonList(a_topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
 
                List<TopicMetadata> metaData = resp.topicsMetadata();
 
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        /*if (returnMetaData != null) {
            m_replicaBrokers.clear();
            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
                m_replicaBrokers.add(replica.host());
            }
        }*/
        return returnMetaData;
    }	
	
}
