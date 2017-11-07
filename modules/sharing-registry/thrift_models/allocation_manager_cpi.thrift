namespace java org.apache.airavata.sharing.registry.service.cpi

include "./allocation_manager_models.thrift"

service AllocationRegistryService{

    /**
      <p>API method to create new allocation requests</p>
    */
    string createAllocationRequest(1: required allocation_manager_models.UserAllocationDetails allocDetails)
    
    /**
     <p>API method to check if the allocation request exists</p>
    */
    bool isAllocationRequestExists(1: required string projectId)
    
    /**
     <p>API method to delete allocation request</p>
    */
    bool deleteAllocationRequest(1: required string projectId)
    
    /**
     <p>API method to get an allocation Request</p>
    */
    allocation_manager_models.UserAllocationDetails getAllocationRequest(1: required string projectId)

}
