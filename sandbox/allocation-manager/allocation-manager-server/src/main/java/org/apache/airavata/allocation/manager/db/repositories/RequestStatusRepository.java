package org.apache.airavata.allocation.manager.db.repositories;

import org.apache.airavata.allocation.manager.db.entities.RequestStatusEntity;
import org.apache.airavata.sharing.registry.models.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestStatusRepository extends AbstractRepository<RequestStatus, RequestStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(DomainRepository.class);

    public RequestStatusRepository(){
        super(RequestStatus.class, RequestStatusEntity.class);
    }
}