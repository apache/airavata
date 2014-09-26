package org.apache.airavata.gfac.bes.utils;

import java.io.Serializable;

import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

public class ActivityInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private EndpointReferenceType activityEPR;
	
	private ActivityStatusType activityStatusDoc;
	

	public EndpointReferenceType getActivityEPR() {
		return activityEPR;
	}
	public void setActivityEPR(EndpointReferenceType activityEPR) {
		this.activityEPR = activityEPR;
	}
	public ActivityStatusType getActivityStatus() {
		return activityStatusDoc;
	}
	public void setActivityStatusDoc(ActivityStatusType activityStatusDoc) {
		this.activityStatusDoc = activityStatusDoc;
	}
	
}
