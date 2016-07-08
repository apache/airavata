package org.apache.airavata.monitoring.simulator;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.monitoring.Util;
import org.apache.airavata.monitoring.mailbox.GmailSMTPMailBox;
import org.apache.airavata.monitoring.mailbox.MailBox;
import org.apache.airavata.monitoring.producer.RabbitMQEmailPublisher;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class FetchPublish {
    private static final String EXCHANGE_NAME = "monitor";

    public static void fetchEmailAndPublish() throws MessagingException, KeyManagementException, NoSuchAlgorithmException,
            IOException, TimeoutException, URISyntaxException, ApplicationSettingsException {
        RabbitMQEmailPublisher publisher = getRabbitMQEmailPublisher();
        MailBox gmailSmtpMailBox = new GmailSMTPMailBox(Util.getMailConfig());
        Message[] messages = gmailSmtpMailBox.getUnreadMessages();
        publisher.publishMessages(messages);
        publisher.shutdown();
    }


    private static RabbitMQEmailPublisher getRabbitMQEmailPublisher() throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        Properties brokerProps = Util.getBrokerProperties();
        String exchangeName = brokerProps.getProperty("monitor.email.exchange.name");
        String brokerURI = brokerProps.getProperty("monitor.email.broker.URI");
        String[] queueNames = new String[]{brokerProps.getProperty("monitor.email.broker.queue1.name"), brokerProps.getProperty("monitor.email.broker.queue2.name")};
        RabbitMQEmailPublisher publisher = new RabbitMQEmailPublisher(exchangeName, brokerURI, queueNames);
        return publisher;
    }


}
