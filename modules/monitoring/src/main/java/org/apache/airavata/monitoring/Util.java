package org.apache.airavata.monitoring;

import java.util.Properties;

public class Util {
    /**
     * Fetch SMTP Properties. Will be reworked to fetch the properties from properties file.
     *
     * @return SMTP Properties
     */
    public static Properties getSMTPProperties() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.port", "993");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.port", "993");
        props.setProperty("mail.userID", "test.airavata@gmail.com");
        props.setProperty("mail.password", "airavata");
        return props;
    }

    /**
     * Fetch Broker Properties. Will be reworked to fetch the properties from properties file.
     *
     * @return
     */
    public static Properties getBrokerProperties() {
        Properties props = new Properties();
        props.setProperty("monitor.email.exchange.name", "monitor");
        props.setProperty("monitor.email.broker.URI", "amqp://localhost:5672");
        props.setProperty("monitor.email.broker.queue1.name", "q1");
        props.setProperty("monitor.email.broker.queue2.name", "q2");
        return props;
    }

}
