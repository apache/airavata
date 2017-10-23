package org.apache.airavata.allocation.manager.notification.sender;

import java.io.FileReader;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailNotification {
	public void sendMail(String requestId, String userMail) {
		System.out.println("Notification has been sent to"+userMail);

	}
}
