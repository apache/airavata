package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
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

    public Map<JobManagerCommand, String> getJobManagerCommand(String resourceJobManagerId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID, resourceJobManagerId);

        List resultSet = (List) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(QueryConstants.GET_JOB_MANAGER_COMMAND);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.setFirstResult(0).getResultList();
        });

        List<JobManagerCommandEntity> jobManagerCommandEntityList = resultSet;
        Map<JobManagerCommand, String> jobManagerCommandMap= new HashMap<JobManagerCommand,String>();
        for (JobManagerCommandEntity jm: jobManagerCommandEntityList) {
            jobManagerCommandMap.put(JobManagerCommand.valueOf(jm.getId().getCommandType()), jm.getCommand());
        }
        return jobManagerCommandMap;
    }

    public Map<ApplicationParallelismType, String> getParallelismPrefix(String resourceJobManagerId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID, resourceJobManagerId);

        List resultSet = (List) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(QueryConstants.GET_PARALLELISM_PREFIX);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.setFirstResult(0).getResultList();
        });

        List<ParallelismCommandEntity> parallelismCommandEntityList = resultSet;
        Map<ApplicationParallelismType, String> applicationParallelismTypeMap= new HashMap<ApplicationParallelismType,String>();
        for (ParallelismCommandEntity pc: parallelismCommandEntityList) {
            applicationParallelismTypeMap.put(ApplicationParallelismType.valueOf(pc.getId().getCommandType()), pc.getCommand());
        }
        return applicationParallelismTypeMap;
    }

}
