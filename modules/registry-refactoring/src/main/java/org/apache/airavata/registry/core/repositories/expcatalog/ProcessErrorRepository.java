package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessErrorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessErrorRepository extends ExpCatAbstractRepository<ErrorModel, ProcessErrorEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessErrorRepository.class);

    public ProcessErrorRepository() { super(ErrorModel.class, ProcessErrorEntity.class); }
}
