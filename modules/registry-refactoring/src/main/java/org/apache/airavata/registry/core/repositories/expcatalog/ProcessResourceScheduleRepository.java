package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessResourceScheduleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessResourceScheduleRepository extends ExpCatAbstractRepository<ComputationalResourceSchedulingModel, ProcessResourceScheduleEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessResourceScheduleRepository.class);

    public ProcessResourceScheduleRepository() { super(ComputationalResourceSchedulingModel.class, ProcessResourceScheduleEntity.class); }
}