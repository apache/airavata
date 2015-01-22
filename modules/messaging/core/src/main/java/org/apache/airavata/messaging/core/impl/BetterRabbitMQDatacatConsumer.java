package org.apache.airavata.messaging.core.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.messaging.event.ExperimentOutputParsedEvent;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.log4j.LogManager;
import org.apache.thrift.TBase;

public class BetterRabbitMQDatacatConsumer {
    private final org.apache.log4j.Logger logger = LogManager.getLogger(BetterRabbitMQDatacatConsumer.class);

    private String BINDING_KEY;
    private String RABBITMQ_HOST;
    private String EXCHANGE_NAME;
    private boolean runFileUpdateListener = false;

    public BetterRabbitMQDatacatConsumer() {
        RABBITMQ_HOST = "localhost";
        EXCHANGE_NAME = "datacat";
        runFileUpdateListener = true;
    }

    public void startBroker() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost(RABBITMQ_HOST);

                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();

                    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
                    String queueName = channel.queueDeclare().getQueue();

                    channel.basicQos(1);
                    channel.queueBind(queueName, EXCHANGE_NAME, "*");

                    logger.debug("Waiting for messages. To exit press CTRL+C");

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(queueName, true, consumer);

                    while (runFileUpdateListener) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                        Message message = new Message();
                        ThriftUtils.createThriftFromBytes(delivery.getBody(), message);
                        TBase event = null;

                        if (message.getMessageType().equals(MessageType.OUTPUT_PARSED)) {

                            ExperimentOutputParsedEvent experimentOutputParsedEvent = new ExperimentOutputParsedEvent();
                            ThriftUtils.createThriftFromBytes(message.getEvent(), experimentOutputParsedEvent);

                            logger.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType() + "'  with filename " +
                                    experimentOutputParsedEvent.getDocumentID());

                            event = experimentOutputParsedEvent;

                            logger.debug(" [x] Received FileInfo Message'");
                            process(experimentOutputParsedEvent, message.getUpdatedTime());
                            logger.debug(" [x] Done Processing FileInfo Message");
                        } else {
                            logger.debug("Recieved message of type ..." +message.getMessageType());
                        }
                        //FIXME Debug the basicAck
                        //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }


        })).start();


    }

    private void process(ExperimentOutputParsedEvent experimentOutputParsedEvent, long updatedTime) {
        logger.info("Processing the event!!!");
        logger.info(experimentOutputParsedEvent.getExperimentId()+" ----- "+ experimentOutputParsedEvent.getDocumentID());
    }

    public void stopBroker() {
        runFileUpdateListener = false;
        logger.info("Shutting down FileUpdateListener...");
    }



}
