package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the USER_SPECIFIC_RESOURCE_DETAILS database table.
 * 
 */
@Entity
@Table(name="USER_SPECIFIC_RESOURCE_DETAILS")
@NamedQuery(name="UserSpecificResourceDetailEntity.findAll", query="SELECT u FROM UserSpecificResourceDetailEntity u")
public class UserSpecificResourceDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID")
	private int id;

	@Column(name="ALLOCATED_SERVICE_UNITS")
	private BigInteger allocatedServiceUnits;

	@Lob
	@Column(name="APPLICATIONS_TO_BE_USED")
	private String applicationsToBeUsed;

	@Column(name="END_DATE")
	private BigInteger endDate;

	@Column(name="PROJECT_ID")
	private int projectId;

	@Lob
	@Column(name="REJECTION_REASON")
	private String rejectionReason;

	@Column(name="REQUESTED_SERVICE_UNITS")
	private BigInteger requestedServiceUnits;

	@Column(name="RESOURCE_TYPE")
	private String resourceType;

	@Column(name="SPECIFIC_RESOURCE")
	private String specificResource;

	@Column(name="START_DATE")
	private BigInteger startDate;

	@Column(name="SUB_STATUS")
	private String subStatus;

	public UserSpecificResourceDetailEntity() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigInteger getAllocatedServiceUnits() {
		return this.allocatedServiceUnits;
	}

	public void setAllocatedServiceUnits(BigInteger allocatedServiceUnits) {
		this.allocatedServiceUnits = allocatedServiceUnits;
	}

	public String getApplicationsToBeUsed() {
		return this.applicationsToBeUsed;
	}

	public void setApplicationsToBeUsed(String applicationsToBeUsed) {
		this.applicationsToBeUsed = applicationsToBeUsed;
	}

	public BigInteger getEndDate() {
		return this.endDate;
	}

	public void setEndDate(BigInteger endDate) {
		this.endDate = endDate;
	}

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getRejectionReason() {
		return this.rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public BigInteger getRequestedServiceUnits() {
		return this.requestedServiceUnits;
	}

	public void setRequestedServiceUnits(BigInteger requestedServiceUnits) {
		this.requestedServiceUnits = requestedServiceUnits;
	}

	public String getResourceType() {
		return this.resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getSpecificResource() {
		return this.specificResource;
	}

	public void setSpecificResource(String specificResource) {
		this.specificResource = specificResource;
	}

	public BigInteger getStartDate() {
		return this.startDate;
	}

	public void setStartDate(BigInteger startDate) {
		this.startDate = startDate;
	}

	public String getSubStatus() {
		return this.subStatus;
	}

	public void setSubStatus(String subStatus) {
		this.subStatus = subStatus;
	}

}