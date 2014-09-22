package com.sriniKafka.app;

import java.io.Serializable;

public class MsgClass implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6206674825837395166L;
	
	String sMessage = "";
	
	public void setMessage(String sMsg){
		sMessage = sMsg;
	}
	
	public String getMessage(){
		return sMessage;
	}
	
}
