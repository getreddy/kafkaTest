package com.sriniKafka.app;

public class SriniKafkaConsumer {

	
	public static void main(String args[]){
		
		String topicName = "mytopic";
		int partitionNum = 0;
		int numOfMessages =  3;
		
		if(args != null && args.length > 0){
			topicName = args[0];
			partitionNum = Integer.parseInt(args[1]);
			numOfMessages = Integer.parseInt(args[2]);
		}
		
		SriniKafkaConsumerTh thObj = new SriniKafkaConsumerTh(topicName, partitionNum, numOfMessages);
		thObj.start();
		
		/*SriniKafkaConsumerTh thObj1 = new SriniKafkaConsumerTh(topicName, partitionNum, numOfMessages);
		thObj1.start();
		
		SriniKafkaConsumerTh thObj2 = new SriniKafkaConsumerTh(topicName, partitionNum, numOfMessages);
		thObj2.start();*/
		
		System.out.println("Srini Consumer done...");
	}
	
}
