package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.AdminEntity;
import org.apache.airavata.sharing.registry.db.entities.AdminPK;
import org.apache.airavata.sharing.registry.models.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminRepository extends AbstractRepository<Admin, AdminEntity, AdminPK> {

    private final static Logger logger = LoggerFactory.getLogger(AdminRepository.class);

    public AdminRepository() {
        super(Admin.class, AdminEntity.class);
    }

}
