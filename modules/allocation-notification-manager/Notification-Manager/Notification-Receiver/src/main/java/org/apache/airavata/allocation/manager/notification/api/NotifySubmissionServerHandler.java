package org.apache.airavata.allocation.manager.notification.api;

import org.apache.airavata.allocation.manager.notification.authenticator.server.NotificationRequestDetail;
import org.apache.airavata.allocation.manager.notification.sender.MailNotification;
import org.apache.thrift.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class NotifySubmissionServerHandler implements NotifySubmissionService.Iface {

	@Override
	public String notifySubmission(String requestID) throws TException {
	//	(new NotifySubmissionServerHandler()).sendMail(requestID, (new NotifySubmissionServerHandler()).getAdminId());
		String mail = (new NotificationRequestDetail()).processRequest(requestID);
        System.out.println("Mail" + mail);
         (new MailNotification()).sendMail(requestID, mail);
		return "Request processed" + requestID;
	}
}
