package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.OwnerEntity;
import org.apache.airavata.sharing.registry.db.entities.OwnerPK;
import org.apache.airavata.sharing.registry.models.Owner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwnerRepository extends AbstractRepository<Owner, OwnerEntity, OwnerPK>{

    private final static Logger logger = LoggerFactory.getLogger(OwnerRepository.class);

    public OwnerRepository() {
        super(Owner.class, OwnerEntity.class);
    }
}
