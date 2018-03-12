package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the REVIEWER_ALLOCATION_DETAILS database table.
 * 
 */
@Entity
@Table(name="REVIEWER_ALLOCATION_DETAILS")
@NamedQuery(name="ReviewerAllocationDetailEntity.findAll", query="SELECT r FROM ReviewerAllocationDetailEntity r")
public class ReviewerAllocationDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID")
	private int id;

	@Column(name="DISK_USAGE_RANGE_PER_JOB")
	private BigInteger diskUsageRangePerJob;

	@Column(name="DOCUMENTS")
	private Object documents;

	@Column(name="MAX_MEMORY_PER_CPU")
	private BigInteger maxMemoryPerCpu;

	@Column(name="NUMBER_OF_CPU_PER_JOB")
	private BigInteger numberOfCpuPerJob;

	@Column(name="PROJECT_ID")
	private int projectId;

	@Column(name="REVIEW_DATE")
	private BigInteger reviewDate;

	@Column(name="TYPICAL_SU_PER_JOB")
	private BigInteger typicalSuPerJob;

	@Column(name="USERNAME")
	private String username;

	public ReviewerAllocationDetailEntity() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public BigInteger getReviewDate() {
		return this.reviewDate;
	}

	public void setReviewDate(BigInteger reviewDate) {
		this.reviewDate = reviewDate;
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