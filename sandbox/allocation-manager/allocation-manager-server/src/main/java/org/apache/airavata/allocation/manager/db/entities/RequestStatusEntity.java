package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the request_status database table.
 * 
 */
@Entity
@Table(name="request_status")
@NamedQuery(name="RequestStatus.findAll", query="SELECT r FROM RequestStatus r")
public class RequestStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="PROJECT_ID")
	private String projectId;

	@Column(name="AWARD_ALLOCATION")
	private BigInteger awardAllocation;

	@Column(name="END_DATE")
	private BigInteger endDate;

	@Lob
	private String reviewers;

	@Column(name="START_DATE")
	private BigInteger startDate;

	private String status;

	public RequestStatus() {
	}

	public String getProjectId() {
		return this.projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public BigInteger getAwardAllocation() {
		return this.awardAllocation;
	}

	public void setAwardAllocation(BigInteger awardAllocation) {
		this.awardAllocation = awardAllocation;
	}

	public BigInteger getEndDate() {
		return this.endDate;
	}

	public void setEndDate(BigInteger endDate) {
		this.endDate = endDate;
	}

	public String getReviewers() {
		return this.reviewers;
	}

	public void setReviewers(String reviewers) {
		this.reviewers = reviewers;
	}

	public BigInteger getStartDate() {
		return this.startDate;
	}

	public void setStartDate(BigInteger startDate) {
		this.startDate = startDate;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}