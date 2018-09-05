package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the REVIEWER_SPECIFIC_RESOURCE_DETAILS database table.
 * 
 */
@Entity
@Table(name="REVIEWER_SPECIFIC_RESOURCE_DETAILS")
@NamedQuery(name="ReviewerSpecificResourceDetailEntity.findAll", query="SELECT r FROM ReviewerSpecificResourceDetailEntity r")
public class ReviewerSpecificResourceDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID")
	private int id;

	@Column(name="APPLICATIONS_TO_BE_USED")
	private String applicationsToBeUsed;

	@Column(name="COMMENTS")
	private String comments;

	@Column(name="PROJECT_ID")
	private int projectId;

	@Column(name="RESOURCE_TYPE")
	private String resourceType;

	@Column(name="REVIEWED_SERVICE_UNITS")
	private BigInteger reviewedServiceUnits;

	@Column(name="SPECIFIC_RESOURCE")
	private String specificResource;

	@Column(name="USERNAME")
	private String username;

	public ReviewerSpecificResourceDetailEntity() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getApplicationsToBeUsed() {
		return this.applicationsToBeUsed;
	}

	public void setApplicationsToBeUsed(String applicationsToBeUsed) {
		this.applicationsToBeUsed = applicationsToBeUsed;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getResourceType() {
		return this.resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public BigInteger getReviewedServiceUnits() {
		return this.reviewedServiceUnits;
	}

	public void setReviewedServiceUnits(BigInteger reviewedServiceUnits) {
		this.reviewedServiceUnits = reviewedServiceUnits;
	}

	public String getSpecificResource() {
		return this.specificResource;
	}

	public void setSpecificResource(String specificResource) {
		this.specificResource = specificResource;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}