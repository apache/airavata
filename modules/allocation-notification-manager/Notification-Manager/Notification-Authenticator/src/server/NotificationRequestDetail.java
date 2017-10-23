package org.apache.airavata.allocation.manager.notification.authenticator.server;

import org.apache.airavata.allocation.manager.notification.authenticator.models.Request;
import org.apache.airavata.allocation.manager.notification.authenticator.stubs.NotifyRequestDetailsServer;
import org.apache.thrift.TException;

public class NotificationRequestDetail {
	public String processRequest(String requestID) {
		Request request = new Request();
		
		try {
			request = (new NotifyRequestDetailsServer()).getStatus(requestID);
			// checks if the user's request is accepted then notify the user.
			if(request.status.equalsIgnoreCase("Accepted")) {
				return request.emailID;
			}
			// checks if the user's request is new and is to be sent to the admin.
			else if(request.status.equalsIgnoreCase("Admin")) {
				return (new NotifyRequestDetailsServer()).getAdmin(requestID).emailID;
			}
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
