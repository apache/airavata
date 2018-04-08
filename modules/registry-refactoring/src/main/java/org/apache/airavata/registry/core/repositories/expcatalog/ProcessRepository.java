package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRepository extends ExpCatAbstractRepository<ProcessModel, ProcessEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessRepository.class);

    public ProcessRepository() { super(ProcessModel.class, ProcessEntity.class); }
}
