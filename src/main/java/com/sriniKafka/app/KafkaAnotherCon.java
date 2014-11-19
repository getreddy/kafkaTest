package com.sriniKafka.app;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import com.sriniKafka.app.KafkaCon.KafkaPartitionConsumer;

public class KafkaAnotherCon {

	public static class KafkaPartitionConsumer implements Runnable {

		private int tnum ;
		private KafkaStream kfs ;
		
		public KafkaPartitionConsumer(int id, KafkaStream ks) {
			tnum = id ;
			kfs = ks ;
			
		}
		
		
		
		public void writeFile(String msg) throws IOException{
			
			File file = new File("//home//srini//kafkaProg//consumeMe2.txt");
			 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}			
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(msg);
			bw.write("\n");
			bw.close();			
		}
		
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("This is thread " + tnum) ;
			
			ConsumerIterator<byte[], byte[]> it = kfs.iterator();
				int i = 1 ;
	        	while (it.hasNext()) {
	        		byte[] msgsBytes = it.next().message();
	        		MsgClass msgObj = null;
	        		try {
						msgObj = (MsgClass) toObject(msgsBytes);
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
						writeFile(msgConsumed);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		++i ;
	        	}
			
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
	
	
	public static void main(String[] args) {
		
		Properties props = new Properties();
        props.put("zookeeper.connect", "192.168.125.160:2181");
        //System.out.println("GroupID: " +args[1]);
        //String groupID = args[0];
        props.put("group.id", "mygroupID2");
        props.put("zookeeper.session.timeout.ms", "413");
        props.put("zookeeper.sync.time.ms", "203");
        props.put("auto.commit.interval.ms", "1000");
        // props.put("auto.offset.reset", "smallest"); 
        
        
		
        ConsumerConfig cf = new ConsumerConfig(props) ;
        
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(cf) ;
        
        //String topic = "mytopic" ;
        String topic = "myFileMsgs" ;
        
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(3));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
        
        System.out.println("Consumer topic streams: " +streams.size());
	
        ExecutorService executor = Executors.newFixedThreadPool(3); ;
        
        int threadnum = 0 ;
        
        for(KafkaStream<byte[],byte[]> stream  : streams) {
        	
        	executor.execute(new KafkaPartitionConsumer(threadnum,stream));
        	++threadnum ;
        }
        
        
        
        
        // consumer.shutdown(); 
	}	
}
