 namespace java org.apache.airavata.allocation.manager.models
 
/**
* <p>Required allocation request details</p>
* <li>projectId : (primary key) This is a unique id given to each new request that is submitted by a user</li>
* <li>allocationStatus : Overall status of the allocation which is the cumulative of all the sub statues of each specific resource associated with the request</li>
* <li>comments : It the comments updated by the user in support of the request</li>
* <li>diskUsageRangePerJob : An optional field to help reviewer and PI for allocation approval </li>
* <li>documents : Resume, CV, PI’s portfolio etc</li>
* <li>keywords : Keyword will be helpful in search</li>
* <li>maxMemoryPerCpu :An optional field to help reviewer and PI for allocation approval</li>
* <li>numberOfCpuPerJob : An optional field to help reviewer and PI for allocation approval</li>
* <li>projectDescription :(Eg: Hypothesis, Model Systems, Methods, and Analysis)</li>
* <li>requestedDate: The date the allocation was requested</li>
* <li>title : Assign a title to allocation request</li>
* <li>typicalSuPerJob :  An optional field to help reviewer and PI for allocation approval</li>
*<li>username: Name of the user (retrieved from the authentication token) who submitted the request
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
* <li>Id: Unique id for the each specific resource associated with the request submitted by the user</li>
* <li>allocatedServiceUnits: Allocation service units allocated by the admin upon approving a request</li>
* <li>applicationsToBeUsed: Applications to be used</li>
* <li>endDate: End date updated by the admin</li>
* <li>projectId: Unique projectid assocaited with the specific resource</li>
* <li>rejectionReason: Rejection reason updated by the admin on rejecting a particular specific request</li>
* <li>requestedServiceUnits: Service units requested by the user for a specific request</li>
* <li>resourceType: Resource type for the request</li>
* <li>specificResource: Specific resource for the request</li>
* <li>startDate: Start date of the allocation updated by the admin</li>
* <li>subStatus: Status of the specific resource</li>
* <li>usedServiceUnits: Total service units used by the user</li>
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
* <li>id: Unique id for the review submitted by a reviewer for a request</li>
* <li>projectId: Unique id of the project</li>
* <li>diskUsageRangePerJob : An optional field to help admin allocation approval </li>
* <li>documents : Resume, CV, PI’s portfolio etc uploaded by reviewer in support</li>
* <li>maxMemoryPerCpu :An optional field updated by the reviewer to help admin for allocation approval</li>
* <li>numberOfCpuPerJob : An optional field updated by the reviewer to help admin for allocation approval</li>
* <li>reviewDate: Date on which reviewer submitted the review</li>
* <li>typicalSuPerJob :  An optional field to help reviewer and PI for allocation approval</li>
* <li>username: Name of the reviewer who submitted the review
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
* <li>id: Unique id for the specific resource detail for each review submitted by the user</li>
* <li>applicationsToBeUsed: Applications to be used</li>
* <li>comments : Field updated by the reviewer in support of the review submitted by him/her</li>
* <li>projectId: Unique id of the project</li>
* <li>resourceType: Resource type for the request</li>
* <li>reviewedServiceUnits: Service units suggested by the reviewer for a specific request</li>
* <li>specificResource: Specific resource for the request suggested by the reviewer</li>
* <li>username: Name of the reviewer who submitted the review
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
* <li>reviewerUsername: Name of the reviewer who submitted the review</li>
* <li>id: Unique id of mapping</li>
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

