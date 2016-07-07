package main.java.org.apache.airavata.cloud.exceptions.marathonExceptions;

public class MarathonException extends Exception {
    private String exceptionMsg;
    public MarathonException(){
    	exceptionMsg="";
    }
    public MarathonException(String exceptionMsgIn){
    	exceptionMsg=exceptionMsgIn;;
    }

    public String toString(){
    	return 	this.exceptionMsg;
    }
 }
