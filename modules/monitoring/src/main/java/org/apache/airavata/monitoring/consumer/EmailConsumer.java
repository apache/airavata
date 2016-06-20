package org.apache.airavata.monitoring.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.airavata.monitoring.MessageExtract;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class EmailConsumer extends DefaultConsumer {

    public EmailConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(body);
        ObjectInput in = new ObjectInputStream(bis);
        MessageExtract msgExtract = null;
        Message message = null;
        try {
            // deserializing the message received from broker into
            // MessageExtract
            msgExtract = (MessageExtract) in.readObject();
            // reconstructing the javax Message
            message = reContructMessage(msgExtract);
            System.out.println(" [x] Received message from'"
                    + message.getFrom()[0].toString() + "'");
            System.out.println(" [x] Received message Recepients'"
                    + message.getRecipients(Message.RecipientType.TO)[0]
                    .toString() + "'");
            System.out.println(" [x] Received message subject'"
                    + message.getSubject() + "'");
            System.out.println(" [x] Received message content'"
                    + message.getContent() + "'");
            processMessage(message);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * @param msgExtract
     * @return Message with content, subject,from and to fields representative
     * of original e-mail message
     * @throws MessagingException
     */
    private Message reContructMessage(MessageExtract msgExtract)
            throws MessagingException {
        Message message = new MimeMessage((Session) null);
        message = new MimeMessage((Session) null);
        message.setText(msgExtract.getContent());
        message.setSubject(msgExtract.getSubject());
        message.addRecipients(Message.RecipientType.TO,
                msgExtract.getRecipients());
        message.setFrom(msgExtract.getFrom());
        return message;
    }

    /***
     * Process e-mail message
     *
     * @param message e-mail message
     */
    private void processMessage(Message message) {
        // TODO processing
    }

}
