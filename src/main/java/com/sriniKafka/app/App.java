package com.sriniKafka.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	int partition = 0;
    	int numOfPartitions = 2;
    	
    	
    	String IP1 = "192.168.125.156";
    	String IP2 = "192.168.125.158";
    	String IP3 = "192.168.125.160";
    	
    	int offset = IP1.lastIndexOf('.');
    	partition = Integer.parseInt( IP2.substring(offset+1)) % numOfPartitions;
    	
    	
    	String path = "//home//srini//kafkaProg//msgFile.txt";
		File fObj = new File(path);
		
		BufferedReader bf = new BufferedReader(new FileReader(fObj));
		StringBuffer sbf = new StringBuffer();
		
		for (String x = bf.readLine(); x != null; x = bf.readLine()){
			
			//System.out.println("Line..: " +x);
			sbf.append(x);
		}
    	
		String msgText = sbf.toString();
    	System.out.println("Test msg size: " +msgText.length());
    	bf.close();
    	
    	System.out.println("Done.." + partition);
        
    }
}
