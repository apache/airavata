package org.apache.airavata.allocation.manager.notification.sender;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.airavata.allocation.manager.notification.models.NotificationMessage;

public class EmailNotificationMessage {
	public NotificationMessage getEmailMessage(String status) {
		NotificationMessage result = new NotificationMessage();
		Properties prop = new Properties();
		InputStream input = null;

		try {
			InputStream input2 = new FileInputStream("./src/main/resources/messages.properties");
			prop.load(input2);

			switch (status) {
			case "APRROVED":
				result.setMessage(prop.getProperty("SUBJECT_APPROVED"));
				result.setSubject(prop.getProperty("MESSAGE_APPROVED"));
				break;
			case "REJECTED":
				result.setMessage(prop.getProperty("SUBJECT_REJECTED"));
				result.setSubject(prop.getProperty("MESSAGE_REJECTED"));
				break;
			case "IN_PROGRESS":
				result.setMessage(prop.getProperty("SUBJECT_IN_PROCESS"));
				result.setSubject(prop.getProperty("MESSAGE_IN_PROCESS"));
				break;
			case "NEW_REQUEST":
				result.setMessage(prop.getProperty("SUBJECT_NEW_REQUEST"));
				result.setSubject(prop.getProperty("MESSAGE_NEW_REQUEST"));
				break;
			}

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
