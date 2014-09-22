package com.sriniKafka.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.mail.SendFailedException;

import org.apache.zookeeper.server.util.SerializeUtils;

import kafka.message.Message;
import kafka.producer.KeyedMessage;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

public class KafkaProd {

	
	
	public static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }	
	
	
	public void sendSampleMsgs(Producer producer) throws IOException{
		String date = "04092014" ;
		String topic = "mytopic" ;
		MsgClass msgObj = null;
		
		for (int i = 1 ; i <= 5 ; i++) {
			
			String msg = date + " This is message " + i ;
			System.out.println(msg) ;
			msgObj = new MsgClass();
			msgObj.setMessage(msg);
			
			byte[] msgBytes = KafkaProd.toByteArray(msgObj);
			
			KeyedMessage<String, byte[]> data = new KeyedMessage<String, byte[]>(topic,  msgBytes);
			 
			producer.send(data);
			
		}
		
		producer.close();
	}
	
	public void sendFileLogs(Producer producer) throws IOException{
		
		String path = "//home//srini//sData//fileTest.txt";
		String topic = "myFileMsgs" ;
		
		File fObj = new File(path);
		MsgClass msgObj = null;
		
		BufferedReader bf = new BufferedReader(new FileReader(fObj));
		
		for (String x = bf.readLine(); x != null; x = bf.readLine()){
			
			System.out.println("Line..: " +x);
			msgObj = new MsgClass();
			msgObj.setMessage(x);
			
			byte[] msgBytes = KafkaProd.toByteArray(msgObj);
			KeyedMessage<String, byte[]> data = new KeyedMessage<String, byte[]>(topic,  msgBytes);
			producer.send(data);
		}
		bf.close();	
		producer.close();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		KafkaProd prodObj = new KafkaProd();
		
		Properties props = new Properties();
		 
		props.put("metadata.broker.list", "192.168.125.156:9092");
		//props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("serializer.class", "kafka.serializer.DefaultEncoder");
		// props.put("partitioner.class", "example.producer.SimplePartitioner");
		props.put("request.required.acks", "1");
		 
		ProducerConfig config = new ProducerConfig(props);
		
		Producer<String, byte[]> producer = new Producer<String, byte[]>(config);
		
		prodObj.sendFileLogs(producer);
		
	}
	
}
