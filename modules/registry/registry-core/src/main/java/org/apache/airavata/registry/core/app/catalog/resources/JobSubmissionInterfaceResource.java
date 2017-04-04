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
package org.apache.airavata.registry.core.app.catalog.resources;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
import org.apache.airavata.registry.core.app.catalog.model.JobSubmissionInterface;
import org.apache.airavata.registry.core.app.catalog.model.JobSubmissionInterface_PK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSubmissionInterfaceResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(JobSubmissionInterfaceResource.class);
	private String jobSubmissionInterfaceId;
	private String computeResourceId;
	private ComputeResourceResource computeHostResource;
	private String jobSubmissionProtocol;
	private int priorityOrder;
    private Timestamp createdTime;
    private Timestamp updatedTime;

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }
	
	@Override
	public void remove(Object identifier) throws AppCatalogException {
		HashMap<String, String> ids;
		if (identifier instanceof Map) {
			ids = (HashMap<String, String>) identifier;
		} else {
			logger.error("Identifier should be a map with the field name and it's value");
			throw new AppCatalogException("Identifier should be a map with the field name and it's value");
		}
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_INTERFACE);
			generator.setParameter(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID, ids.get(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID));
			generator.setParameter(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID, ids.get(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID));
			Query q = generator.deleteQuery(em);
			q.executeUpdate();
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@Override
	public AppCatalogResource get(Object identifier) throws AppCatalogException {
		HashMap<String, String> ids;
		if (identifier instanceof Map) {
			ids = (HashMap<String, String>) identifier;
		} else {
			logger.error("Identifier should be a map with the field name and it's value");
			throw new AppCatalogException("Identifier should be a map with the field name and it's value");
		}
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_INTERFACE);
			generator.setParameter(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID, ids.get(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID));
			generator.setParameter(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID, ids.get(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID));
			Query q = generator.selectQuery(em);
			JobSubmissionInterface jobSubmissionInterface = (JobSubmissionInterface) q.getSingleResult();
			JobSubmissionInterfaceResource jobSubmissionInterfaceResource = (JobSubmissionInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_INTERFACE, jobSubmissionInterface);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return jobSubmissionInterfaceResource;
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@Override
	public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
		List<AppCatalogResource> jobSubmissionInterfaceResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_INTERFACE);
			Query q;
			if ((fieldName.equals(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID)) || (fieldName.equals(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(JobSubmissionInterfaceConstants.JOB_SUBMISSION_PROTOCOL)) || (fieldName.equals(JobSubmissionInterfaceConstants.PRIORITY_ORDER))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					JobSubmissionInterface jobSubmissionInterface = (JobSubmissionInterface) result;
					JobSubmissionInterfaceResource jobSubmissionInterfaceResource = (JobSubmissionInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_INTERFACE, jobSubmissionInterface);
					jobSubmissionInterfaceResources.add(jobSubmissionInterfaceResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Job Submission Interface Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Job Submission Interface Resource.");
			}
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
		return jobSubmissionInterfaceResources;
	}

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    @Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> jobSubmissionInterfaceResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(JOB_SUBMISSION_INTERFACE);
			Query q;
			if ((fieldName.equals(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID)) || (fieldName.equals(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(JobSubmissionInterfaceConstants.JOB_SUBMISSION_PROTOCOL)) || (fieldName.equals(JobSubmissionInterfaceConstants.PRIORITY_ORDER))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					JobSubmissionInterface jobSubmissionInterface = (JobSubmissionInterface) result;
					JobSubmissionInterfaceResource jobSubmissionInterfaceResource = (JobSubmissionInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.JOB_SUBMISSION_INTERFACE, jobSubmissionInterface);
					jobSubmissionInterfaceResourceIDs.add(jobSubmissionInterfaceResource.getComputeResourceId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Job Submission Interface Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Job Submission Interface Resource.");
			}
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
		return jobSubmissionInterfaceResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			JobSubmissionInterface existingJobSubmissionInterface = em.find(JobSubmissionInterface.class, new JobSubmissionInterface_PK(jobSubmissionInterfaceId, computeResourceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			JobSubmissionInterface jobSubmissionInterface;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingJobSubmissionInterface == null) {
				jobSubmissionInterface = new JobSubmissionInterface();
                jobSubmissionInterface.setCreationTime(AiravataUtils.getCurrentTimestamp());
			} else {
				jobSubmissionInterface = existingJobSubmissionInterface;
                jobSubmissionInterface.setUpdateTime(AiravataUtils.getCurrentTimestamp());
			}
			jobSubmissionInterface.setJobSubmissionInterfaceId(getJobSubmissionInterfaceId());
			jobSubmissionInterface.setComputeResourceId(getComputeResourceId());
			ComputeResource computeResource = em.find(ComputeResource.class, getComputeResourceId());
			jobSubmissionInterface.setComputeResource(computeResource);
			jobSubmissionInterface.setJobSubmissionProtocol(getJobSubmissionProtocol());
			jobSubmissionInterface.setPriorityOrder(getPriorityOrder());
			if (existingJobSubmissionInterface == null) {
				em.persist(jobSubmissionInterface);
			} else {
				em.merge(jobSubmissionInterface);
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
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@Override
	public boolean isExists(Object identifier) throws AppCatalogException {
		HashMap<String, String> ids;
		if (identifier instanceof Map) {
			ids = (HashMap<String, String>) identifier;
		} else {
			logger.error("Identifier should be a map with the field name and it's value");
			throw new AppCatalogException("Identifier should be a map with the field name and it's value");
		}
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			JobSubmissionInterface jobSubmissionInterface = em.find(JobSubmissionInterface.class, new JobSubmissionInterface_PK(ids.get(JobSubmissionInterfaceConstants.JOB_SUBMISSION_INTERFACE_ID), ids.get(JobSubmissionInterfaceConstants.COMPUTE_RESOURCE_ID)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return jobSubmissionInterface != null;
		} catch (ApplicationSettingsException e) {
			logger.error(e.getMessage(), e);
			throw new AppCatalogException(e);
		} finally {
			if (em != null && em.isOpen()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	public String getJobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}
	
	public String getComputeResourceId() {
		return computeResourceId;
	}
	
	public ComputeResourceResource getComputeHostResource() {
		return computeHostResource;
	}
	
	public String getJobSubmissionProtocol() {
		return jobSubmissionProtocol;
	}
	
	public int getPriorityOrder() {
		return priorityOrder;
	}
	
	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId=jobSubmissionInterfaceId;
	}
	
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId=computeResourceId;
	}
	
	public void setComputeHostResource(ComputeResourceResource computeHostResource) {
		this.computeHostResource=computeHostResource;
	}
	
	public void setJobSubmissionProtocol(String jobSubmissionProtocol) {
		this.jobSubmissionProtocol=jobSubmissionProtocol;
	}
	
	public void setPriorityOrder(int priorityOrder) {
		this.priorityOrder=priorityOrder;
	}
}