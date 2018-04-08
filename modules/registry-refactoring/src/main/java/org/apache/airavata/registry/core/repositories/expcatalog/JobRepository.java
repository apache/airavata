package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.registry.core.entities.expcatalog.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRepository extends ExpCatAbstractRepository<JobModel, JobEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(JobRepository.class);

    public JobRepository() { super(JobModel.class, JobEntity.class); }
}
