package com.sriniKafka.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class StockService extends AbstractExecutionThreadService{

	Producer m_prod = null;
	String m_hostIP = null;
	int m_maxMsgsPerThread;
	int m_batchOffset;
	
	public StockService(Producer prod, String hostIP, int maxMsgsPerThread, int batchOffset){
		m_prod = prod;
		m_hostIP = hostIP;
		m_maxMsgsPerThread = maxMsgsPerThread;
		m_batchOffset = batchOffset;
	}
	
	protected void startUp() throws Exception{
		
		System.out.println("Startup method...");
	}
	
	@Override
	protected void run() throws Exception {
		long quoteInterval = 0;
		MsgClass msgObj = null;
		String topic = "mytopic" ;
		System.out.println("Srini's Stock trading service running.. Num Of messages: " +m_maxMsgsPerThread);
		int cnt = m_maxMsgsPerThread;
		long thID = Thread.currentThread().getId();
		//while (isRunning()) {
		while(cnt != 0){
			if(cnt <= 0)
				break;
			//URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT+YHOO&f=nabvp2err2d1t8");
			URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL&f=nabvp2err2d1t8");
			URLConnection con = url.openConnection();
			InputStream in = con.getInputStream();
			
			BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
	        String line = null;
	        
	        // this is required for partition...
	        /*Random rnd = new Random();
			String fileName = m_hostIP;//"192.168.2." + rnd.nextInt(255);*/
	        
	        String partitionNum = Integer.toString(cnt);
			
			
	         //read file line by line
	        int batchOffset = m_batchOffset;
	        while( (line = bReader.readLine()) != null){
	        	batchOffset = batchOffset + cnt;
	        	
	        	String msg = line;
	        	
	        	msg  = "THID: " +thID + " MSGID: " +batchOffset + " : " + msg;
	        	System.out.println(msg);
	        	msgObj = new MsgClass();
				msgObj.setMessage(msg);
				
				byte[] msgBytes = KafkaProd.toByteArray(msgObj);
				
				KeyedMessage<String, byte[]> data = new KeyedMessage<String, byte[]>(topic, partitionNum,  msgBytes);
				 
				m_prod.send(data);		        	
	        }
	        cnt--;
	        in.close();
	        bReader.close();
	        //System.out.println("===Update after " + quoteInterval/1000 + " secs..");
	        //Thread.sleep(quoteInterval);
		}
		
	}
	
	protected void shutDown() throws Exception{
		System.out.println("Shutdown method...");
	}
}