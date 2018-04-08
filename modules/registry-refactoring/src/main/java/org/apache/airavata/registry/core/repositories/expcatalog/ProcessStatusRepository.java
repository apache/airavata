package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStatusRepository extends ExpCatAbstractRepository<ProcessStatus, ProcessStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessStatusRepository.class);

    public ProcessStatusRepository() { super(ProcessStatus.class, ProcessStatusEntity.class); }
}
