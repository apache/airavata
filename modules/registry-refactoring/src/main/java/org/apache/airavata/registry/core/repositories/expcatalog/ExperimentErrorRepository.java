package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentErrorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentErrorRepository extends ExpCatAbstractRepository<ErrorModel, ExperimentErrorEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentErrorRepository.class);

    public ExperimentErrorRepository() { super(ErrorModel.class, ExperimentErrorEntity.class); }
}
