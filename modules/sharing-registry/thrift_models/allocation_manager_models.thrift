 namespace java org.apache.airavata.sharing.registry.models


/**
* <p>A user should have an account with airavata to request an allocation</p>
* <li>username : Login Username</li>
* <li>password :Login Password</li>
**/
struct UserDetails{
	1: optional string username,
 	2: optional string password
}

/**
* <p>Field to share sponsorship details</p>
* <li>sponsorshipClass : (Enum)Type of organization, for example: University, Government Agency, Medical Center, Private lab, Company etc</li>
* <li>sponsorName : Name of supervisor, manager, group leader or self</li>
**/
struct Sponsorship{
	1:optional SponsorshipClass sponsorshipClass,
	4:optional string sponsorName
}

/**
* <p>Required allocation request details</p>
* <li>projectId : 	Ask the user to assign project ID, but this project should unique, we will need an API endpoint to check whether this ID is not used by other projects</li>
* <li>applicationsToBeUsed : Select the application that the user intends to use, according to application chosen here, resources that can be allocable will be fetch from resource discovery module. User will not be restricted to these application upon allocation grant, provided the resources allocated support the application.</li>
* <li>diskUsageRangePerJob : An optional field to help reviewer and PI for allocation approval</li>
* <li>documents : Resume, CV, PI’s portfolio etc</li>
* <li>externalAllocationAccessMechanisms :  Mechanism to use for job scheduling (Eg: ssh, 2FA etc)</li>
* <li>externalAllocationAccountPassword :external Login Password</li>
* <li>externalAllocationAccountUsername : external Login Username</li>
* <li>externalAllocationOrganizationName : Name of organization where the user has allocation, can be a list of supported once, still to decide</li>
* <li>externalAllocationProjectId : A chargeable project ID recognised by the the third party organization.</li>
* <li><b>externalAllocationResourceName</b> : Allocated resource</li>
* <li>fieldOfScience :An optional field to help reviewer and PI for allocation approval</li>* <li>allocationType : Community, Campus, Specific Resource (Paid), User with exclusive allocation : Only use airavata to manage jobs.</li>
* <li>keywords : Keyword will be helpful in search</li>* <li>principalInvestigator : In case of Gridchem, only PI can request allocations, but here one suggestion is to allow user with organization affiliation like (staff, postdoctoral, etc) and not students - Name: can be self, Email: To notify the PI about allocation request</li>
* <li>maxMemoryPerCpu :An optional field to help reviewer and PI for allocation approval</li>
* <li>numberOfCpuPerJob : An optional field to help reviewer and PI for allocation approval</li>
* <li>principalInvistigatorEmail :Principal Investigator email</li>
* <li>principalInvistigatorName : Principal Investigator name</li>
* <li>projectDescription :(Eg: Hypothesis, Model Systems, Methods, and Analysis)</li>
* <li>projectReviewedAndFundedBy : (Eg., NSF, NIH, DOD, DOE, None etc...). An optional field to help reviewer and PI for allocation approval</li>
* <li>serviceUnits : 1 SU is approximately 1 workstation CPU hour, if the user fails to give a value, default value will be chosen.</li>
* <li>specificResourceSelection : This list will be fetched from resource discovery module, in case of community allocation, the request is subject to reviewers, PI discretion and availability</li>
* <li>title : Assign a title to allocation request</li>
* <li>typeOfAllocation : If the User has an exclusive allocation with third party organization and wants to use airavata middleware to manage jobs.</li>
* <li>typicalSuPerJob :  An optional field to help reviewer and PI for allocation approval</li>
**/
struct UserAllocationDetails{
1:optional string projectId,
2:optional string applicationsToBeUsed,
3:optional i64 diskUsageRangePerJob,
4:optional binary documents,
5:optional string externalAllocationAccessMechanisms,
6:optional string externalAllocationAccountPassword,
7:optional string externalAllocationAccountUsername,
8:optional string externalAllocationOrganizationName,
9:optional string externalAllocationProjectId,
10:optional string externalAllocationResourceName,
11:optional string fieldOfScience,
12:optional string keywords,
13:optional i64 maxMemoryPerCpu,
14:optional i64 numberOfCpuPerJob,
15:optional string principalInvistigatorEmail,
16:optional string principalInvistigatorName,
17:optional string projectDescription,
18:optional string projectReviewedAndFundedBy,
19:optional i64 serviceUnits,
20:optional string specificResourceSelection,
21:optional string title,
22:optional string typeOfAllocation,
23:optional i64 typicalSuPerJob
}

/**
* <p>If the User has an exclusive allocation with third party organization and wants to use airavata middleware to manage jobs.</p>
* <li>organization : Name of organization where the user has allocation, can be a list of supported once, still to decide</li>
* <li><b>resourceName</b> : Allocated resource</li>
* <li>projectId : A chargeable project ID recognised by the the third party organization.</li>
* <li>accountDetails : details needed for verification (Username, Password)</li>
* <li>accessMech :  Mechanism to use for job scheduling (Eg: ssh, 2FA etc)</li>
**/
struct ExternalAllocation {
    1:optional string organization,
    2:optional string resourceName,
    3: optional string projectId,
    4: optional UserDetails accountDetails,
    5:optional list<string> accessMech
}

/**
* <p>Resource discovery module will help allocation manager service to know the list of resource which are open for allocation. This data should to initialized and maintained by Admin.</p>
* <li>resourceId : Id of the resource</li>
* <li><b>hostName</b> : Hostname</li>
* <li>resourceDesc :Brief description of the resource. </li>
**/
struct ResourceDescription {
    1:optional string resourceId,
    2:optional string hostName,
    3: optional string resourceDesc
}

/**
* <p>This is an internal enum type for managing sponsorship class</p>
**/
enum SponsorshipClass {
	UNIVERSITY,
	GOVERNMENT_AGENCY,
	MEDICAL_CENTER,
	PRIVATE_LAB,
	COMPANY
 }

/**
* <p>Exception model used in the allocation manager service</p>
**/
exception AllocationManagerException {
  1: required string message
}

