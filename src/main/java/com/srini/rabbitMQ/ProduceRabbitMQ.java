package com.srini.rabbitMQ;

import java.util.Map;
import java.util.HashMap;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

public class ProduceRabbitMQ {

	
	private final static String QUEUE_NAME = "prog.srini.queue";
	private final static String QUEUE__DISCARD_NAME = "prog.srini.discard.queue";
	private static final String EXCHANGE_NAME = "prog.srini";
	private static final String EXCHANGE_DISCARD_NAME = "prog.srini.discard";

	public static void main(String[] argv)
	      throws java.io.IOException {
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();	
	    
	    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
	    channel.exchangeDeclare(EXCHANGE_DISCARD_NAME, "topic");
	    Map<String, Object> args = new HashMap<String, Object>();
	    args.put("x-dead-letter-exchange", EXCHANGE_DISCARD_NAME);
	    
	    channel.queueDeclare(QUEUE_NAME, true, false, false, args);
	    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "prog.key");
	    
	    
	    channel.queueDeclare(QUEUE__DISCARD_NAME, true, false, false, null);
	    channel.queueBind(QUEUE__DISCARD_NAME, EXCHANGE_DISCARD_NAME, "prog.key");
	    
	    
	    String message = "Hello World Srini!";
	    BasicProperties.Builder props = new BasicProperties().builder();
	    props.expiration("20000");
	    channel.basicPublish(EXCHANGE_NAME, "prog.key", false, props.build(), message.getBytes());
	    
	    
	    //channel.exchangeDeclare("SriniExchange", "direct");
	    /*Map<String, Object> args = new HashMap<String, Object>();
	    args.put("x-dead-letter-exchange", "SQue1Dead");	    
	    
	    channel.queueDeclare(QUEUE_NAME, false, false, false, args);
	    String message = "Hello World Srini!";
	    BasicProperties.Builder props = new BasicProperties().builder();
	    props.expiration("20000");
	    channel.basicPublish("", QUEUE_NAME, props.build(), message.getBytes());
	    System.out.println(" [x] Sent '" + message + "'");
	    */
	    channel.close();
	    connection.close();
	}
	  
}
