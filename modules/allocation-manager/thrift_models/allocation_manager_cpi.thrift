namespace java org.apache.airavata.allocation.manager.service.cpi

include "./allocation_manager_models.thrift"
include "../../../thrift-interface-descriptions/airavata-apis/airavata_errors.thrift"
include "../../../thrift-interface-descriptions/airavata-apis/security_model.thrift"

service AllocationRegistryService{

    /**
      <p>API method to create new allocation requests</p>
    */
    i64 createAllocationRequest(1: required security_model.AuthzToken authzToken,2: required allocation_manager_models.UserAllocationDetail allocDetail) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to delete allocation request</p>
    */
    bool deleteAllocationRequest(1: required security_model.AuthzToken authzToken,2: required i64 projectId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to get an allocation Request details based on a projectid</p>
    */
    allocation_manager_models.UserAllocationDetail getAllocationRequest(1: required security_model.AuthzToken authzToken,2: required i64 projectId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to update an allocation Request</p>
    */
    bool updateAllocationRequest(1: required security_model.AuthzToken authzToken,2: required allocation_manager_models.UserAllocationDetail allocDetail) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/** USER_SPECIFIC_RESOURCE_DETAILS */

/**
      <p>API method to create new specific resource requests</p>
    */
    i64 createUserSpecificResource(1: required security_model.AuthzToken authzToken,2: required allocation_manager_models.UserSpecificResourceDetail allocDetail) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to delete specific resource requests</p>
    */
    bool deleteUserSpecificResource(1: required security_model.AuthzToken authzToken,2: required i64 projectId,3: required string specificResource) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to get specific resource requests based on a project id</p>
    */
    list<allocation_manager_models.UserSpecificResourceDetail> getUserSpecificResource(1: required security_model.AuthzToken authzToken,2: required i64 projectId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to update specific resource requests</p>
    */
    bool updateUserSpecificResource(1: required security_model.AuthzToken authzToken,2: required i64 projectId,3: required list<allocation_manager_models.UserSpecificResourceDetail> listUserSpecificResource) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/** Methods for admin **/

/**
    <p>API method to get all allocation requests for admin</p>
    */
    list<allocation_manager_models.UserAllocationDetail> getAllRequests(1: required security_model.AuthzToken authzToken,2: required string userName, 3: required string userRole) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/**
    <p>API method to assign reviewers</p>
    */
    bool assignReviewers(1: required security_model.AuthzToken authzToken,2:required i64 projectId , 3: required string reviewerId, 4: required string adminId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/**
    <p>API method to update request submitted by reviewer</p>
    */
    bool updateRequestByReviewer(1: required security_model.AuthzToken authzToken, 2: required allocation_manager_models.ReviewerAllocationDetail reviewerAllocationDetail) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/**
        <p>API method to get all requests assigned to the reviewers</p>
        */
        list<allocation_manager_models.UserAllocationDetail> getAllRequestsForReviewers(1: required security_model.AuthzToken authzToken,2: required string userName) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    

/**
        <p>API method to get all the reviews for a request</p>
        */
        list<allocation_manager_models.ReviewerAllocationDetail> getAllReviewsForARequest(1: required security_model.AuthzToken authzToken,2:required i64 projectId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/** REVIEWER_SPECIFIC_RESOURCE_DETAILS */

/**
      <p>API method to create new specific resource requests for a reviewer</p>
    */
    i64 createReviewerSpecificResource(1: required security_model.AuthzToken authzToken,2: required allocation_manager_models.ReviewerSpecificResourceDetail allocDetail) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to delete specific resource requests for a reviewer</p>
    */
    bool deleteReviewerSpecificResource(1: required security_model.AuthzToken authzToken,2: required i64 projectId,3: required string specificResource) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
    /**
     <p>API method to get specific resource requests for a reviewer</p>
    */
    list<allocation_manager_models.ReviewerSpecificResourceDetail> getReviewerSpecificResource(1: required security_model.AuthzToken authzToken,2: required i64 projectId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);
    
/**
     <p>API method to update specific resource requests for a reviewer</p>
    */
    bool updateReviewerSpecificResource(1: required security_model.AuthzToken authzToken,2: required i64 projectId,3: required list<allocation_manager_models.ReviewerSpecificResourceDetail> listReviewerSpecificResource) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/**
        <p>API method to get all unassigned reviewers for a request</p>
    */
        list<allocation_manager_models.ProjectReviewer> getAllAssignedReviewersForRequest(1: required security_model.AuthzToken authzToken,2:required i64 projectId) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

        /**
        <p>API method to approve a request</p>
        */
        bool approveRequest(1: required security_model.AuthzToken authzToken,2: required i64 projectId, 3: required string adminId, 4: required i64 startDate, 5: required i64 endDate, 6: required i64 awardAllocation,7: required string specificResourceName)  throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

 /**
        <p>API method to reject a request</p>
        */
        bool rejectRequest(1: required security_model.AuthzToken authzToken,2:required i64 projectId, 3: required string adminId,4: required string rejectionReason,5: required string specificResourceName) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

        /**
        <p>API method to get the remaining allocation units</p>
        */
        i64 getRemainingAllocationUnits(1: required security_model.AuthzToken authzToken,2:required string specificResource) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

        /**
        <p>API method to deduct the used allocation units</p>
        */
        bool deductAllocationUnits(1: required security_model.AuthzToken authzToken,2:required string specificResource,3:required i64 allocationUnits) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

/**
        <p>API method to check if the user can submit a new request</p>
        */
        bool canSubmitRequest(1: required security_model.AuthzToken authzToken, 2:required string userName) throws (1: allocation_manager_models.AllocationManagerException ame,2: airavata_errors.AuthorizationException ae);

}
