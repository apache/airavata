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
import org.apache.airavata.registry.core.app.catalog.model.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageInterfaceResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(StorageInterfaceResource.class);
	private String storageResourceId;
	private StorageResourceResource storageResourceResource;
	private String dataMovementProtocol;
	private String dataMovementInterfaceId;
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

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_INTERFACE);
			generator.setParameter(StorageInterfaceConstants.STORAGE_RESOURCE_ID, ids.get(StorageInterfaceConstants.STORAGE_RESOURCE_ID));
			generator.setParameter(StorageInterfaceConstants.DATA_MOVEMENT_ID, ids.get(StorageInterfaceConstants.DATA_MOVEMENT_ID));
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_INTERFACE);
			generator.setParameter(StorageInterfaceConstants.STORAGE_RESOURCE_ID, ids.get(StorageInterfaceConstants.STORAGE_RESOURCE_ID));
			generator.setParameter(StorageInterfaceConstants.DATA_MOVEMENT_ID, ids.get(StorageInterfaceConstants.DATA_MOVEMENT_ID));
			Query q = generator.selectQuery(em);
			StorageInterface storageInterface = (StorageInterface) q.getSingleResult();
			StorageInterfaceResource storageInterfaceResource = (StorageInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_INTERFACE, storageInterface);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return storageInterfaceResource;
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
		List<AppCatalogResource> storageInterfaceResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_INTERFACE);
			Query q;
			if ((fieldName.equals(StorageInterfaceConstants.STORAGE_RESOURCE_ID)) || (fieldName.equals(StorageInterfaceConstants.DATA_MOVEMENT_PROTOCOL)) || (fieldName.equals(StorageInterfaceConstants.DATA_MOVEMENT_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					StorageInterface storageInterface = (StorageInterface) result;
					StorageInterfaceResource storageInterfaceResource = (StorageInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_INTERFACE, storageInterface);
					storageInterfaceResources.add(storageInterfaceResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Data Movement Interface Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Data Movement Interface Resource.");
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
		return storageInterfaceResources;
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
		List<String> storageInterfaceResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_INTERFACE);
			Query q;
			if ((fieldName.equals(StorageInterfaceConstants.STORAGE_RESOURCE_ID)) || (fieldName.equals(StorageInterfaceConstants.DATA_MOVEMENT_PROTOCOL)) || (fieldName.equals(StorageInterfaceConstants.DATA_MOVEMENT_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					StorageInterface storageInterface = (StorageInterface) result;
					StorageInterfaceResource storageInterfaceResource = (StorageInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_INTERFACE, storageInterface);
					storageInterfaceResourceIDs.add(storageInterfaceResource.getStorageResourceId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Storage Interface Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Storage Interface Resource.");
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
		return storageInterfaceResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			StorageInterface existingStorageInterface = em.find(StorageInterface.class, new StorageInterface_PK(storageResourceId, dataMovementInterfaceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			StorageInterface storageInterface;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingStorageInterface == null) {
				storageInterface = new StorageInterface();
                storageInterface.setCreationTime(AiravataUtils.getCurrentTimestamp());
			} else {
				storageInterface = existingStorageInterface;
                storageInterface.setUpdateTime(AiravataUtils.getCurrentTimestamp());
			}
			storageInterface.setStorageResourceId(getStorageResourceId());
			StorageResource storageResource = em.find(StorageResource.class, getStorageResourceId());
			storageInterface.setStorageResource(storageResource);
			storageInterface.setDataMovementProtocol(getDataMovementProtocol());
			storageInterface.setDataMovementInterfaceId(getDataMovementInterfaceId());
            storageInterface.setPriorityOrder(priorityOrder);
			if (existingStorageInterface == null) {
				em.persist(storageInterface);
			} else {
				em.merge(storageInterface);
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
			StorageInterface storageInterface = em.find(StorageInterface.class, new StorageInterface_PK(ids.get(StorageInterfaceConstants.STORAGE_RESOURCE_ID), ids.get(StorageInterfaceConstants.DATA_MOVEMENT_ID)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return storageInterface != null;
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
	
	public String getDataMovementProtocol() {
		return dataMovementProtocol;
	}
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public void setDataMovementProtocol(String dataMovementProtocol) {
		this.dataMovementProtocol=dataMovementProtocol;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public StorageResourceResource getStorageResourceResource() {
        return storageResourceResource;
    }

    public void setStorageResourceResource(StorageResourceResource storageResourceResource) {
        this.storageResourceResource = storageResourceResource;
    }
}
