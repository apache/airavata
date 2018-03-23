 namespace java org.apache.airavata.allocation.manager.models
 
/**
* <p>Required allocation request details</p>
* <li>projectId : (primary key) Ask the user to assign project ID, but this project should unique, we will need an API endpoint to check whether this ID is not used by other projects and the username</li>
* <li>allocationStatus : Overall status of the allocation</li>
* <li>diskUsageRangePerJob : An optional field to help reviewer and PI for allocation approval</li>
* <li>documents : Resume, CV, PI’s portfolio etc</li>
* <li>fieldOfScience :An optional field to help reviewer and PI for allocation approval</li>
* <li>keywords : Keyword will be helpful in search</li>
* <li>maxMemoryPerCpu :An optional field to help reviewer and PI for allocation approval</li>
* <li>numberOfCpuPerJob : An optional field to help reviewer and PI for allocation approval</li>
* <li>projectDescription :(Eg: Hypothesis, Model Systems, Methods, and Analysis)</li>
* <li>projectReviewedAndFundedBy : (Eg., NSF, NIH, DOD, DOE, None etc...). An optional field to help reviewer and PI for allocation approval</li>
* <li>requestedDate: The date the allocation was requested</li>
* <li>serviceUnits : 1 SU is approximately 1 workstation CPU hour, if the user fails to give a value, default value will be chosen.</li>
* <li>specificResourceSelection : This list will be fetched from resource discovery module, in case of community allocation, the request is subject to reviewers, PI discretion and availability</li>
* <li>title : Assign a title to allocation request</li>
* <li>typeOfAllocation : If the User has an exclusive allocation with third party organization and wants to use airavata middleware to manage jobs.</li>
* <li>typicalSuPerJob :  An optional field to help reviewer and PI for allocation approval</li>
**/
struct UserAllocationDetail{
	1:optional i64 projectId,
	2:optional string allocationStatus,
	3:optional string comments,
	4:optional i64 diskUsageRangePerJob,
	5:optional binary documents,
	6:optional string keywords,
	7:optional i64 maxMemoryPerCpu,
	8:optional i64 numberOfCpuPerJob,
	9:optional string projectDescription,
	10:optional i64 requestedDate,
	11:optional string title,
	12:optional i64 typicalSuPerJob,
	13:optional string username
}


/**
* <p>User Specific Resource Detail</p>
* <li>Id: Unique id</li>
* <li>allocatedServiceUnits: Allocation requested by the user</li>
* <li>applicationsToBeUsed: Applications to be used</li>
* <li>endDate: End date updated by the admin</li>
* <li>projectId: Unique id of the project</li>
* <li>rejectionReason: Rejection reason updated by the admin</li>
* <li>resourceType: Resource type for the request</li>
* <li>specificResource: Specific resource for the request</li>
* <li>startDate: Start date of the allocation updated by the admin</li>
* <li>subStatus: Status of the specific resource</li>
**/
struct UserSpecificResourceDetail{
	1:optional i64 id,
	2:optional i64 allocatedServiceUnits,
	3:optional string applicationsToBeUsed,
	4:optional i64 endDate,
	5:optional i64 projectId,
	6:optional string rejectionReason,
	7:optional i64 requestedServiceUnits,
	8:optional string resourceType,
	9:optional string specificResource,
	10:optional i64 startDate,
	11:optional string subStatus,
	12:optional i64 usedServiceUnits
}


/**
* <p>Allocation Request status details</p>
* <li>projectId: Unique id of the project</li>
* <li>awardAllocation: Allocation awarded</li>
* <li>endDate: End date of the request</li>
* <li>reviewers: reviewers of the request</li>
* <li>startDate: Start date of the allocation</li>
* <li>status: Status of the allocation request</li>
**/
struct ReviewerAllocationDetail{
	1:optional i64 id,
	2:optional i64 projectId,
	3:optional i64 diskUsageRangePerJob,
	4:optional binary documents,
	5:optional i64 maxMemoryPerCpu,
	6:optional i64 numberOfCpuPerJob,
	7:optional i64 reviewDate,
	8:optional i64 typicalSuPerJob,
	9:optional string username
}


/**
* <p>Allocation Request status details</p>
* <li>projectId: Unique id of the project</li>
* <li>awardAllocation: Allocation awarded</li>
* <li>endDate: End date of the request</li>
* <li>reviewers: reviewers of the request</li>
* <li>startDate: Start date of the allocation</li>
* <li>status: Status of the allocation request</li>
**/
struct ReviewerSpecificResourceDetail{
	1:optional i64 id,
	2:optional string applicationsToBeUsed,
	3:optional string comments,
	4:optional i64 projectId,
	5:optional string resourceType,
	6:optional i64 reviewedServiceUnits,
	7:optional string specificResource,
	8:optional string username
}

/**
* <p>Allocation Request status details</p>
* <li>projectId: Unique id of the project</li>
* <li>awardAllocation: Allocation awarded</li>
* <li>endDate: End date of the request</li>
* <li>reviewers: reviewers of the request</li>
* <li>startDate: Start date of the allocation</li>
* <li>status: Status of the allocation request</li>
**/
struct ProjectReviewer{
	1:optional i64 projectId,
	2:optional string reviewerUsername,
	3:optional i64 id
}

/**
* <p>Exception model used in the allocation manager service</p>
**/
exception AllocationManagerException {
  1: required string message
}

