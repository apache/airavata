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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResourceFileSystem;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResourceFileSystem_PK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeResourceFileSystemResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(ComputeResourceFileSystemResource.class);
	private String computeResourceId;
	private ComputeResourceResource computeHostResource;
	private String path;
	private String fileSystem;
	
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_FILE_SYSTEM);
			generator.setParameter(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID, ids.get(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID));
			generator.setParameter(ComputeResourceFileSystemConstants.FILE_SYSTEM, ids.get(ComputeResourceFileSystemConstants.FILE_SYSTEM));
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_FILE_SYSTEM);
			generator.setParameter(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID, ids.get(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID));
			generator.setParameter(ComputeResourceFileSystemConstants.FILE_SYSTEM, ids.get(ComputeResourceFileSystemConstants.FILE_SYSTEM));
			Query q = generator.selectQuery(em);
			ComputeResourceFileSystem computeResourceFileSystem = (ComputeResourceFileSystem) q.getSingleResult();
			ComputeResourceFileSystemResource computeResourceFileSystemResource = (ComputeResourceFileSystemResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE_FILE_SYSTEM, computeResourceFileSystem);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return computeResourceFileSystemResource;
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
		List<AppCatalogResource> computeResourceFileSystemResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_FILE_SYSTEM);
			Query q;
			if ((fieldName.equals(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(ComputeResourceFileSystemConstants.PATH)) || (fieldName.equals(ComputeResourceFileSystemConstants.FILE_SYSTEM))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					ComputeResourceFileSystem computeResourceFileSystem = (ComputeResourceFileSystem) result;
					ComputeResourceFileSystemResource computeResourceFileSystemResource = (ComputeResourceFileSystemResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE_FILE_SYSTEM, computeResourceFileSystem);
					computeResourceFileSystemResources.add(computeResourceFileSystemResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Compute Resource File System Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Compute Resource File System Resource.");
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
		return computeResourceFileSystemResources;
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
		List<String> computeResourceFileSystemResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_FILE_SYSTEM);
			Query q;
			if ((fieldName.equals(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(ComputeResourceFileSystemConstants.PATH)) || (fieldName.equals(ComputeResourceFileSystemConstants.FILE_SYSTEM))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					ComputeResourceFileSystem computeResourceFileSystem = (ComputeResourceFileSystem) result;
					ComputeResourceFileSystemResource computeResourceFileSystemResource = (ComputeResourceFileSystemResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE_FILE_SYSTEM, computeResourceFileSystem);
					computeResourceFileSystemResourceIDs.add(computeResourceFileSystemResource.getComputeResourceId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Compute Resource File System Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Compute Resource File System Resource.");
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
		return computeResourceFileSystemResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			ComputeResourceFileSystem existingComputeResourceFileSystem = em.find(ComputeResourceFileSystem.class, new ComputeResourceFileSystem_PK(computeResourceId, fileSystem));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			ComputeResourceFileSystem computeResourceFileSystem;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingComputeResourceFileSystem == null) {
				computeResourceFileSystem = new ComputeResourceFileSystem();
			} else {
				computeResourceFileSystem = existingComputeResourceFileSystem;
			}
			computeResourceFileSystem.setComputeResourceId(getComputeResourceId());
			ComputeResource computeResource = em.find(ComputeResource.class, getComputeResourceId());
			computeResourceFileSystem.setComputeResource(computeResource);
			computeResourceFileSystem.setPath(getPath());
			computeResourceFileSystem.setFileSystem(getFileSystem());
			if (existingComputeResourceFileSystem == null) {
				em.persist(computeResourceFileSystem);
			} else {
				em.merge(computeResourceFileSystem);
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
			ComputeResourceFileSystem computeResourceFileSystem = em.find(ComputeResourceFileSystem.class, new ComputeResourceFileSystem_PK(ids.get(ComputeResourceFileSystemConstants.COMPUTE_RESOURCE_ID), ids.get(ComputeResourceFileSystemConstants.FILE_SYSTEM)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return computeResourceFileSystem != null;
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
	
	public String getPath() {
		return path;
	}
	
	public String getFileSystem() {
		return fileSystem;
	}
	
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId=computeResourceId;
	}
	
	public void setComputeHostResource(ComputeResourceResource computeHostResource) {
		this.computeHostResource=computeHostResource;
	}
	
	public void setPath(String path) {
		this.path=path;
	}
	
	public void setFileSystem(String fileSystem) {
		this.fileSystem=fileSystem;
	}
}
