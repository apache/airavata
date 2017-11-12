package org.apache.airavata.allocation.manager.db.repositories;

import org.apache.airavata.allocation.manager.db.entities.UserDetailEntity;
import org.apache.airavata.sharing.registry.models.UserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDetailRepository extends AbstractRepository<UserDetail, UserAllocationEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    public UserDetailRepository(){
        super(UserDetail.class, UserDetailEntity.class);
    }

    public static void main(String args[])
    {

    }
}