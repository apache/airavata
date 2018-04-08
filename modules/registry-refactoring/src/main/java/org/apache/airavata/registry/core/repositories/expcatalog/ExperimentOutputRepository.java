package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentInputEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentOutputRepository extends ExpCatAbstractRepository<OutputDataObjectType, ExperimentInputEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentOutputRepository.class);

    public ExperimentOutputRepository() { super(OutputDataObjectType.class, ExperimentInputEntity.class); }
}
