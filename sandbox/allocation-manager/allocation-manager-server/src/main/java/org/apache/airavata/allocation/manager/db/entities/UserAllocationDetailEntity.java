package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the user_allocation_details database table.
 * 
 */
@Entity
@Table(name="user_allocation_details")
@NamedQuery(name="UserAllocationDetail.findAll", query="SELECT u FROM UserAllocationDetail u")
public class UserAllocationDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private UserAllocationDetailPK id;

	@Lob
	@Column(name="APPLICATIONS_TO_BE_USED")
	private String applicationsToBeUsed;

	@Column(name="DISK_USAGE_RANGE_PER_JOB")
	private BigInteger diskUsageRangePerJob;

	@Lob
	private byte[] documents;

	@Lob
	@Column(name="FIELD_OF_SCIENCE")
	private String fieldOfScience;

	@Lob
	private String keywords;

	@Column(name="MAX_MEMORY_PER_CPU")
	private BigInteger maxMemoryPerCpu;

	@Column(name="NUMBER_OF_CPU_PER_JOB")
	private BigInteger numberOfCpuPerJob;

	@Lob
	@Column(name="PROJECT_DESCRIPTION")
	private String projectDescription;

	@Lob
	@Column(name="PROJECT_REVIEWED_AND_FUNDED_BY")
	private String projectReviewedAndFundedBy;

	@Column(name="REQUESTED_DATE")
	private BigInteger requestedDate;

	@Column(name="SERVICE_UNITS")
	private BigInteger serviceUnits;

	@Lob
	@Column(name="SPECIFIC_RESOURCE_SELECTION")
	private String specificResourceSelection;

	@Lob
	private String title;

	@Column(name="TYPE_OF_ALLOCATION")
	private String typeOfAllocation;

	@Column(name="TYPICAL_SU_PER_JOB")
	private BigInteger typicalSuPerJob;

	public UserAllocationDetail() {
	}

	public UserAllocationDetailPK getId() {
		return this.id;
	}

	public void setId(UserAllocationDetailPK id) {
		this.id = id;
	}

	public String getApplicationsToBeUsed() {
		return this.applicationsToBeUsed;
	}

	public void setApplicationsToBeUsed(String applicationsToBeUsed) {
		this.applicationsToBeUsed = applicationsToBeUsed;
	}

	public BigInteger getDiskUsageRangePerJob() {
		return this.diskUsageRangePerJob;
	}

	public void setDiskUsageRangePerJob(BigInteger diskUsageRangePerJob) {
		this.diskUsageRangePerJob = diskUsageRangePerJob;
	}

	public byte[] getDocuments() {
		return this.documents;
	}

	public void setDocuments(byte[] documents) {
		this.documents = documents;
	}

	public String getFieldOfScience() {
		return this.fieldOfScience;
	}

	public void setFieldOfScience(String fieldOfScience) {
		this.fieldOfScience = fieldOfScience;
	}

	public String getKeywords() {
		return this.keywords;
	}

	public void setKeywords(String keywords) {
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

	public String getProjectDescription() {
		return this.projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getProjectReviewedAndFundedBy() {
		return this.projectReviewedAndFundedBy;
	}

	public void setProjectReviewedAndFundedBy(String projectReviewedAndFundedBy) {
		this.projectReviewedAndFundedBy = projectReviewedAndFundedBy;
	}

	public BigInteger getRequestedDate() {
		return this.requestedDate;
	}

	public void setRequestedDate(BigInteger requestedDate) {
		this.requestedDate = requestedDate;
	}

	public BigInteger getServiceUnits() {
		return this.serviceUnits;
	}

	public void setServiceUnits(BigInteger serviceUnits) {
		this.serviceUnits = serviceUnits;
	}

	public String getSpecificResourceSelection() {
		return this.specificResourceSelection;
	}

	public void setSpecificResourceSelection(String specificResourceSelection) {
		this.specificResourceSelection = specificResourceSelection;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTypeOfAllocation() {
		return this.typeOfAllocation;
	}

	public void setTypeOfAllocation(String typeOfAllocation) {
		this.typeOfAllocation = typeOfAllocation;
	}

	public BigInteger getTypicalSuPerJob() {
		return this.typicalSuPerJob;
	}

	public void setTypicalSuPerJob(BigInteger typicalSuPerJob) {
		this.typicalSuPerJob = typicalSuPerJob;
	}

}