package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.UserAllocationDetailsEntity;
import org.apache.airavata.sharing.registry.models.UserAllocationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAllocationDetailsRepository extends AbstractRepository<UserAllocationDetails, UserAllocationDetailsEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    public UserAllocationDetailsRepository(){
        super(UserAllocationDetails.class, UserAllocationDetailsEntity.class);
    }
}