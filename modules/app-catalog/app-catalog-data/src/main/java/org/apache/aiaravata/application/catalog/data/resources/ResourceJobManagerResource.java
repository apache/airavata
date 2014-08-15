/*
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
 *
 */

package org.apache.aiaravata.application.catalog.data.resources;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ResourceJobManager;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceJobManagerResource extends AbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(ResourceJobManagerResource.class);
	private String resourceJobManagerId;
	private String pushMonitoringEndpoint;
	private String jobManagerBinPath;
	private String resourceJobManagerType;
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
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(RESOURCE_JOB_MANAGER);
			generator.setParameter(ResourceJobManagerConstants.RESOURCE_JOB_MANAGER_ID, identifier);
			Query q = generator.deleteQuery(em);
			q.executeUpdate();
			em.getTransaction().commit();
			em.close();
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
	public Resource get(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(RESOURCE_JOB_MANAGER);
			generator.setParameter(ResourceJobManagerConstants.RESOURCE_JOB_MANAGER_ID, identifier);
			Query q = generator.selectQuery(em);
			ResourceJobManager resourceJobManager = (ResourceJobManager) q.getSingleResult();
			ResourceJobManagerResource resourceJobManagerResource = (ResourceJobManagerResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.RESOURCE_JOB_MANAGER, resourceJobManager);
			em.getTransaction().commit();
			em.close();
			return resourceJobManagerResource;
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
	public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
		List<Resource> resourceJobManagerResources = new ArrayList<Resource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(RESOURCE_JOB_MANAGER);
			Query q;
			if ((fieldName.equals(ResourceJobManagerConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(ResourceJobManagerConstants.PUSH_MONITORING_ENDPOINT)) || (fieldName.equals(ResourceJobManagerConstants.JOB_MANAGER_BIN_PATH)) || (fieldName.equals(ResourceJobManagerConstants.RESOURCE_JOB_MANAGER_TYPE))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					ResourceJobManager resourceJobManager = (ResourceJobManager) result;
					ResourceJobManagerResource resourceJobManagerResource = (ResourceJobManagerResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.RESOURCE_JOB_MANAGER, resourceJobManager);
					resourceJobManagerResources.add(resourceJobManagerResource);
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Resource Job Manager Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Resource Job Manager Resource.");
			}
			em.getTransaction().commit();
			em.close();
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
		return resourceJobManagerResources;
	}

    @Override
    public List<Resource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    @Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> resourceJobManagerResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(RESOURCE_JOB_MANAGER);
			Query q;
			if ((fieldName.equals(ResourceJobManagerConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(ResourceJobManagerConstants.PUSH_MONITORING_ENDPOINT)) || (fieldName.equals(ResourceJobManagerConstants.JOB_MANAGER_BIN_PATH)) || (fieldName.equals(ResourceJobManagerConstants.RESOURCE_JOB_MANAGER_TYPE))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					ResourceJobManager resourceJobManager = (ResourceJobManager) result;
					ResourceJobManagerResource resourceJobManagerResource = (ResourceJobManagerResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.RESOURCE_JOB_MANAGER, resourceJobManager);
					resourceJobManagerResourceIDs.add(resourceJobManagerResource.getResourceJobManagerId());
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Resource Job Manager Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Resource Job Manager Resource.");
			}
			em.getTransaction().commit();
			em.close();
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
		return resourceJobManagerResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			ResourceJobManager existingResourceJobManager = em.find(ResourceJobManager.class, resourceJobManagerId);
			em.close();
			ResourceJobManager resourceJobManager;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingResourceJobManager == null) {
				resourceJobManager = new ResourceJobManager();
                resourceJobManager.setCreationTime(createdTime);
			} else {
				resourceJobManager = existingResourceJobManager;
                resourceJobManager.setUpdateTime(updatedTime);
			}
			resourceJobManager.setResourceJobManagerId(getResourceJobManagerId());
			resourceJobManager.setPushMonitoringEndpoint(getPushMonitoringEndpoint());
			resourceJobManager.setJobManagerBinPath(getJobManagerBinPath());
			resourceJobManager.setResourceJobManagerType(getResourceJobManagerType());
			if (existingResourceJobManager == null) {
				em.persist(resourceJobManager);
			} else {
				em.merge(resourceJobManager);
			}
			em.getTransaction().commit();
			em.close();
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
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			ResourceJobManager resourceJobManager = em.find(ResourceJobManager.class, identifier);
			em.close();
			return resourceJobManager != null;
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
	
	public String getResourceJobManagerId() {
		return resourceJobManagerId;
	}
	
	public String getPushMonitoringEndpoint() {
		return pushMonitoringEndpoint;
	}
	
	public String getJobManagerBinPath() {
		return jobManagerBinPath;
	}
	
	public String getResourceJobManagerType() {
		return resourceJobManagerType;
	}
	
	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId=resourceJobManagerId;
	}
	
	public void setPushMonitoringEndpoint(String pushMonitoringEndpoint) {
		this.pushMonitoringEndpoint=pushMonitoringEndpoint;
	}
	
	public void setJobManagerBinPath(String jobManagerBinPath) {
		this.jobManagerBinPath=jobManagerBinPath;
	}
	
	public void setResourceJobManagerType(String resourceJobManagerType) {
		this.resourceJobManagerType=resourceJobManagerType;
	}
}
