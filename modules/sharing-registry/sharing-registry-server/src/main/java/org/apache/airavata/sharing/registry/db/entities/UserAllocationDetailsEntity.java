package org.apache.airavata.sharing.registry.db.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigInteger;


/**
 * The persistent class for the user_allocation_details database table.
 * 
 */
@Entity
@Table(name="user_allocation_details")
@NamedQuery(name="UserAllocationDetailsEntity.findAll", query="SELECT u FROM UserAllocationDetailsEntity u")
public class UserAllocationDetailsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="PROJECT_ID")
	private String projectId;

	@Lob
	@Column(name="APPLICATIONS_TO_BE_USED")
	private String applicationsToBeUsed;

	@Column(name="DISK_USAGE_RANGE_PER_JOB")
	private BigInteger diskUsageRangePerJob;

	@Lob
	private byte[] documents;

	@Lob
	@Column(name="EXTERNAL_ALLOCATION_ACCESS_MECHANISMS")
	private String externalAllocationAccessMechanisms;

	@Lob
	@Column(name="EXTERNAL_ALLOCATION_ACCOUNT_PASSWORD")
	private String externalAllocationAccountPassword;

	@Lob
	@Column(name="EXTERNAL_ALLOCATION_ACCOUNT_USERNAME")
	private String externalAllocationAccountUsername;

	@Lob
	@Column(name="EXTERNAL_ALLOCATION_ORGANIZATION_NAME")
	private String externalAllocationOrganizationName;

	@Column(name="EXTERNAL_ALLOCATION_PROJECT_ID")
	private String externalAllocationProjectId;

	@Lob
	@Column(name="EXTERNAL_ALLOCATION_RESOURCE_NAME")
	private String externalAllocationResourceName;

	@Lob
	@Column(name="FIELD_OF_SCIENCE")
	private String fieldOfScience;

	@Lob
	private String keywords;

	@Column(name="MAX_MEMORY_PER_CPU")
	private BigInteger maxMemoryPerCpu;

	@Column(name="NUMBER_OF_CPU_PER_JOB")
	private BigInteger numberOfCpuPerJob;

	@Column(name="PRINCIPAL_INVISTIGATOR_EMAIL")
	private String principalInvistigatorEmail;

	@Column(name="PRINCIPAL_INVISTIGATOR_NAME")
	private String principalInvistigatorName;

	@Lob
	@Column(name="PROJECT_DESCRIPTION")
	private String projectDescription;

	@Lob
	@Column(name="PROJECT_REVIEWED_AND_FUNDED_BY")
	private String projectReviewedAndFundedBy;

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

	public UserAllocationDetailsEntity() {
	}

	public String getProjectId() {
		return this.projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
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

	public String getExternalAllocationAccessMechanisms() {
		return this.externalAllocationAccessMechanisms;
	}

	public void setExternalAllocationAccessMechanisms(String externalAllocationAccessMechanisms) {
		this.externalAllocationAccessMechanisms = externalAllocationAccessMechanisms;
	}

	public String getExternalAllocationAccountPassword() {
		return this.externalAllocationAccountPassword;
	}

	public void setExternalAllocationAccountPassword(String externalAllocationAccountPassword) {
		this.externalAllocationAccountPassword = externalAllocationAccountPassword;
	}

	public String getExternalAllocationAccountUsername() {
		return this.externalAllocationAccountUsername;
	}

	public void setExternalAllocationAccountUsername(String externalAllocationAccountUsername) {
		this.externalAllocationAccountUsername = externalAllocationAccountUsername;
	}

	public String getExternalAllocationOrganizationName() {
		return this.externalAllocationOrganizationName;
	}

	public void setExternalAllocationOrganizationName(String externalAllocationOrganizationName) {
		this.externalAllocationOrganizationName = externalAllocationOrganizationName;
	}

	public String getExternalAllocationProjectId() {
		return this.externalAllocationProjectId;
	}

	public void setExternalAllocationProjectId(String externalAllocationProjectId) {
		this.externalAllocationProjectId = externalAllocationProjectId;
	}

	public String getExternalAllocationResourceName() {
		return this.externalAllocationResourceName;
	}

	public void setExternalAllocationResourceName(String externalAllocationResourceName) {
		this.externalAllocationResourceName = externalAllocationResourceName;
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

	public String getPrincipalInvistigatorEmail() {
		return this.principalInvistigatorEmail;
	}

	public void setPrincipalInvistigatorEmail(String principalInvistigatorEmail) {
		this.principalInvistigatorEmail = principalInvistigatorEmail;
	}

	public String getPrincipalInvistigatorName() {
		return this.principalInvistigatorName;
	}

	public void setPrincipalInvistigatorName(String principalInvistigatorName) {
		this.principalInvistigatorName = principalInvistigatorName;
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
