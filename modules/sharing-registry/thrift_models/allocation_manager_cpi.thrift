namespace java org.apache.airavata.sharing.registry.service.cpi

include "./allocation_manager_models.thrift"

service AllocationRegistryService{

    /**
      <p>API method to create a new domain</p>
    */
    void createAllocationRequest(1: required allocation_manager_models.UserAllocationDetails allocDetails)
    }
