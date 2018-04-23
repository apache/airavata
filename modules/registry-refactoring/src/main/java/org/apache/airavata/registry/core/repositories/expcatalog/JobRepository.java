package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobRepository extends ExpCatAbstractRepository<JobModel, JobEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(JobRepository.class);

    public JobRepository() { super(JobModel.class, JobEntity.class); }

    protected String saveJobModelData(JobModel jobModel, CompositeIdentifier cis) throws RegistryException {
        JobEntity jobEntity = saveJob(jobModel, cis);
        return jobEntity.getJobId();
    }

    protected JobEntity saveJob(JobModel jobModel, CompositeIdentifier cis) throws RegistryException {
        if (jobModel.getJobId() == null || jobModel.getJobId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Job's JobId");
            jobModel.setJobId((String) cis.getSecondLevelIdentifier());
        }

        String jobId = jobModel.getJobId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobEntity jobEntity = mapper.map(jobModel, JobEntity.class);

        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating the Primary Key of JobStatus objects for the Job");
            jobEntity.getJobStatuses().forEach(jobStatusEntity -> {
                jobStatusEntity.setJobId(jobId);
                jobStatusEntity.setTaskId((String) cis.getTopLevelIdentifier());
                if (jobStatusEntity.getStatusId() == null) {
                    jobStatusEntity.setStatusId(ExpCatalogUtils.getID("STATUS"));
                }
            });
        }

        if (!isJobExist(cis)) {
            logger.debug("Checking if the Job already exists");
            jobEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(jobEntity));
    }

    public String addJob(JobModel job, String processId) throws RegistryException {
        CompositeIdentifier cis = new CompositeIdentifier(job.getTaskId(), job.getJobId());
        job.setProcessId(processId);
        String jobId = saveJobModelData(job, cis);
        String taskId = (String) cis.getTopLevelIdentifier();
        TaskRepository taskRepository = new TaskRepository();
        TaskModel taskModel = taskRepository.getTask(taskId);
        List<JobModel> jobModelList = taskModel.getJobs();

        if (jobModelList != null && !jobModelList.contains(job)) {
            logger.debug("Adding the Job to the list");
            jobModelList.add(job);
            taskModel.setJobs(jobModelList);
            taskRepository.updateTask(taskModel, taskId);
        }

        return jobId;
    }

    public String updateJob(JobModel job, CompositeIdentifier cis) throws RegistryException {
        return saveJobModelData(job, cis);
    }

    public JobModel getJob(CompositeIdentifier cis) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        return jobRepository.get((String) cis.getSecondLevelIdentifier());
    }

    public String addJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {
        JobModel jobModel = getJob(cis);
        List<JobStatus> jobStatusList = jobModel.getJobStatuses();
        jobStatusList.add(jobStatus);
        jobModel.setJobStatuses(jobStatusList);
        return updateJob(jobModel, cis);
    }

    public String updateJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {
        return addJobStatus(jobStatus, cis);
    }

    public List<JobStatus> getJobStatus(CompositeIdentifier cis) throws RegistryException {
        JobModel jobModel = getJob(cis);
        return jobModel.getJobStatuses();
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        JobRepository jobRepository = new JobRepository();
        List<JobModel> jobModelList;

        if (fieldName.equals(DBConstants.Job.PROCESS_ID)) {
            logger.debug("Search criteria is ProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.PROCESS_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_PROCESS_ID, -1, 0, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Job.TASK_ID)) {
            logger.debug("Search criteria is TaskId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Job.TASK_ID, value);
            jobModelList = jobRepository.select(QueryConstants.GET_JOB_FOR_TASK_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Job module.");
            throw new IllegalArgumentException("Unsupported field name for Job module.");
        }

        return jobModelList;
    }

    public List<String> getJobIds(String fieldName, Object value) throws RegistryException {
        List<String> jobIds = new ArrayList<>();
        List<JobModel> jobModelList = getJobList(fieldName, value);
        for (JobModel jobModel : jobModelList) {
            jobIds.add(jobModel.getJobId());
        }
        return jobIds;
    }

    public boolean isJobExist(CompositeIdentifier cis) throws RegistryException {
        String jobId = (String) cis.getSecondLevelIdentifier();
        return isExists(jobId);
    }

    public void removeJob(CompositeIdentifier cis) throws RegistryException {
        String jobId = (String) cis.getSecondLevelIdentifier();
        delete(jobId);
    }

}
