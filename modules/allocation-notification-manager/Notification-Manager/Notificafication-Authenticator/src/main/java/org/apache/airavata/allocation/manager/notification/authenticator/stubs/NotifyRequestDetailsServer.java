package org.apache.airavata.allocation.manager.notification.authenticator.stubs;

import java.util.List;

import org.apache.airavata.allocation.manager.notification.authenticator.data.Data;
import org.apache.airavata.allocation.manager.notification.authenticator.models.*;
import org.apache.airavata.allocation.manager.notification.authenticator.stubs.*;
import org.apache.thrift.TException;

public class NotifyRequestDetailsServer {
	public List<Reviewer> getReviewers(String requestID) throws TException {
		
		return (new Data()).getReviewers(requestID);
	}

	public Admin getAdmin(String requestID) throws TException {
		// TODO Auto-generated method stub
		return (new Data()).getAdmin(requestID);
	}

	public Request getStatus(String requestID) throws TException {
		// TODO Auto-generated method stub
		return (new Data()).getStatus(requestID);
	}

}
