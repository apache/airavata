package org.apache.airavata.allocation.manager.notification.receiver;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class NotificationSender {
	private final static String QUEUE_NAME = "notify";
	public static void main(String[] args) {

	    try {
	    	   
	        //Create a connection factory
	        ConnectionFactory factory = new ConnectionFactory();
	        //Set the host to the location of the RabbitMQ server
	        factory.setHost("localhost");
	        //Open a new connection
	        Connection connection = factory.newConnection();
	        //Channel is the abstraction for interacting with a queue
	        Channel channel = connection.createChannel();
	        //Create the Queue if it does not exist
	        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	        //assuming this is the request id send
	        String project_ID  = "1001" ;
	        
	      
	          channel.basicPublish("", QUEUE_NAME, null, project_ID.getBytes());
	     
	          System.out.println(
	             " [x] Sent the request");
	        
	       
	        //Close the channel
	        channel.close();
	        //Close the connection
	        connection.close();
	       
	      } catch (Exception e) {
	        //Dump any exception to the console
	        e.printStackTrace();
	      }
	 }

}
