package com.sriniKafka.app;

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

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class SriniKafkaConsumerTh extends AbstractExecutionThreadService{

	String topicName;
	int partitionNum;
	int numOfMessages;
	InterProcessMutex lock = null;
	CuratorFramework  client = null;
	String zkPath = "";
	int port = 9092;
	List<String> seeds = new ArrayList<String>();
	
	
	public SriniKafkaConsumerTh(String topicName, int partitionNum, int numOfMessages){
		this.topicName = topicName;
		this.partitionNum = partitionNum;
		this.numOfMessages = numOfMessages;
		zkPath = "/SriniConsumerTest"  + "/" +topicName + "/" + partitionNum + "/" + "offsetsLock";
	}
	
	protected void startUp() throws Exception{
		
		System.out.println("Startup method...");
		
		// kafka specific
		// read this information (host and port num) from the config file
	    seeds.add("192.168.125.156");
	    
	    
		// zookeeper specific
		client = CuratorFrameworkFactory.newClient("localhost", new ExponentialBackoffRetry(1000, 3));
		client.start();
		lock = new InterProcessMutex(client, zkPath);
	}
	
	
	@Override
	protected void run() throws Exception {
		
		String clientThName = "SriniConsumerTest_" + Thread.currentThread().getId();
		long readOffset = 0;
		long maxOffsetNum = 0;
		while(true && isRunning()){
			//System.out.println("SriniKafkaConsumerTh working..");
			
			// Find the broker leader (when there are multiple brokers)
			PartitionMetadata metadata = findLeader(seeds, port, topicName, partitionNum);
			if (metadata == null) {
	            System.out.println("Can't find metadata for Topic and Partition. Exiting");
	            return;
	        }
	        String leadBroker = metadata.leader().host();
	        System.out.println("LeadBroker: " +leadBroker + " PartitionNum: " + partitionNum + "..ClientName: " +clientThName);
	        
	        SimpleConsumer consumer = new SimpleConsumer(leadBroker, port, 100000, 64 * 1024, clientThName);
	        
	        maxOffsetNum = getLastOffset(consumer,topicName, partitionNum, kafka.api.OffsetRequest.LatestTime(), clientThName);
	        if(readOffset >= maxOffsetNum){
	        	Thread.sleep(1000);
	        	continue;
	        }
			//
			// Read the offset from the zookeeper and update the offset again so that 
			// other thread will read it from there. 
			try{
				if ( !lock.acquire(100, TimeUnit.SECONDS) ){
					throw new IllegalStateException(clientThName + " could not acquire the lock");
				}
				byte[] nodeVal = client.getData().forPath(zkPath);
		    	String nodeSVal = "0";
		    	if(nodeVal.length > 0)
		    		nodeSVal = new String(nodeVal);
		    	
		    	readOffset = Long.parseLong(nodeSVal);
		    	
		    	 
		    	maxOffsetNum = getLastOffset(consumer,topicName, partitionNum, kafka.api.OffsetRequest.LatestTime(), clientThName);
		    	
				long nextOffset = readOffset + numOfMessages;
				if((maxOffsetNum > 0) && (nextOffset > maxOffsetNum))
					nextOffset = maxOffsetNum;
				
				client.setData().forPath(zkPath, new Long(nextOffset).toString().getBytes());
			}finally{
				System.out.println("=====" +clientThName + " releasing the lock=====");
				lock.release(); 
			}
			
			//
			FetchRequest req = new FetchRequestBuilder()
	        .clientId(clientThName)
	        .addFetch(topicName, partitionNum, readOffset, 100000) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
	        .build();
		
			
			FetchResponse fetchResponse = consumer.fetch(req);
		    //int numErrors = 0;
		    if (fetchResponse.hasError()) {
		    	//numErrors++;
		        // Something went wrong!
		        short code = fetchResponse.errorCode(topicName, partitionNum);
		        System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
		        consumer.close();
		        consumer = null;
		        return;
		    }
	    	int numRead = 0;
	    	//maxOffsetNum = fetchResponse.highWatermark(topicName, partitionNum);
	    	//System.out.println("High Water Mark: " +maxOffsetNum);
		    for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topicName, partitionNum)) {
	        	
		    	System.out.println("======");
		    	
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
	            System.out.println("" + clientThName + ": " +String.valueOf(messageAndOffset.offset()) + ": "  + msgObj.getMessage());
	            numRead++;
	            if(numRead >= numOfMessages){
	            	break;
	            }
	        }
	        
	        if (consumer != null) consumer.close();
		    
			//Thread.sleep(2000);
		}
		
	}

	protected void shutDown() throws Exception{
		System.out.println("Shutdown method...");
		//stop();
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
	
	public static long getLastOffset(SimpleConsumer consumer, String topic, int partition,
            long whichTime, String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(),clientName);
		OffsetResponse response = consumer.getOffsetsBefore(request);
		
		if (response.hasError()) {
			System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition) );
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	}
	
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
}
