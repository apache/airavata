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
import org.apache.airavata.registry.core.app.catalog.model.*;
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

public class ParallelismPrefixCommandResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(ParallelismPrefixCommandResource.class);
	private String resourceJobManagerId;
	private ResourceJobManagerResource resourceJobManagerResource;
	private String commandType;
	private String command;
	
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PARALLELISM_PREFIX_COMMAND);
			generator.setParameter(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID, ids.get(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID));
			generator.setParameter(ParallelismCommandConstants.COMMAND_TYPE, ids.get(ParallelismCommandConstants.COMMAND_TYPE));
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PARALLELISM_PREFIX_COMMAND);
			generator.setParameter(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID, ids.get(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID));
			generator.setParameter(ParallelismCommandConstants.COMMAND_TYPE, ids.get(ParallelismCommandConstants.COMMAND_TYPE));
			Query q = generator.selectQuery(em);
			ParallelismPrefixCommand parallelismPrefixCommand = (ParallelismPrefixCommand) q.getSingleResult();
			ParallelismPrefixCommandResource prefixCommandResource = (ParallelismPrefixCommandResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.PARALLELISM_PREFIX_COMMAND, parallelismPrefixCommand);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return prefixCommandResource;
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
		List<AppCatalogResource> parallelismCommandResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PARALLELISM_PREFIX_COMMAND);
			Query q;
			if ((fieldName.equals(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(ParallelismCommandConstants.COMMAND_TYPE)) || (fieldName.equals(ParallelismCommandConstants.COMMAND))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					ParallelismPrefixCommand prefixCommand = (ParallelismPrefixCommand) result;
					ParallelismPrefixCommandResource parallelismPrefixCommandResource = (ParallelismPrefixCommandResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.PARALLELISM_PREFIX_COMMAND, prefixCommand);
					parallelismCommandResources.add(parallelismPrefixCommandResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Parallelism Command Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Parallelism Command Resource.");
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
		return parallelismCommandResources;
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
		List<String> parallelismCommandResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PARALLELISM_PREFIX_COMMAND);
			Query q;
			if ((fieldName.equals(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(ParallelismCommandConstants.COMMAND_TYPE)) || (fieldName.equals(ParallelismCommandConstants.COMMAND))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					ParallelismPrefixCommand parallelismPrefixCommand = (ParallelismPrefixCommand) result;
					ParallelismPrefixCommandResource parallelismPrefixCommandResource = (ParallelismPrefixCommandResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.PARALLELISM_PREFIX_COMMAND, parallelismPrefixCommand);
					parallelismCommandResourceIDs.add(parallelismPrefixCommandResource.getResourceJobManagerId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Parallelism Command Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Parallelism Command Resource.");
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
		return parallelismCommandResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			ParallelismPrefixCommand existingParallelismCommand = em.find(ParallelismPrefixCommand.class, new ParallelismPrefixCommand_PK(resourceJobManagerId, commandType));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

			ParallelismPrefixCommand prefixCommand;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingParallelismCommand == null) {
				prefixCommand = new ParallelismPrefixCommand();
			} else {
				prefixCommand = existingParallelismCommand;
			}
			prefixCommand.setResourceJobManagerId(getResourceJobManagerId());
			ResourceJobManager resourceJobManager = em.find(ResourceJobManager.class, getResourceJobManagerId());
			prefixCommand.setResourceJobManager(resourceJobManager);
			prefixCommand.setCommandType(getCommandType());
			prefixCommand.setCommand(getCommand());
			if (existingParallelismCommand == null) {
				em.persist(prefixCommand);
			} else {
				em.merge(prefixCommand);
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
			ParallelismPrefixCommand parallelismPrefixCommand = em.find(ParallelismPrefixCommand.class, new ParallelismPrefixCommand_PK(ids.get(ParallelismCommandConstants.RESOURCE_JOB_MANAGER_ID), ids.get(ParallelismCommandConstants.COMMAND_TYPE)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return parallelismPrefixCommand != null;
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
	
	public ResourceJobManagerResource getResourceJobManagerResource() {
		return resourceJobManagerResource;
	}
	
	public String getCommandType() {
		return commandType;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId=resourceJobManagerId;
	}
	
	public void setResourceJobManagerResource(ResourceJobManagerResource resourceJobManagerResource) {
		this.resourceJobManagerResource=resourceJobManagerResource;
	}
	
	public void setCommandType(String commandType) {
		this.commandType=commandType;
	}
	
	public void setCommand(String command) {
		this.command=command;
	}
}
