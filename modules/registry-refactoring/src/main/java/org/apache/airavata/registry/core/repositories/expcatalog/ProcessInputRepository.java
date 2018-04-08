package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessInputEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInputRepository extends ExpCatAbstractRepository<InputDataObjectType, ProcessInputEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessInputRepository.class);

    public ProcessInputRepository() { super(InputDataObjectType.class, ProcessInputEntity.class); }
}
