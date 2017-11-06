package org.apache.airavata.allocation.manager.notification.sender;

import org.apache.airavata.allocation.manager.notification.models.NotificationMessage;

public class EmailNotificationMessage {

	public NotificationMessage getEmailMessage(String status) {
		NotificationMessage result = new NotificationMessage();
		result.setNotificationMessage("hello");
		return result;
	}
}
