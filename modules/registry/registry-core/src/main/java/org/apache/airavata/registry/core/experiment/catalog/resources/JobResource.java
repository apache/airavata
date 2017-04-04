/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.model.status.JobState;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.Job;
import org.apache.airavata.registry.core.experiment.catalog.model.JobPK;
import org.apache.airavata.registry.core.experiment.catalog.model.JobStatus;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JobResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(JobResource.class);
    private String jobId;
    private String taskId;
	private String processId;
    private String jobDescription;
    private Timestamp creationTime;
    private String computeResourceConsumed;
    private String jobName;
    private String workingDir;
    private String stdOut;
    private String stdErr;
    private int exitCode;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getComputeResourceConsumed() {
        return computeResourceConsumed;
    }

    public void setComputeResourceConsumed(String computeResourceConsumed) {
        this.computeResourceConsumed = computeResourceConsumed;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stderr) {
        this.stdErr = stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        switch (type){
            case JOB_STATUS:
                JobStatusResource jobStatusResource = new JobStatusResource();
                jobStatusResource.setJobId(jobId);
                return jobStatusResource;
            default:
                logger.error("Unsupported resource type for job details data resource.", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case JOB_STATUS:
                    generator = new QueryGenerator(JOB_STATUS);
                    generator.setParameter(JobStatusConstants.STATUS_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for job details resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }


    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case JOB_STATUS:
                    generator = new QueryGenerator(JOB_STATUS);
                    generator.setParameter(JobStatusConstants.STATUS_ID, name);
                    q = generator.selectQuery(em);
                    JobStatus status = (JobStatus) q.getSingleResult();
                    JobStatusResource statusResource = (JobStatusResource) Utils.getResource(ResourceType.JOB_STATUS, status);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return statusResource;
                default:
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    logger.error("Unsupported resource type for Job resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for Job resource.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        List<ExperimentCatResource> resourceList = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case JOB_STATUS:
                    generator = new QueryGenerator(JOB_STATUS);
                    generator.setParameter(JobStatusConstants.JOB_ID, jobId);
                    generator.setParameter(JobStatusConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            JobStatus jobStatus = (JobStatus) result;
                            JobStatusResource jobStatusResource =
                                    (JobStatusResource) Utils.getResource(ResourceType.JOB_STATUS, jobStatus);
                            resourceList.add(jobStatusResource);
                        }
                    }
                    break;
                default:
                    logger.error("Unsupported resource type for job resource.", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    
    public void save()  throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            JobPK jobPK = new JobPK();
            jobPK.setJobId(jobId);
            jobPK.setTaskId(taskId);
            Job existingJob = em.find(Job.class, jobPK);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            Job job;
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if(existingJob == null){
                job = new Job();
            }else {
                job = existingJob;
            }
            job.setJobId(jobId);
            job.setTaskId(taskId);
            job.setProcessId(processId);
            if (jobDescription != null) {
                job.setJobDescription(jobDescription.toCharArray());
            }
            if (stdOut != null) {
                job.setStdOut(stdOut.toCharArray());
            }
            if (stdErr != null) {
                job.setStdErr(stdErr.toCharArray());
            }
            job.setCreationTime(creationTime);
            job.setComputeResourceConsumed(computeResourceConsumed);
            job.setJobName(jobName);
            job.setWorkingDir(workingDir);
            job.setExitCode(exitCode);
            if (existingJob == null){
                em.persist(job);
            }else {
                em.merge(job);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<JobStatusResource> getJobStatuses() throws RegistryException{
        List<JobStatusResource> jobStatusResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.JOB_STATUS);
        for (ExperimentCatResource resource : resources) {
            JobStatusResource statusResource = (JobStatusResource) resource;
            jobStatusResources.add(statusResource);
        }
        return jobStatusResources;
    }

    public JobStatusResource getJobStatus() throws RegistryException{
        List<JobStatusResource> jobStatusResources = getJobStatuses();
        if(jobStatusResources.size() == 0){
            return null;
        }else{
            JobStatusResource max = jobStatusResources.get(0);
            for(int i=1; i<jobStatusResources.size();i++) {
                if (jobStatusResources.get(i).getTimeOfStateChange().after(max.getTimeOfStateChange())
                   || (jobStatusResources.get(i).getTimeOfStateChange().equals(max.getTimeOfStateChange()) && jobStatusResources.get(i).getState().equals(JobState.COMPLETE.toString()))
                   || (jobStatusResources.get(i).getTimeOfStateChange().equals(max.getTimeOfStateChange()) && jobStatusResources.get(i).getState().equals(JobState.FAILED.toString()))
                   || (jobStatusResources.get(i).getTimeOfStateChange().equals(max.getTimeOfStateChange()) && jobStatusResources.get(i).getState().equals(JobState.CANCELED.toString()))) {
                    max = jobStatusResources.get(i);
                }
            }
            return max;
        }
    }
}