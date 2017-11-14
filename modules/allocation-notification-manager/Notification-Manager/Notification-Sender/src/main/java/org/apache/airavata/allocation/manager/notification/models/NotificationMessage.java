/*The program in this file works to fetch a notification message for the given status from a resource file*/
package org.apache.airavata.allocation.manager.notification.models ;

public class NotificationMessage {
	
	private String subject;
	private String message;
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
