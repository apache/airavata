package org.apache.airavata.allocation.manager.notification.receiver;

import org.apache.airavata.allocation.manager.notification.authenticator.server.NotificationRequestDetail;
import org.apache.airavata.allocation.manager.notification.sender.MailNotification;
import org.apache.thrift.transport.TServerSocket;
import com.rabbitmq.client.*;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.io.IOException;

public class NotificationReceiver {
	public static void StartsimpleServer() {
		try {

			// Create a connection factory
			ConnectionFactory factory = new ConnectionFactory();
			// Set the host to the location of the RabbitMQ server
			factory.setHost("localHost");
			// Open a new connection
			Connection connection = factory.newConnection();

			Channel channel = connection.createChannel();

			channel.queueDeclare("notify", false, false, false, null);
			// Comfort logging
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String requestID = new String(body, "UTF-8");
					(new MailNotification()).sendMail(requestID, (new NotificationRequestDetail()).getStatus(requestID),
							(new NotificationRequestDetail()).getReceiverList(requestID));
				}
			};
			channel.basicConsume("notify", true, consumer);

		} catch (Exception e) {
			// Dump any error to the console
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		StartsimpleServer();
	}
}
