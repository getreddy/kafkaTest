package com.sriniKafka.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.api.FetchRequestBuilder;
import kafka.javaapi.FetchResponse;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.consumer.TopicFilter;
import kafka.consumer.Whitelist;
import kafka.api.FetchRequest;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.message.MessageAndMetadata;
import kafka.message.MessageAndOffset;

import com.sriniKafka.app.KafkaCon.KafkaPartitionConsumer;

public class KafKaConWithFilter {

	
	
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
	
	public class KafkaConsumerThread implements Runnable {

		KafkaStream<byte[], byte[]> m_ks;
		
		public KafkaConsumerThread(KafkaStream<byte[], byte[]> ks){
			m_ks = ks;
		}
		
		public void run() {
			long tnum = Thread.currentThread().getId();
			System.out.println("Thread launched....: "+tnum);
			int exitCount = 0;
			for (MessageAndMetadata<byte[], byte[]> msgAndMetadata: m_ks) {
				exitCount ++;
            	byte[] msgsBytes = msgAndMetadata.message();
            	byte[] keyMsg = msgAndMetadata.key();
                //Do something with the bytes you just got off Kafka.
            	MsgClass msgObj = null;
            	String keyS = "";
        		try {
					msgObj = (MsgClass) toObject(msgsBytes);
					//if(keyMsg != null)
						//keyS = (String) toObject(keyMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	 
        		
        		
        		String msgConsumed = "Consumer Thread: " + tnum + ": " + "Message num: " +msgObj.getMessage();
        		System.out.println(msgConsumed);
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		boolean breakMe = false;
        		if(breakMe)
        			break;
        		//System.out.println("******* Key Message: " +keyS);
        		long ltime = kafka.api.OffsetRequest.LatestTime();
        		System.out.println("Some time: " +ltime);
        		
        		/*if(exitCount > 2){
        			System.out.println("I am done..");
        			return;
        		}*/
        		
            }			
			
		}
		
	}
	
	
	public class KafkaConsumerCallableThread implements Callable<KafkaStream<byte[], byte[]>> {

		KafkaStream<byte[], byte[]> m_ks;
		
		public KafkaConsumerCallableThread(KafkaStream<byte[], byte[]> ks){
			m_ks = ks;
		}
		
		public KafkaStream<byte[], byte[]> call() {
			long tnum = Thread.currentThread().getId();
			System.out.println("Thread launched....: "+tnum);
			int exitCount = 0;
			for (MessageAndMetadata<byte[], byte[]> msgAndMetadata: m_ks) {
				exitCount ++;
            	byte[] msgsBytes = msgAndMetadata.message();
            	byte[] keyMsg = msgAndMetadata.key();
                //Do something with the bytes you just got off Kafka.
            	MsgClass msgObj = null;
            	String keyS = "";
        		try {
					msgObj = (MsgClass) toObject(msgsBytes);
					//if(keyMsg != null)
						//keyS = (String) toObject(keyMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	 
        		
        		
        		String msgConsumed = "Consumer Thread: " + tnum + ": " + "Message num: " +msgObj.getMessage();
        		System.out.println(msgConsumed);
        		boolean breakMe = false;
        		if(breakMe)
        			break;
        		//System.out.println("******* Key Message: " +keyS);
        		long ltime = kafka.api.OffsetRequest.LatestTime();
        		System.out.println("Some time: " +ltime);
        		
        		/*if(exitCount > 2){
        			System.out.println("I am done..");
        			return m_ks;
        		}*/
        		
            }			
			
			return m_ks;
		}
		
	}	
	
	private void printMessages(ByteBufferMessageSet messageSet) throws UnsupportedEncodingException {
	    for(MessageAndOffset messageAndOffset: messageSet) {
	      ByteBuffer payload = messageAndOffset.message().payload();
	      byte[] bytes = new byte[payload.limit()];
	      payload.get(bytes);
	      System.out.println(new String(bytes, "UTF-8"));
	    }
	  }	
	
	public void testSimpleConsumerTest() throws Exception{
		
		
		SimpleConsumer simpleConsumer = new SimpleConsumer("192.168.125.156",
                9092,
                100000,
                64*1024,
                "SimpleConsumerDemoClient");
		FetchRequest req = new FetchRequestBuilder()
			        .clientId("SimpleConsumerDemoClient")
			        .addFetch("mytopic", 0, 0L, 100)
			        .build();
		
		FetchResponse fetchResponse = simpleConsumer.fetch(req);
		printMessages((ByteBufferMessageSet) fetchResponse.messageSet("mytopic", 0));
	}
	
	public void testConsumerFilter(String groupID) throws Exception{
		
		Properties props = new Properties();
        props.put("zookeeper.connect", "192.168.125.160:2181");
        //System.out.println("GroupID: " +args[1]);
        //String groupID = args[0];
        //props.put("group.id", "mygroupID1");
        props.put("group.id", groupID);
        props.put("zookeeper.session.timeout.ms", "413");
        props.put("zookeeper.sync.time.ms", "203");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest"); 
        
      
		
        ConsumerConfig cf = new ConsumerConfig(props) ;
        
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(cf) ;
        TopicFilter sourceTopicFilter = new Whitelist("mytopic"); 
        //String topic = "mytopic" ;
        
        List<KafkaStream<byte[], byte[]>> streams = consumer.createMessageStreamsByFilter(sourceTopicFilter, 2); 

        
        ExecutorService executor = Executors.newFixedThreadPool(streams.size());
        
        System.out.println("before the loop..");
        for(final KafkaStream<byte[], byte[]> stream: streams){
        	
        	//boolean isStreamEmpty = stream.isEmpty();
        	//System.out.println("Stream Empty..." +isStreamEmpty);
        	//int size = stream.size();
        	//System.out.println("Size of stream: " +size);
        	executor.submit(new  KafkaConsumerThread(stream));
        	//executor.submit(new KafkaConsumerCallableThread(stream));
        }
        
        
        
       /* for(final KafkaStream<byte[], byte[]> stream: streams){
            executor.submit(new Runnable() {
                public void run() {
                    for (MessageAndMetadata<byte[], byte[]> msgAndMetadata: stream) {
                    	try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                    	byte[] msgsBytes = msgAndMetadata.message();
                    	byte[] keyMsg = msgAndMetadata.key();
                        //Do something with the bytes you just got off Kafka.
                    	MsgClass msgObj = null;
                    	String keyS = "";
    	        		try {
    						msgObj = (MsgClass) toObject(msgsBytes);
    						//if(keyMsg != null)
    							//keyS = (String) toObject(keyMsg);
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					} catch (ClassNotFoundException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}	 
    	        		
    	        		long tnum = Thread.currentThread().getId();
    	        		String msgConsumed = "Consumer Thread: " + tnum + ": " + "Message num: " +msgObj.getMessage();
    	        		System.out.println(msgConsumed);
    	        		boolean breakMe = false;
    	        		if(breakMe)
    	        			break;
    	        		//System.out.println("******* Key Message: " +keyS);
    	        		long ltime = kafka.api.OffsetRequest.LatestTime();
    	        		System.out.println("Some time: " +ltime);
    	        		
                    }
                }
            });
        }	     */   
        
	}
	
		public static void main(String[] args) {
			
			KafKaConWithFilter obj = new KafKaConWithFilter();
			try {
				String mygroupID = "";
				if(args != null && args.length > 0)
					mygroupID = args[0];
				
				obj.testConsumerFilter(mygroupID);
				//obj.testSimpleConsumerTest();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        // consumer.shutdown(); 
		}	
	
}
