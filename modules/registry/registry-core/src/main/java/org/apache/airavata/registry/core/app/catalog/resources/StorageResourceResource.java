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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.StorageResource;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StorageResourceResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(StorageResourceResource.class);
	private String resourceDescription;
	private String storageResourceId;
	private String hostName;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private boolean enabled;

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

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
	public void remove(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_RESOURCE);
			generator.setParameter(StorageResourceConstants.RESOURCE_ID, identifier);
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
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_RESOURCE);
			generator.setParameter(StorageResourceConstants.RESOURCE_ID, identifier);
			Query q = generator.selectQuery(em);
			StorageResource storageResource = (StorageResource) q.getSingleResult();
			StorageResourceResource storageResourceResource = (StorageResourceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_RESOURCE, storageResource);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return storageResourceResource;
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
		List<AppCatalogResource> storageResourceResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_RESOURCE);
			Query q;
			if ((fieldName.equals(StorageResourceConstants.RESOURCE_DESCRIPTION)) || (fieldName.equals(StorageResourceConstants.RESOURCE_ID)) || (fieldName.equals(StorageResourceConstants.HOST_NAME))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					StorageResource storageResource = (StorageResource) result;
					StorageResourceResource storageResourceResource = (StorageResourceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_RESOURCE, storageResource);
					storageResourceResources.add(storageResourceResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Storage Resource Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Storage Resource Resource.");
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
		return storageResourceResources;
	}

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> storageResourceResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_RESOURCE);
            Query q = generator.selectQuery(em);
            List<?> results = q.getResultList();
            for (Object result : results) {
                StorageResource storageResource = (StorageResource) result;
                StorageResourceResource storageResourceResource = (StorageResourceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_RESOURCE, storageResource);
                storageResourceResources.add(storageResourceResource);
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
        return storageResourceResources;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        List<String> storageResourceResources = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_RESOURCE);
            Query q = generator.selectQuery(em);
            List<?> results = q.getResultList();
            for (Object result : results) {
                StorageResource storageResource = (StorageResource) result;
				storageResourceResources.add(storageResource.getStorageResourceId());
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
        return storageResourceResources;
    }

    @Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> storageResourceResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_RESOURCE);
			Query q;
			if ((fieldName.equals(StorageResourceConstants.RESOURCE_DESCRIPTION)) || (fieldName.equals(StorageResourceConstants.RESOURCE_ID)) || (fieldName.equals(StorageResourceConstants.HOST_NAME))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					StorageResource storageResource = (StorageResource) result;
					StorageResourceResource storageResourceResource = (StorageResourceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_RESOURCE, storageResource);
					storageResourceResourceIDs.add(storageResourceResource.getStorageResourceId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Storage Resource Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Storage Resource Resource.");
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
		return storageResourceResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			StorageResource existingStorageResource = em.find(StorageResource.class, storageResourceId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			StorageResource storageResource;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingStorageResource == null) {
				storageResource = new StorageResource();
                storageResource.setCreationTime(AiravataUtils.getCurrentTimestamp());
			} else {
				storageResource = existingStorageResource;
                storageResource.setUpdateTime(AiravataUtils.getCurrentTimestamp());
			}
			storageResource.setDescription(getResourceDescription());
			storageResource.setStorageResourceId(getStorageResourceId());
			storageResource.setHostName(getHostName());
            storageResource.setEnabled(isEnabled());
			if (existingStorageResource == null) {
				em.persist(storageResource);
			} else {
				em.merge(storageResource);
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
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			StorageResource storageResource = em.find(StorageResource.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return storageResource != null;
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
	
	public String getResourceDescription() {
		return resourceDescription;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription=resourceDescription;
	}
	
	public void setHostName(String hostName) {
		this.hostName=hostName;
	}

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }
}