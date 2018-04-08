package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentStatusRepository extends ExpCatAbstractRepository<ExperimentStatus, ExperimentStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentStatusRepository.class);

    public ExperimentStatusRepository() { super(ExperimentStatus.class, ExperimentStatusEntity.class); }
}
