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
import org.apache.airavata.registry.core.app.catalog.model.BatchQueue;
import org.apache.airavata.registry.core.app.catalog.model.BatchQueue_PK;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchQueueResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(BatchQueueResource.class);
	private String computeResourceId;
	private ComputeResourceResource computeHostResource;
	private int maxRuntime;
	private int maxJobInQueue;
	private String queueDescription;
	private String queueName;
	private int maxProcessors;
	private int maxNodes;
	private int maxMemory;
	private int cpuPerNode;
	private int defaultNodeCount;
	private int defaultCPUCount;
	private int defaultWalltime;
	private String queueSpecificMacros;
	private boolean isDefaultQueue;


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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(BATCH_QUEUE);
            generator.setParameter(BatchQueueConstants.COMPUTE_RESOURCE_ID, ids.get(BatchQueueConstants.COMPUTE_RESOURCE_ID));
            generator.setParameter(BatchQueueConstants.QUEUE_NAME, ids.get(BatchQueueConstants.QUEUE_NAME));
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(BATCH_QUEUE);
			generator.setParameter(BatchQueueConstants.COMPUTE_RESOURCE_ID, ids.get(BatchQueueConstants.COMPUTE_RESOURCE_ID));
			generator.setParameter(BatchQueueConstants.QUEUE_NAME, ids.get(BatchQueueConstants.QUEUE_NAME));
			Query q = generator.selectQuery(em);
			BatchQueue batchQueue = (BatchQueue) q.getSingleResult();
			BatchQueueResource batchQueueResource = (BatchQueueResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.BATCH_QUEUE, batchQueue);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return batchQueueResource;
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
		List<AppCatalogResource> batchQueueResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(BATCH_QUEUE);
			Query q;
			if ((fieldName.equals(BatchQueueConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(BatchQueueConstants.MAX_RUNTIME)) || (fieldName.equals(BatchQueueConstants.MAX_JOB_IN_QUEUE)) || (fieldName.equals(BatchQueueConstants.QUEUE_DESCRIPTION)) || (fieldName.equals(BatchQueueConstants.QUEUE_NAME)) || (fieldName.equals(BatchQueueConstants.MAX_PROCESSORS)) || (fieldName.equals(BatchQueueConstants.MAX_NODES))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					BatchQueue batchQueue = (BatchQueue) result;
					BatchQueueResource batchQueueResource = (BatchQueueResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.BATCH_QUEUE, batchQueue);
					batchQueueResources.add(batchQueueResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Batch Queue Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Batch Queue Resource.");
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
		return batchQueueResources;
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
		List<String> batchQueueResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(BATCH_QUEUE);
			Query q;
			if ((fieldName.equals(BatchQueueConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(BatchQueueConstants.MAX_RUNTIME)) || (fieldName.equals(BatchQueueConstants.MAX_JOB_IN_QUEUE)) || (fieldName.equals(BatchQueueConstants.QUEUE_DESCRIPTION)) || (fieldName.equals(BatchQueueConstants.QUEUE_NAME)) || (fieldName.equals(BatchQueueConstants.MAX_PROCESSORS)) || (fieldName.equals(BatchQueueConstants.MAX_NODES))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					BatchQueue batchQueue = (BatchQueue) result;
					BatchQueueResource batchQueueResource = (BatchQueueResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.BATCH_QUEUE, batchQueue);
					batchQueueResourceIDs.add(batchQueueResource.getComputeResourceId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Batch Queue Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Batch Queue Resource.");
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
		return batchQueueResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			BatchQueue existingBatchQueue = em.find(BatchQueue.class, new BatchQueue_PK(computeResourceId, queueName));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			BatchQueue batchQueue;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingBatchQueue == null) {
				batchQueue = new BatchQueue();
			} else {
				batchQueue = existingBatchQueue;
			}
			batchQueue.setComputeResourceId(getComputeResourceId());
			ComputeResource computeResource = em.find(ComputeResource.class, getComputeResourceId());
			batchQueue.setComputeResource(computeResource);
			batchQueue.setMaxRuntime(getMaxRuntime());
			batchQueue.setMaxJobInQueue(getMaxJobInQueue());
			batchQueue.setQueueDescription(getQueueDescription());
			batchQueue.setQueueName(getQueueName());
			batchQueue.setMaxProcessors(getMaxProcessors());
			batchQueue.setMaxNodes(getMaxNodes());
			batchQueue.setMaxMemory(getMaxMemory());
			batchQueue.setCpuPerNode(getCpuPerNode());
			batchQueue.setDefaultCPUCount(getDefaultCPUCount());
			batchQueue.setDefaultNodeCount(getDefaultNodeCount());
			batchQueue.setDefaultWalltime(getDefaultWalltime());
			batchQueue.setQueueSpecificMacros(getQueueSpecificMacros());
			batchQueue.setIsDefaultQueue(isDefaultQueue());
			if (existingBatchQueue == null) {
				em.persist(batchQueue);
			} else {
				em.merge(batchQueue);
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
			BatchQueue batchQueue = em.find(BatchQueue.class, new BatchQueue_PK(ids.get(BatchQueueConstants.COMPUTE_RESOURCE_ID), ids.get(BatchQueueConstants.QUEUE_NAME)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return batchQueue != null;
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
	
	public String getComputeResourceId() {
		return computeResourceId;
	}
	
	public ComputeResourceResource getComputeHostResource() {
		return computeHostResource;
	}
	
	public int getMaxRuntime() {
		return maxRuntime;
	}
	
	public int getMaxJobInQueue() {
		return maxJobInQueue;
	}
	
	public String getQueueDescription() {
		return queueDescription;
	}
	
	public String getQueueName() {
		return queueName;
	}
	
	public int getMaxProcessors() {
		return maxProcessors;
	}
	
	public int getMaxNodes() {
		return maxNodes;
	}
	
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId=computeResourceId;
	}
	
	public void setComputeHostResource(ComputeResourceResource computeHostResource) {
		this.computeHostResource=computeHostResource;
	}
	
	public void setMaxRuntime(int maxRuntime) {
		this.maxRuntime=maxRuntime;
	}
	
	public void setMaxJobInQueue(int maxJobInQueue) {
		this.maxJobInQueue=maxJobInQueue;
	}
	
	public void setQueueDescription(String queueDescription) {
		this.queueDescription=queueDescription;
	}
	
	public void setQueueName(String queueName) {
		this.queueName=queueName;
	}
	
	public void setMaxProcessors(int maxProcessors) {
		this.maxProcessors=maxProcessors;
	}
	
	public void setMaxNodes(int maxNodes) {
		this.maxNodes=maxNodes;
	}

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

	public int getCpuPerNode() {
		return cpuPerNode;
	}

	public void setCpuPerNode(int cpuPerNode) {
		this.cpuPerNode = cpuPerNode;
	}

	public int getDefaultNodeCount() {
		return defaultNodeCount;
	}

	public void setDefaultNodeCount(int defaultNodeCount) {
		this.defaultNodeCount = defaultNodeCount;
	}

	public int getDefaultCPUCount() {
		return defaultCPUCount;
	}

	public void setDefaultCPUCount(int defaultCPUCount) {
		this.defaultCPUCount = defaultCPUCount;
	}

	public boolean isDefaultQueue() {
		return isDefaultQueue;
	}

	public void setIsDefaultQueue(boolean isDefaultQueue) {
		this.isDefaultQueue = isDefaultQueue;
	}

	public int getDefaultWalltime() {
		return defaultWalltime;
	}

	public void setDefaultWalltime(int defaultWalltime) {
		this.defaultWalltime = defaultWalltime;
	}

	public String getQueueSpecificMacros() {
		return queueSpecificMacros;
	}

	public void setQueueSpecificMacros(String queueSpecificMacros) {
		this.queueSpecificMacros = queueSpecificMacros;
	}
}