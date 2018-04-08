package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.registry.core.entities.expcatalog.JobStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStatusRepository extends ExpCatAbstractRepository<JobStatus, JobStatusEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(JobStatusRepository.class);

    public JobStatusRepository() { super(JobStatus.class, JobStatusEntity.class); }
}
