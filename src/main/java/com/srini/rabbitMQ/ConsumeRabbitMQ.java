package com.srini.rabbitMQ;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.QueueingConsumer;

public class ConsumeRabbitMQ {

	private final static String QUEUE_NAME = "hello";

	void resetTravelRoute(BasicProperties properties) {
	    properties.getHeaders().remove("x-death");
	}
	
	public void consumeMe() throws java.io.IOException,
    java.lang.InterruptedException{
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    
	    channel.queueBind("CWQ1", "CWExch1", "id1.initial.task2");
	    
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume("CWQ1", false, consumer);
	    
	    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	    String message = new String(delivery.getBody());
	    long deliverTag = delivery.getEnvelope().getDeliveryTag();
	    channel.basicAck(deliverTag, false);
	    
	   /* GetResponse response1 = channel.basicGet(
	            "prog.srini.queue", false);
	    if (response1 == null) {
	    	System.out.println("Null Returned...");
	        return;
	    }
	    
	    
	    GetResponse response2 = channel.basicGet(
	            "prog.srini.queue", false);
	    if (response2 == null) {
	    	System.out.println("Null Returned...");
	        return;
	    }
	    
	    channel.basicAck(
		        response1.getEnvelope().getDeliveryTag(), false);
	    
	    channel.basicAck(
		        response2.getEnvelope().getDeliveryTag(), false);*/
	    
	    
	    
	   /* channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QUEUE_NAME, true, consumer);

	    while (true) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      String message = new String(delivery.getBody());
	      System.out.println(" [x] Received '" + message + "'");
	    }*/
	    
	   /* GetResponse response = channel.basicGet(
	            "prog.srini.discard.queue", false);
	    if (response == null) {
	    	System.out.println("Null Returned...");
	        return;
	    }
	    System.out.println("Got the message and trying to queue it back..");

	    BasicProperties properties = response.getProps();
	    String originQueue = extractOriginQueue(properties);
	    
	    // Reset the message travel route
	    resetTravelRoute(properties);
	    System.out.println("OriginQueue : " +originQueue);
	    Channel publishingChannel = connection.createChannel();
	    // Publish the message to the origin queue
	    //publishingChannel.txSelect();
	    publishingChannel.basicPublish("", originQueue, 
	        properties, response.getBody());
	    publishingChannel.txRollback();
	    publishingChannel.txCommit();
	    
	    
	    
	 // Acknowledge the message in the dead letter queue
	    channel.basicAck(
	        response.getEnvelope().getDeliveryTag(), false);
	    
	    publishingChannel.txRollback();
	    System.out.println("Published..");*/
	}
	
	String extractOriginQueue(BasicProperties properties) {
	    ArrayList<HashMap> travelRoute = (ArrayList<HashMap>)
	          properties.getHeaders().get("x-death");

	    LongString queueString = (LongString)travelRoute
	          .get(travelRoute.size()-1).get("queue");
	    return new String(queueString.getBytes(), 
	           Charset.forName("UTF-8"));
	}
	
	  public static void main(String[] argv)
	      throws java.io.IOException,
	             java.lang.InterruptedException {
		  	
		  ConsumeRabbitMQ obj = new ConsumeRabbitMQ();
		  obj.consumeMe();
		    
		    
	    }	
	  
	  	  
	
}
