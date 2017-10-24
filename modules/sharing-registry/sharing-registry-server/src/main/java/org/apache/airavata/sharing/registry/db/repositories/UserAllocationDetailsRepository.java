package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.UserAllocationDetailsEntity;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.UserAllocationDetails;
import org.apache.airavata.sharing.registry.db.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAllocationDetailsRepository extends AbstractRepository<UserAllocationDetails, UserAllocationDetailsEntity, UserAllocationDetails> {
    private final static Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    public UserAllocationDetailsRepository(){
        super(UserAllocationDetails.class, UserAllocationDetailsEntity.class);
    }
    
    //Create a new allocation request
    public void createAllocation(UserAllocationDetails reqDetails) throws SharingRegistryException  {
        String query = "INSERT INTO user_allocation_details " + " ("+
        DBConstants.UserAllocationDetailsTable.APPLICATIONS_TO_BE_USED +","+
        DBConstants.UserAllocationDetailsTable.DISK_USAGE_RANGE_PER_JOB +","+
        DBConstants.UserAllocationDetailsTable.EXTERNAL_ALLOCATION_ACCESS_MECHANISMS +","+
        DBConstants.UserAllocationDetailsTable.EXTERNAL_ALLOCATION_ACCOUNT_PASSWORD +","+
        DBConstants.UserAllocationDetailsTable.EXTERNAL_ALLOCATION_ACCOUNT_USERNAME +","+
        DBConstants.UserAllocationDetailsTable.EXTERNAL_ALLOCATION_ORGANIZATION_NAME +","+
        DBConstants.UserAllocationDetailsTable.EXTERNAL_ALLOCATION_PROJECT_ID + "," +
        DBConstants.UserAllocationDetailsTable.EXTERNAL_ALLOCATION_RESOURCE_NAME + "," +
        DBConstants.UserAllocationDetailsTable.FIELD_OF_SCIENCE + "," +
        DBConstants.UserAllocationDetailsTable.MAX_MEMORY_PER_CPU + "," +
        DBConstants.UserAllocationDetailsTable.NUMBER_OF_CPU_PER_JOB + "," +
        DBConstants.UserAllocationDetailsTable.PRINCIPAL_INVISTIGATOR_EMAIL + "," +
        DBConstants.UserAllocationDetailsTable.PRINCIPAL_INVISTIGATOR_NAME + "," +
        DBConstants.UserAllocationDetailsTable.PROJECT_DESCRIPTION + "," +
        DBConstants.UserAllocationDetailsTable.PROJECT_REVIEWED_AND_FUNDED_BY + "," +
        DBConstants.UserAllocationDetailsTable.SERVICE_UNITS + "," +
        DBConstants.UserAllocationDetailsTable.SPECIFIC_RESOURCE_SELECTION + "," +
        DBConstants.UserAllocationDetailsTable.TYPE_OF_ALLOCATION + "," +
        DBConstants.UserAllocationDetailsTable.TYPICAL_SU_PER_JOB + ") VALUES(" + reqDetails.applicationsToBeUsed 
        + "," +reqDetails.diskUsageRangePerJob + ","+ reqDetails.externalAllocationAccessMechanisms +","+
        reqDetails.externalAllocationAccountPassword+ "," + reqDetails.externalAllocationAccountUsername + "," +
        reqDetails.externalAllocationOrganizationName + "," + reqDetails.externalAllocationProjectId 
        + "," + reqDetails.maxMemoryPerCpu + "," +
        reqDetails.numberOfCpuPerJob + "," + reqDetails.principalInvistigatorEmail + "," +
        reqDetails.principalInvistigatorName + "," + reqDetails.projectDescription + "," +
        reqDetails.projectReviewedAndFundedBy + "," + reqDetails.serviceUnits + "," +
        reqDetails.specificResourceSelection + "," +
        reqDetails.typeOfAllocation+"," + reqDetails.typicalSuPerJob +")";
        
        //Check if the requested amount is less than the threshold.
        //If yes then create the request else notify the admin
        create(query);
    }
}