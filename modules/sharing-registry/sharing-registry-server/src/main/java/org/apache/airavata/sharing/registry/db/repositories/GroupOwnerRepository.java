package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.GroupOwnerEntity;
import org.apache.airavata.sharing.registry.db.entities.GroupOwnerPK;
import org.apache.airavata.sharing.registry.models.GroupOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupOwnerRepository extends AbstractRepository<GroupOwner, GroupOwnerEntity, GroupOwnerPK>{

    private final static Logger logger = LoggerFactory.getLogger(GroupOwnerRepository.class);

    public GroupOwnerRepository() {
        super(GroupOwner.class, GroupOwnerEntity.class);
    }
}
