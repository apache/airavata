package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentInputEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentInputRepository extends ExpCatAbstractRepository<InputDataObjectType, ExperimentInputEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentInputRepository.class);

    public ExperimentInputRepository() { super(InputDataObjectType.class, ExperimentInputEntity.class); }
}
