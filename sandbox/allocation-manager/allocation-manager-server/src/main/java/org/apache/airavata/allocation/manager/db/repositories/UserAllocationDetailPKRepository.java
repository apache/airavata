package org.apache.airavata.allocation.manager.db.repositories;

import org.apache.airavata.allocation.manager.db.entities.UserAllocationDetailPKEntity;
import org.apache.airavata.sharing.registry.models.UserAllocationDetailPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAllocationDetailPKRepository extends AbstractRepository<UserAllocationDetailPK, UserAllocationDetailPKEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    public UserAllocationDetailPKRepository(){
        super(UserAllocationDetailPK.class, UserAllocationDetailPKEntity.class);
    }
}