package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessOutputEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessOutputRepository extends ExpCatAbstractRepository<OutputDataObjectType, ProcessOutputEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessOutputRepository.class);

    public ProcessOutputRepository() { super(OutputDataObjectType.class, ProcessOutputEntity.class); }
}
