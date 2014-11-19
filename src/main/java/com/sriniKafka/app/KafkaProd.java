package com.sriniKafka.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;


import kafka.producer.KeyedMessage;
import kafka.producer.async.ProducerSendThread;
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
	
	
	class ExecuteStockUpdates implements Callable<Integer>{

		Producer m_producer;
		String m_hostIP;
		int m_maxMsgsPerThread;
		int m_batchOffset;
		
		public ExecuteStockUpdates(Producer producer, String hostIP, int maxMsgsPerThread, int batchOffset){
			m_producer = producer;
			m_hostIP = hostIP;
			m_maxMsgsPerThread = maxMsgsPerThread;
			m_batchOffset = batchOffset;
		}
		
		public String getText() throws Exception{
			
			String path = "//home//srini//kafkaProg//msgFile.txt";
			File fObj = new File(path);
			
			BufferedReader bf = null;
			String msgText = "";
			try{
				bf = new BufferedReader(new FileReader(fObj));
				StringBuffer sbf = new StringBuffer();
				
				for (String x = bf.readLine(); x != null; x = bf.readLine()){
					
					//System.out.println("Line..: " +x);
					sbf.append(x);
				}
		    	
				msgText = sbf.toString();
		    	//System.out.println("Test msg size: " +msgText.length());
			}finally{
				bf.close();
			}
	    	
	    	return msgText;
			
		}
		
		
		public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

		public String now() {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			return sdf.format(cal.getTime());
		}
		
		public Integer call() throws Exception {
			
			/*StockService ser = new StockService(m_producer, m_hostIP, m_maxMsgsPerThread, m_batchOffset);
			//ser.startUp();
			System.out.println("Starting service....");
			//ser.start();
			ser.startAndWait();
			
			//System.out.println("Sleeping...");
			//Thread.sleep(50000);
			ser.stop();	
			
			return 0;*/
			
			
			long quoteInterval = 0;
			MsgClass msgObj = null;
			String topic = "mytopic" ;
			System.out.println("Srini's Stock trading service running.. Num Of messages: " +m_maxMsgsPerThread);
			int cnt = m_maxMsgsPerThread;
			long thID = Thread.currentThread().getId();
			//while (isRunning()) {
			while(true){
				if(cnt <= 0)
					break;
				//URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT+YHOO&f=nabvp2err2d1t8");
				/*URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL&f=nabvp2err2d1t8");
				URLConnection con = url.openConnection();
				InputStream in = con.getInputStream();
				
				BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
		        String line = null;*/
		        
		        // this is required for partition...
		        /*Random rnd = new Random();
				String fileName = m_hostIP;//"192.168.2." + rnd.nextInt(255);*/
				int batchOffset = m_batchOffset;
				batchOffset = batchOffset + cnt;
		        String partitionNum = Integer.toString(batchOffset);
				
		        int partitionTest = batchOffset % 2;
		         //read file line by line
		        //int batchOffset = m_batchOffset;
		        String line = "\"Apple Inc.\",98.14,98.12,68179688,\"+1.46%\",6.20,15.53,N/A,\"10/17/2014\",112.64\"";
		        //String line = getText();
		        // delete above line after performance test.. and play with below line for real time data...
		       // while( (line = bReader.readLine()) != null){
		        	//batchOffset = batchOffset + cnt;
		        	
		        	String msg = line;
		        	
		        	String nowDate = now();
		        	
		        	msg  = nowDate + " THID: " +thID + " PartitionNum: " +partitionTest + " MSGID: " +batchOffset + " : " + msg;
		        	
		        	String debugMsg = "THID: " +thID + " PartitionNum: " +partitionTest + " MSGID: " +batchOffset ;
		        	System.out.println(msg);
		        	msgObj = new MsgClass();
					msgObj.setMessage(msg);
					
					byte[] msgBytes = KafkaProd.toByteArray(msgObj);
					
					KeyedMessage<String, byte[]> data = new KeyedMessage<String, byte[]>(topic, partitionNum,  msgBytes);
					 
					m_producer.send(data);		        	
		        //}
		        cnt--;
		        //in.close();
		        //bReader.close();
		        //System.out.println("===Update after " + quoteInterval/1000 + " secs..");
		        //Thread.sleep(quoteInterval);
			}	
			
			return 0;
			
		}
		
	}
	
	
	public void sendStockUpdates(Producer producer, String hostIP) throws IOException, InterruptedException, ExecutionException{
		
		int numThs = 1;
		
		
		
		// Create num of threads to send messges to broker in async manner.
		ExecutorService exService = Executors.newFixedThreadPool(numThs);
		
		List<Future<Integer>> list = new ArrayList<Future<Integer>>();
		
		// Number of messages to be sent for each partition.
		int maxMsgsPerThread = 10;
		int batchOffset = 0;
		for(int i=0; i<numThs; i++){
			Callable<Integer> worker = new ExecuteStockUpdates(producer, hostIP, maxMsgsPerThread, batchOffset);
			batchOffset = batchOffset + maxMsgsPerThread;
			Future<Integer> submit = exService.submit(worker);
			list.add(submit);
		}
		
		/*StockService ser = new StockService(producer, hostIP);
		//ser.startUp();
		System.out.println("Starting service....");
		ser.start();
		
		System.out.println("Sleeping...");
		Thread.sleep(30000);
		ser.stop();	*/	
		for (Future<Integer> future : list) {
			int ret = future.get();
		}
		
		System.out.println("Done with all the threads...");
		exService.shutdown();
		
		
	}
	
	
	public void sendStockUpdatesUsingProdConfig(ProducerConfig config, String hostIP) throws IOException, InterruptedException, ExecutionException{
		
		//int numThs = 5;
		int numThs = 1;
		
		long startTime = System.currentTimeMillis();
		
		// Create num of threads to send messges to broker in async manner.
		ExecutorService exService = Executors.newFixedThreadPool(numThs);
		
		List<Future<Integer>> list = new ArrayList<Future<Integer>>();
		
		// Number of messages to be sent for each partition.
		//int maxMsgsPerThread = 10000;
		int maxMsgsPerThread = 10;
		int batchOffset = 0;
		List<Producer<String, byte[]>> prodList = new ArrayList<Producer<String, byte[]>>();
		
		for(int i=0; i<numThs; i++){
			Producer<String, byte[]> producer = new Producer<String, byte[]>(config);
			Callable<Integer> worker = new ExecuteStockUpdates(producer, hostIP, maxMsgsPerThread, batchOffset);
			batchOffset = batchOffset + maxMsgsPerThread;
			Future<Integer> submit = exService.submit(worker);
			list.add(submit);
			prodList.add(producer);
		}
		
		/*StockService ser = new StockService(producer, hostIP);
		//ser.startUp();
		System.out.println("Starting service....");
		ser.start();
		
		System.out.println("Sleeping...");
		Thread.sleep(30000);
		ser.stop();	*/	
		for (Future<Integer> future : list) {
			int ret = future.get();
		}
		
		for(Producer<String, byte[]> prod : prodList )
		{
			prod.close();
		}
		System.out.println("Done with all the threads...");
		exService.shutdown();
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time elapsed in secs: " + (endTime - startTime) / 1000);
	}	
	
	public void sendFileLogs(Producer producer) throws IOException{
		
		String path = "//home//srini//kafkaProg//logFile.txt";
		String topic = "myFileMsgs" ;
		
		File fObj = new File(path);
		MsgClass msgObj = null;
		Random rnd = new Random();
		
		String fileName = "192.168.2." + rnd.nextInt(255);
		
		BufferedReader bf = new BufferedReader(new FileReader(fObj));
		
		for (String x = bf.readLine(); x != null; x = bf.readLine()){
			
			System.out.println("Line..: " +x);
			msgObj = new MsgClass();
			msgObj.setMessage(x);
			
			byte[] msgBytes = KafkaProd.toByteArray(msgObj);
			KeyedMessage<String, byte[]> data = new KeyedMessage<String, byte[]>(topic, fileName, msgBytes);
			producer.send(data);
		}
		bf.close();	
		producer.close();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		KafkaProd prodObj = new KafkaProd();
		
		long startTime = System.currentTimeMillis();
		
		
		Properties props = new Properties();
		 
		props.put("metadata.broker.list", "192.168.125.156:9092,192.168.125.158:9092,192.168.125.160:9092");
		//props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("serializer.class", "kafka.serializer.DefaultEncoder");
		props.put("key.serializer.class", "kafka.serializer.StringEncoder");
		props.put("producer.type", "async");
		//props.put("producer.type", "sync");
		props.put("batch.num.messages", "1");
		props.put("partitioner.class", "com.sriniKafka.app.StringBasedPartitioner");
		props.put("request.required.acks", "1");
		 
		ProducerConfig config = new ProducerConfig(props);
		
		//Producer<String, byte[]> producer = new Producer<String, byte[]>(config);
		
		String hostIP = "127.0.0.1";
		//prodObj.sendFileLogs(producer);
		try {
			if(args != null && args.length > 0)
				hostIP = args[0];
			
			//prodObj.sendStockUpdates(producer, hostIP);
			//producer.close();

			prodObj.sendStockUpdatesUsingProdConfig(config, hostIP);
			
			System.out.println("I am done with Producing messages..");
			long endTime = System.currentTimeMillis();
			
			
			System.out.println("Time elapsed (produced) in secs: " + (endTime - startTime) / 1000);			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
