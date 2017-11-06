package org.apache.airavata.allocation.manager.notification.sender;

import java.net.Authenticator;
import java.util.List;

import org.apache.airavata.allocation.manager.notification.models.EmailCredentials;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class MailNotification {
//	public static void main(String args[]) {
//		EmailNotificationConfiguration obj = new EmailNotificationConfiguration();
//		obj.EmailConfigProperties();
//	}
	public void sendMail(String requestId, String status,String senderList ) {
		
		EmailNotificationMessage message = new EmailNotificationMessage();
		EmailNotificationConfiguration emailConfiguration = new EmailNotificationConfiguration(); 
		System.out.println("here--------"+message.getEmailMessage("Accepted").getNotificationMessage());
		
		//EmailCredentials ob2 = emailConfiguration.getCredentials();
		String username =emailConfiguration.getCredentials().getUserName();
		String password =emailConfiguration.getCredentials().getPassword();
		System.out.println("*"+username);
		System.out.println("**"+password);
		  boolean hasAuth=!username.isEmpty();
		Email email = new SimpleEmail();
		email.setHostName("smtp.googlemail.com");
		email.setSmtpPort(465);
	    DefaultAuthenticator auth=null;
	    if(hasAuth)auth=new DefaultAuthenticator(username,password);

	//	email.setAuthenticator(new DefaultAuthenticator(username,password));
	    email.setAuthenticator(auth);
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

	}
}
