package org.apache.airavata.allocation.manager.notification.sender;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.airavata.allocation.manager.notification.models.NotificationMessage;

public class EmailNotificationMessage {
	public static void main(String args[]) {
		System.out.println("this is from function--"+ new EmailNotificationMessage().getEmailMessage("n"));
	}

	public NotificationMessage getEmailMessage(String status) {
		NotificationMessage result = new NotificationMessage();
		Properties prop = new Properties();
		InputStream input = null;

		try {
			InputStream input2 = new FileInputStream("./src/main/resources/messages.properties");
			prop.load(input2);

			System.out.println("this is from messages" + prop.getProperty("subject"));
			result.setMessage( prop.getProperty("subject"));
			result.setSubject( prop.getProperty("message"));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
}
