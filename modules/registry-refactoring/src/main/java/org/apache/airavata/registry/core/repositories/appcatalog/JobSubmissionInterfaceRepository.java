package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.registry.core.entities.appcatalog.JobSubmissionInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.JobSubmissionInterfacePK;

public class JobSubmissionInterfaceRepository extends AppCatAbstractRepository<JobSubmissionInterface, JobSubmissionInterfaceEntity, JobSubmissionInterfacePK> {

    public JobSubmissionInterfaceRepository() {
        super(JobSubmissionInterface.class, JobSubmissionInterfaceEntity.class);
    }
}
