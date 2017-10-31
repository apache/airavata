package org.apache.airavata.allocation.manager.notification.sender;

import java.util.List;

public class MailNotification {
	public void sendMail(String requestId, String status,String senderList ) {
		
		NotificationMessage notificationMessage = new NotificationMessage();
		 
		System.out.println(notificationMessage.getNotificationMessageForStatus(status));

	}
}
