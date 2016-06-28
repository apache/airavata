package org.apache.airavata.cloud.exceptions.auroraExceptions;

public class AuroraException extends Exception {
    private String exceptionMsg;
    public AuroraException(){
    	exceptionMsg="";
    }
    public AuroraException(String exceptionMsgIn){
    	exceptionMsg=exceptionMsgIn;;
    }

    public String toString(){
    	return 	this.exceptionMsg;
    }
 }
