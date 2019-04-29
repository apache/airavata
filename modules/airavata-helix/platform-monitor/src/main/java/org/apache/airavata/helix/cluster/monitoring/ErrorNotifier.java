package org.apache.airavata.helix.cluster.monitoring;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ErrorNotifier {

    private final static Logger logger = LoggerFactory.getLogger(ErrorNotifier.class);

    public void sendNotification(PlatformMonitorError monitorError) {
        if (monitorError.getError() == null) {
            logger.error("Monitor error " + monitorError.getReason());
        } else {
            logger.error("Monitor error " + monitorError.getReason(), monitorError.getError());
        }
        sendEmail(monitorError);
    }

    private void sendEmail(PlatformMonitorError monitorError) {

        try {

            String username = ServerSettings.getSetting("sender.email.account");
            String password = ServerSettings.getSetting("sender.email.password");
            String targetEmails = ServerSettings.getSetting("target.email.accounts");

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            String[] targetEmailArr = targetEmails.split(",");

            for (String targetEmail : targetEmailArr) {
                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(targetEmail));
                message.setSubject("Possible issue in " + ServerSettings.getSetting("platform.name"));
                message.setText(monitorError.getReason() + "\n" + "Error code " + monitorError.getErrorCode() + "\n" +
                        (monitorError.getError() != null ? ExceptionUtils.getFullStackTrace(monitorError.getError()) : ""));

                Transport.send(message);

                logger.info("Sent notification email to " + targetEmail);
            }

        } catch (Exception e) {
            logger.error("Failed to send email", e);
        }
    }
}
