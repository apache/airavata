package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.JobSubmissionInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.JobSubmissionInterfacePK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;

public class JobSubmissionInterfaceRepository extends AppCatAbstractRepository<JobSubmissionInterface, JobSubmissionInterfaceEntity, JobSubmissionInterfacePK> {

    public JobSubmissionInterfaceRepository() {
        super(JobSubmissionInterface.class, JobSubmissionInterfaceEntity.class);
    }

    public String addJobSubmission(String computeResourceId, JobSubmissionInterface jobSubmissionInterface) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobSubmissionInterfaceEntity jobSubmissionInterfaceEntity = mapper.map(jobSubmissionInterface, JobSubmissionInterfaceEntity.class);
        ComputeResourceDescription computeResourceDescription = new ComputeResourceRepository().get(computeResourceId);
        ComputeResourceEntity computeResourceEntity = mapper.map(computeResourceDescription, ComputeResourceEntity.class);
        jobSubmissionInterfaceEntity.setComputeResource(computeResourceEntity);
        jobSubmissionInterfaceEntity.setComputeResourceId(computeResourceId);
        execute(entityManager -> entityManager.merge(jobSubmissionInterfaceEntity));

        return jobSubmissionInterfaceEntity.getJobSubmissionInterfaceId();
    }
}
