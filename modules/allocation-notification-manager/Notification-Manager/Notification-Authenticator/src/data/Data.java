package org.apache.airavata.allocation.manager.notification.authenticator.data;

import java.util.*;


import org.apache.airavata.allocation.manager.notification.authenticator.models.*;

public class Data {

	private static List<Reviewer> ReviewerList;

	static {
		ReviewerList = new ArrayList<Reviewer>() {
			{
				add(new Reviewer("1001","demosga123@gmail.com"));
		
			}
		};
	}
	
	private static HashMap<String, Admin> AdminList;

	static {
		AdminList = new HashMap<String, Admin>() {
			{
				put("1001", new Admin("1001","demosga123@gmail.com"));
		
			}
		};
	}
	
	
	private static HashMap<String, Request> RequestList;

	static {
		RequestList = new HashMap<String, Request>() {
			{
				put("1001", new Request("1001","exampleAllocation","Accepted","user123","demosga123@gmail.com"));
		
			}
		};
	}
public List<Reviewer> getReviewers(String requestID) {
		List<Reviewer> result = new ArrayList<Reviewer>();
		for (int i = 0 ; i < RequestList.size();i++) {
			if(ReviewerList.get(i).requestID.equals(requestID)) {
				result.add(ReviewerList.get(i));
			}
		}
		return result;
		}
	

public Admin getAdmin(String requestID) {
		return AdminList.get(requestID);
	}
public Request getStatus(String requestID) {
		return RequestList.get(requestID);
	}

}
