package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.entities.appcatalog.*;

import java.util.Map;

public class ResourceJobManagerRepository extends AppCatAbstractRepository<ResourceJobManager, ResourceJobManagerEntity, String> {

    public ResourceJobManagerRepository() {
        super(ResourceJobManager.class, ResourceJobManagerEntity.class);
    }

    public void createJobManagerCommand(Map<JobManagerCommand, String> jobManagerCommands, ResourceJobManagerEntity resourceJobManagerEntity) {
        for (JobManagerCommand commandType : jobManagerCommands.keySet()) {
            if (jobManagerCommands.get(commandType) != null && !jobManagerCommands.get(commandType).isEmpty()) {
                JobManagerCommandPK jobManagerCommandPK = new JobManagerCommandPK();
                jobManagerCommandPK.setCommandType(commandType.toString());
                jobManagerCommandPK.setResourceJobManagerId(resourceJobManagerEntity.getResourceJobManagerId());
                JobManagerCommandEntity jobManagerCommandEntity = new JobManagerCommandEntity();
                jobManagerCommandEntity.setCommand(jobManagerCommands.get(commandType));
                jobManagerCommandEntity.setResourceJobManager(resourceJobManagerEntity);
                jobManagerCommandEntity.setId(jobManagerCommandPK);
                execute(entityManager -> entityManager.merge(jobManagerCommandEntity));
            }
        }
    }

    public void createParallesimPrefix(Map<ApplicationParallelismType, String> parallelismPrefix,ResourceJobManagerEntity resourceJobManagerEntity) {
        for (ApplicationParallelismType commandType : parallelismPrefix.keySet()) {
            if (parallelismPrefix.get(commandType) != null && !parallelismPrefix.get(commandType).isEmpty()) {
                ParallelismCommandPK parallelismCommandPK = new ParallelismCommandPK();
                parallelismCommandPK.setCommandType(commandType.toString());
                parallelismCommandPK.setResourceJobManagerId(resourceJobManagerEntity.getResourceJobManagerId());
                ParallelismCommandEntity parallelismCommandEntity = new ParallelismCommandEntity();
                parallelismCommandEntity.setCommand(parallelismPrefix.get(commandType));
                parallelismCommandEntity.setResourceJobManager(resourceJobManagerEntity);
                parallelismCommandEntity.setId(parallelismCommandPK);
                execute(entityManager -> entityManager.merge(parallelismCommandEntity));
            }
        }
    }
}
