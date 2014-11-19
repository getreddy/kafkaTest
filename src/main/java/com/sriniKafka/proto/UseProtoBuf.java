package com.sriniKafka.proto;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sriniKafka.proto.BufRecordProtos.BufRecord;

public class UseProtoBuf {

	public static void main(String args[]){
		
		BufRecord Srini= BufRecordProtos.BufRecord.newBuilder()
	            .setAge(38)
	            .setName("Srini") 
	            .build();
		
		try {
		       // write
		        FileOutputStream output = new FileOutputStream("cat.ser");
		        Srini.writeTo(output);
		        output.close();
		 
		       // read
		        BufRecord sriniFromFile = BufRecord.parseFrom(new FileInputStream("cat.ser"));
		        System.out.println(sriniFromFile);
		 
		    } catch (IOException e) {
		        e.printStackTrace();
		}		
		
		System.out.println("done..");
		
	}
	
}
