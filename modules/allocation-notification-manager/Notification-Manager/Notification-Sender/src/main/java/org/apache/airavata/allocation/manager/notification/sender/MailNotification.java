package org.apache.airavata.allocation.manager.notification.sender;

import java.util.List;
import org.apache.commons.mail.*;

public class MailNotification {
	public void sendMail(String requestId, String status,String senderList ) {
		
		NotificationMessage notificationMessage = new NotificationMessage();
		 
		System.out.println(notificationMessage.getNotificationMessageForStatus(status));
		
		Email email = new SimpleEmail();
		email.setHostName("smtp.googlemail.com");
		email.setSmtpPort(465);
		email.setAuthenticator(new DefaultAuthenticator("username", "password"));
		email.setSSLOnConnect(true);
		email.setFrom("user@gmail.com");
		email.setSubject("TestMail");
		email.setMsg("This is a test mail ... :-)");
		email.addTo("foo@bar.com");
		email.send();

	}
}
