package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the USER_ALLOCATION_DETAILS database table.
 * 
 */
@Entity
@Table(name="USER_ALLOCATION_DETAILS")
@NamedQuery(name="UserAllocationDetailEntity.findAll", query="SELECT u FROM UserAllocationDetailEntity u")
public class UserAllocationDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="PROJECT_ID")
	private int projectId;

	@Column(name="ALLOCATION_STATUS")
	private String allocationStatus;

	@Lob
	@Column(name="COMMENTS")
	private String comments;

	@Column(name="DISK_USAGE_RANGE_PER_JOB")
	private BigInteger diskUsageRangePerJob;

	@Column(name="DOCUMENTS")
	private Object documents;

	@Column(name="KEYWORDS")
	private Object keywords;

	@Column(name="MAX_MEMORY_PER_CPU")
	private BigInteger maxMemoryPerCpu;

	@Column(name="NUMBER_OF_CPU_PER_JOB")
	private BigInteger numberOfCpuPerJob;

	@Column(name="PROJECT_DESCRIPTION")
	private Object projectDescription;

	@Column(name="REQUESTED_DATE")
	private BigInteger requestedDate;

	@Column(name="TITLE")
	private Object title;

	@Column(name="TYPICAL_SU_PER_JOB")
	private BigInteger typicalSuPerJob;

	@Column(name="USERNAME")
	private String username;

	public UserAllocationDetailEntity() {
	}

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getAllocationStatus() {
		return this.allocationStatus;
	}

	public void setAllocationStatus(String allocationStatus) {
		this.allocationStatus = allocationStatus;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public BigInteger getDiskUsageRangePerJob() {
		return this.diskUsageRangePerJob;
	}

	public void setDiskUsageRangePerJob(BigInteger diskUsageRangePerJob) {
		this.diskUsageRangePerJob = diskUsageRangePerJob;
	}

	public Object getDocuments() {
		return this.documents;
	}

	public void setDocuments(Object documents) {
		this.documents = documents;
	}

	public Object getKeywords() {
		return this.keywords;
	}

	public void setKeywords(Object keywords) {
		this.keywords = keywords;
	}

	public BigInteger getMaxMemoryPerCpu() {
		return this.maxMemoryPerCpu;
	}

	public void setMaxMemoryPerCpu(BigInteger maxMemoryPerCpu) {
		this.maxMemoryPerCpu = maxMemoryPerCpu;
	}

	public BigInteger getNumberOfCpuPerJob() {
		return this.numberOfCpuPerJob;
	}

	public void setNumberOfCpuPerJob(BigInteger numberOfCpuPerJob) {
		this.numberOfCpuPerJob = numberOfCpuPerJob;
	}

	public Object getProjectDescription() {
		return this.projectDescription;
	}

	public void setProjectDescription(Object projectDescription) {
		this.projectDescription = projectDescription;
	}

	public BigInteger getRequestedDate() {
		return this.requestedDate;
	}

	public void setRequestedDate(BigInteger requestedDate) {
		this.requestedDate = requestedDate;
	}

	public Object getTitle() {
		return this.title;
	}

	public void setTitle(Object title) {
		this.title = title;
	}

	public BigInteger getTypicalSuPerJob() {
		return this.typicalSuPerJob;
	}

	public void setTypicalSuPerJob(BigInteger typicalSuPerJob) {
		this.typicalSuPerJob = typicalSuPerJob;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}