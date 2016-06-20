package org.apache.airavata.monitoring.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.airavata.monitoring.MessageExtract;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitMQEmailPublisher {
    private static final String EXCHANGE_TYPE = "fanout";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private String exchangeName;

    /**
     * RabbitMQ Publisher with auto-recovery enabled
     *
     * @param exchangeName Name of the exchange
     * @param brokerURL    Broker URL
     * @param queueNames   Name of the queues which needs to be declared and binded to
     *                     the exchange
     * @throws IOException
     * @throws TimeoutException
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    public RabbitMQEmailPublisher(String exchangeName, String brokerURL,
                                  String[] queueNames) throws IOException, TimeoutException,
            KeyManagementException, NoSuchAlgorithmException,
            URISyntaxException {
        this.exchangeName = exchangeName;
        // TODO get singleton instance of connection factory
        this.factory = new ConnectionFactory();
        this.factory.setUri(brokerURL);
        factory.setAutomaticRecoveryEnabled(true);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE);
        for (String queueName : queueNames) {
            // declare all the queues
            channel.queueDeclare(queueName, true, false, false, null)
                    .getQueue();
            // bind all the queues to exchange
            channel.queueBind(queueName, exchangeName, "");
        }
    }

    public void publish(byte[] message) throws IOException {
        channel.basicPublish(exchangeName, "", null, message);
        System.out.println("[*]Publisher: Message Sent to Exchange:" + exchangeName + "");
    }

    /**
     * Publish Message to exchange
     *
     * @param message Message to be published
     * @throws IOException
     * @throws MessagingException
     */
    public void publishMessage(Message message) throws IOException,
            MessagingException {
        MessageExtract msgExtract = new MessageExtract(message);
        publish(msgExtract.getSerializedBytes());
    }

    /**
     * Publish Messages to exchange
     *
     * @param messages Messages to be published
     * @throws IOException
     * @throws MessagingException
     */
    public void publishMessages(Message[] messages) throws IOException,
            MessagingException {
        for (Message message : messages) {
            publishMessage(message);
        }
    }

    public void shutdown() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

}
