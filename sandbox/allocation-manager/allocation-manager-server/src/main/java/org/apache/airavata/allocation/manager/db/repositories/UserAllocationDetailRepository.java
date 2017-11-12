package org.apache.airavata.allocation.manager.db.repositories;

import org.apache.airavata.allocation.manager.db.entities.UserAllocationDetailEntity;
import org.apache.airavata.sharing.registry.models.UserAllocationDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAllocationDetailRepository extends AbstractRepository<UserAllocationDetail, UserAllocationDetailEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    public UserAllocationDetailRepository(){
        super(UserAllocationDetail.class, UserAllocationDetailEntity.class);
    }
}