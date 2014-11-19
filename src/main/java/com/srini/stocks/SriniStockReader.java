package com.srini.stocks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service.State;



public class SriniStockReader {

	
	
	/**
	 * 	<dependency>
     <groupId>com.bloomberglp.blpapi</groupId>
     <artifactId>bloombergapi</artifactId>
     <scope>system</scope>
     <version>0.1</version>
     <systemPath>/home/srini/blpapi_java_3.7.1.1/bin/blpapi-3.7.1-1.jar</systemPath>
   </dependency>
   */
	
	
	/*class StockReaderTh implements Runnable{
		
		
		boolean stopQuote = true;
		long quoteInterval = 5000;
		
		public StockReaderTh(){
			
		}
		
		public void startQuoteReading(){
			try {
				System.out.println("Start Quote Thread started");
				while(stopQuote){
					URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT+YHOO&f=nabvp2err2d1t8");
					URLConnection con = url.openConnection();
					InputStream in = con.getInputStream();
					
					BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
			        String line = null;
			        
			         //read file line by line
			        while( (line = bReader.readLine()) != null){
			        	System.out.println(line);
			        }
			        in.close();
			        System.out.println("===Update after " + quoteInterval/1000 + " secs..");
			        Thread.sleep(quoteInterval);
					
				}
				System.out.println("Start Quote Thread Stop Stopped");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		public void stopQuoteReading(){
			stopQuote = false;
			System.out.println("Start Quote Thread Stop requested");
		}

		public void run() {
			// TODO Auto-generated method stub
			startQuoteReading();
			
		}
	}*/
	
	
	class StockQuoteService extends AbstractService implements Callable<ListenableFuture<State> >{


		ExecutorService pool = null;
		boolean stopQuote = true;
		long quoteInterval = 5000;
		//StockReaderTh obj = new StockReaderTh();
		
		@Override
		protected void doStart() {
			
			notifyStarted();
			System.out.println("Stock quote doStart...");
			
			
			try {
				System.out.println("Start Quote Thread started");
				while(stopQuote){
					URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT+YHOO&f=nabvp2err2d1t8");
					URLConnection con = url.openConnection();
					InputStream in = con.getInputStream();
					
					BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
			        String line = null;
			        
			         //read file line by line
			        while( (line = bReader.readLine()) != null){
			        	System.out.println(line);
			        }
			        in.close();
			        System.out.println("===Update after " + quoteInterval/1000 + " secs..");
			        Thread.sleep(quoteInterval);
					
				}
				System.out.println("Start Quote Thread Stop Stopped");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			//pool = Executors.newFixedThreadPool(1);
			//pool.execute(obj);
			
			//this.doStop();
		}
		
		

		@Override
		protected void doStop() {
			notifyStopped();
			//obj.stopQuoteReading();
			//pool.shutdown();
			stopQuote = false;
			System.out.println("Start Quote Thread Stop requested");
			
		}

		public ListenableFuture<State>  call() throws Exception {
			// TODO Auto-generated method stub
			ListenableFuture<State> thState =  this.start();
			
			return thState;
			
		}
		
	}	
	
	
	class StockService extends AbstractExecutionThreadService{

		protected void startUp() throws Exception{
			
			System.out.println("Startup method...");
		}
		
		@Override
		protected void run() throws Exception {
			long quoteInterval = 5000;
			System.out.println("Srini's Stock trading service running..");
			while (isRunning()) {
				URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT+YHOO&f=nabvp2err2d1t8");
				URLConnection con = url.openConnection();
				InputStream in = con.getInputStream();
				
				BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
		        String line = null;
		        
		         //read file line by line
		        while( (line = bReader.readLine()) != null){
		        	System.out.println(line);
		        }
		        in.close();
		        System.out.println("===Update after " + quoteInterval/1000 + " secs..");
		        Thread.sleep(quoteInterval);
			}
			
		}
		
		protected void shutDown() throws Exception{
			System.out.println("Shutdown method...");
			
		}
		
		
	}	
	
	public void testAbsExeTh() throws Exception{
		StockService ser = new StockService();
		//ser.startUp();
		System.out.println("Starting service....");
		ser.start();
		
		System.out.println("Sleeping...");
		Thread.sleep(20000);
		ser.stop();
		System.out.println("Done with testAbsExeTh");
	}
	
	
	public void testQuoteService() throws InterruptedException{
		StockQuoteService absServer = new StockQuoteService();
		//absServer.start();
		ExecutorService pool = null;
		pool = Executors.newFixedThreadPool(1);
		pool.submit(absServer);
		Thread.sleep(10000);
		absServer.doStop();
	}
	
	
	
	class MyGoogServiceIdle extends AbstractIdleService{

		@Override
		protected void startUp() throws Exception {
			//Thread.sleep(5000);
			while(true){
				System.out.println("Thread started...");
				Thread.sleep(5000);
				break;
			}
			System.out.println("AbstractIdleService startup called...");
			
			
		}

		@Override
		protected void shutDown() throws Exception {
			System.out.println("AbstractIdleService shutDown called...");
			
		}
		
	}	
	
	public void testIdleService() {
		
		MyGoogServiceIdle obj = new MyGoogServiceIdle();
		obj.start();
		System.out.println("After start");
		obj.stop();
	}
	
	
	public static void main( String[] args ) throws Exception
    {
		   
		//URL url = new URL("http://finance.google.com/finance/info?client=ig&q=NASDAQ:GOOG");
		/*URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT+YHOO&f=nabvp2err2d1t8");
		URLConnection con = url.openConnection();
		InputStream in = con.getInputStream();*/
		
		/*String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		System.out.println(body);*/	
		
		 /*BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
         String line = null;
        
         //read file line by line
         while( (line = bReader.readLine()) != null){
        	 System.out.println(line);
         }*/
		
		SriniStockReader obj = new SriniStockReader();
		//obj.testQuoteService();
		//obj.testAbsExeTh();
		obj.testIdleService();
         
        System.out.println( "Srini stocks world.." );
    }	
	
}
