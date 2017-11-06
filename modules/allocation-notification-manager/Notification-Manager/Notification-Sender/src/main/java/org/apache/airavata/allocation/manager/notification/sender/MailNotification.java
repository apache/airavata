package org.apache.airavata.allocation.manager.notification.sender;

import java.net.Authenticator;
import java.util.List;

import org.apache.airavata.allocation.manager.notification.models.EmailCredentials;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class MailNotification {
	// public static void main(String args[]) {
	// EmailNotificationConfiguration obj = new EmailNotificationConfiguration();
	// obj.EmailConfigProperties();
	// }
	public boolean sendMail(String requestId, String status, String senderList) {

		EmailNotificationMessage message = new EmailNotificationMessage();
		EmailNotificationConfiguration emailConfiguration = new EmailNotificationConfiguration();
		System.out.println("here--------" + message.getEmailMessage("Accepted").getNotificationMessage());

		// EmailCredentials ob2 = emailConfiguration.getCredentials();
		String username = emailConfiguration.getCredentials().getUserName();
		String password = emailConfiguration.getCredentials().getPassword();
		Email email = new SimpleEmail();
		email.setHostName("smtp.googlemail.com");
		email.setSmtpPort(465);

		email.setAuthenticator(new DefaultAuthenticator("demosga123", "sgauser123"));
		//email.setAuthenticator(auth);
		email.setSSLOnConnect(true);
		try {
			email.setFrom("nikithauc@gmail.com");
			email.setSubject("TestMail");
			email.setMsg(message.getEmailMessage("Accepted").getNotificationMessage());
			email.addTo("nikithauc@gmail.com");
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//mailNotification(username, password);
		
		System.out.println("wse to /String " + username.toString());
		System.out.println("*s*" + password);
		
		return true; //indicating mail has been sent successfully;

	}

	public void mailNotification(String username, String password) {
		
		System.out.println("wb " + username);
		System.out.println("*s*" + password);
		
		Email email = new SimpleEmail();
		email.setHostName("smtp.googlemail.com");
		email.setSmtpPort(465);
		DefaultAuthenticator auth = null;
		// if(hasAuth)auth=new DefaultAuthenticator(username,password);

		email.setAuthenticator(new DefaultAuthenticator(username.toString(), password.toString()));
		email.setAuthenticator(auth);
		email.setSSLOnConnect(true);
		try {
			email.setFrom("nikithauc@gmail.com");
			email.setSubject("TestMail");
			email.setMsg("hi");
			email.addTo("nikithauc@gmail.com");
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
