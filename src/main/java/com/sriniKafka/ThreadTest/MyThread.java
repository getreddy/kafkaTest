package com.sriniKafka.ThreadTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;


public class MyThread {
	
	
	class MyCallable implements Callable<String>{

		String m_str;
		
		public MyCallable(String str){
			m_str = str;
		}
		
		public String call() throws Exception {
			// TODO Auto-generated method stub
			System.out.println("Thread ID: " +Thread.currentThread().getId() +" And Value: " +m_str);
			return m_str;
		}
		
	};
	
	public void testMyThread() throws InterruptedException, ExecutionException{
		ExecutorService executor = Executors.newFixedThreadPool(3);
		
		String sText = "Srini";
		
		List<Future<String>> myFutureList = new ArrayList<Future<String>>();
		
		for(int i = 0; i <= 5 ; i++){
			String sThText = sText + ":" +i;
			MyCallable myRun = new MyCallable(sThText);
			
			Future<String> retTh = executor.submit(myRun);
			myFutureList.add(retTh);
		}
		
		// show thread execution values
		for(Future<String> retObj : myFutureList){
			String sReturnObj = retObj.get();
			//System.out.println("Thread value: " +sReturnObj);
		}
		
	}
	
	
	class MyGoogService extends AbstractExecutionThreadService{

		protected void startUp() throws Exception{
			
			System.out.println("Startup method...");
			
		}
		
		@Override
		protected void run() throws Exception {
			
			System.out.println("Srini's google service running..");
			while(true && isRunning()){
				System.out.println("AbstractExecutionThreadService working..");
				Thread.sleep(5000);
			}
		}
		
		protected void shutDown() throws Exception{
			System.out.println("Shutdown method...");
			//stop();
		}
		
		/*protected void triggerShutdown(){
			System.out.println("triggerShutdown method...");
			stop();
		}*/
		
	}
	
	class MyGoogServiceIdle extends AbstractIdleService{

		@Override
		protected void startUp() throws Exception {
			//Thread.sleep(5000);
			System.out.println("AbstractIdleService startup called...");
			
			
		}

		@Override
		protected void shutDown() throws Exception {
			System.out.println("AbstractIdleService shutDown called...");
			
		}
		
	}
	
	class MyGoogAbstractService extends AbstractService implements Callable<ListenableFuture<State> >{

		LogReader logObj = new LogReader();
		
		@Override
		protected void doStart() {
			notifyStarted();
			System.out.println("MyGoogAbstractService doStart...");
			
			/*try {
				fileScan();
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
			System.out.println("File Scanning done...");
			//this.doStop();*/
			
			while(true){
				System.out.println("Working..");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		public int  fileScan() throws InterruptedException, IOException{
			String fileName = "/home/srini/testData/fileScan.log";
			return logObj.startReading(fileName);
			
		}

		@Override
		protected void doStop() {
			notifyStopped();
			logObj.stopReading();
			System.out.println("MyGoogAbstractService doStop...");
			
		}

		public ListenableFuture<State>  call() throws Exception {
			// TODO Auto-generated method stub
			ListenableFuture<State> thState =  this.start();
			
			//Thread.sleep(2000);
			
			//this.doStop();
			
			return thState;
			
		}
		
	}
	
	
	public void testGoogleService(ExecutorService executor) throws InterruptedException, ExecutionException{
		
		//Service service = new MyGoogService();
		//Service service = new MyGoogAbstractService();
		/*State st = service.startAndWait();
		if(st == Service.State.RUNNING){
			System.out.println("Its running..");
		}*/
		
		//service.start();
		//MyGoogAbstractService absServer = new MyGoogAbstractService();
		MyGoogService absServer = new MyGoogService();
		absServer.start();
		Thread.sleep(5000);
		absServer.stop();
		//Future<ListenableFuture<State> > retState = executor.submit(absServer);
		/*ListenableFuture<State> serState = retState.get();
		
		if(serState.get() == Service.State.RUNNING){
			System.out.println("Serice thread running....");
		}else if(serState.get() == Service.State.STOPPING){
			System.out.println("Serice thread stopped......");
		}*/
		//absServer.stop();
		//executor.shutdown();
		
		System.out.println("Done with calling..");
	}
	
	
	
	class LogReader {
		
		TailerListener listener = null;
		Tailer tailer = null;
		ExecutorService pool = null;
		
		public LogReader(){
			pool = Executors.newFixedThreadPool(1);
		}
		
		class MyTailerListener extends TailerListenerAdapter {
			public void handle(String line) {
				processLine(line);
			}
		}
		
	    public int startReading(String filename) throws InterruptedException, IOException {
	    	
			int delay = 1;
			 
			listener = new MyTailerListener();
			tailer = new Tailer(new File(filename), listener, delay);
	 
			//ExecutorService pool = Executors.newFixedThreadPool(1);
			pool.submit(tailer);
			//System.out.println("waiting .....");
			//pool.awaitTermination(4, TimeUnit.SECONDS);
			System.out.println("waiting done.......");
			//tailer.stop();
			//pool.shutdown();
				/*Thread th = new Thread(tailer);
				th.start();
				Thread.sleep(5000);
				tailer.stop();*/
			
			//tailer.run();
			
			return 1;
			
			
	    }
	    
	    public void stopReading(){
	    	tailer.stop();
	    	pool.shutdown();
	    }

	    private void processLine(String s) {
	        //processing line
	    	System.out.println("Line: ==> " +s);
	    }
	}	
	
	
	
	public static void main(String args[]){
		
		MyThread obj = new MyThread();
		
		try {
			//obj.testMyThread();
			obj.testGoogleService(null);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try {
			ExecutorService executor = Executors.newFixedThreadPool(5);
			obj.testGoogleService(executor);
			
			executor.awaitTermination(2, TimeUnit.SECONDS);
			executor.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		System.out.println("Main Thread Done...");
	}

}
