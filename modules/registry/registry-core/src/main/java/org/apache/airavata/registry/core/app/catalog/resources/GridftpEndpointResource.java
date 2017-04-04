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
import org.apache.airavata.registry.core.app.catalog.model.GridftpDataMovement;
import org.apache.airavata.registry.core.app.catalog.model.GridftpEndpoint;
import org.apache.airavata.registry.core.app.catalog.model.GridftpEndpoint_PK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridftpEndpointResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(GridftpEndpointResource.class);
	private String endpoint;
	private String dataMovementInterfaceId;
	private GridftpDataMovementResource gridftpDataMovementResource;
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_ENDPOINT);
			generator.setParameter(GridftpEndpointConstants.ENDPOINT, ids.get(GridftpEndpointConstants.ENDPOINT));
			generator.setParameter(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID, ids.get(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID));
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_ENDPOINT);
			generator.setParameter(GridftpEndpointConstants.ENDPOINT, ids.get(GridftpEndpointConstants.ENDPOINT));
			generator.setParameter(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID, ids.get(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID));
			Query q = generator.selectQuery(em);
			GridftpEndpoint gridftpEndpoint = (GridftpEndpoint) q.getSingleResult();
			GridftpEndpointResource gridftpEndpointResource = (GridftpEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRIDFTP_ENDPOINT, gridftpEndpoint);
			em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return gridftpEndpointResource;
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
		List<AppCatalogResource> gridftpEndpointResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_ENDPOINT);
			Query q;
			if ((fieldName.equals(GridftpEndpointConstants.ENDPOINT)) || (fieldName.equals(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					GridftpEndpoint gridftpEndpoint = (GridftpEndpoint) result;
					GridftpEndpointResource gridftpEndpointResource = (GridftpEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRIDFTP_ENDPOINT, gridftpEndpoint);
					gridftpEndpointResources.add(gridftpEndpointResource);
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Gridftp Endpoint Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Gridftp Endpoint Resource.");
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
		return gridftpEndpointResources;
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
		List<String> gridftpEndpointResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GRIDFTP_ENDPOINT);
			Query q;
			if ((fieldName.equals(GridftpEndpointConstants.ENDPOINT)) || (fieldName.equals(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					GridftpEndpoint gridftpEndpoint = (GridftpEndpoint) result;
					GridftpEndpointResource gridftpEndpointResource = (GridftpEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GRIDFTP_ENDPOINT, gridftpEndpoint);
					gridftpEndpointResourceIDs.add(gridftpEndpointResource.getDataMovementInterfaceId());
				}
			} else {
				em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
				logger.error("Unsupported field name for Gridftp Endpoint Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Gridftp Endpoint Resource.");
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
		return gridftpEndpointResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			GridftpEndpoint existingGridftpEndpoint = em.find(GridftpEndpoint.class, new GridftpEndpoint_PK(endpoint, dataMovementInterfaceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

			GridftpEndpoint gridftpEndpoint;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingGridftpEndpoint == null) {
				gridftpEndpoint = new GridftpEndpoint();
                gridftpEndpoint.setCreationTime(AiravataUtils.getCurrentTimestamp());
			} else {
				gridftpEndpoint = existingGridftpEndpoint;
                gridftpEndpoint.setUpdateTime(AiravataUtils.getCurrentTimestamp());
			}
			gridftpEndpoint.setEndpoint(getEndpoint());
			gridftpEndpoint.setDataMovementInterfaceId(getDataMovementInterfaceId());
			GridftpDataMovement gridftpDataMovement = em.find(GridftpDataMovement.class, getDataMovementInterfaceId());
			gridftpEndpoint.setGridftpDataMovement(gridftpDataMovement);
			if (existingGridftpEndpoint == null) {
				em.persist(gridftpEndpoint);
			} else {
				em.merge(gridftpEndpoint);
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
			GridftpEndpoint gridftpEndpoint = em.find(GridftpEndpoint.class, new GridftpEndpoint_PK(ids.get(GridftpEndpointConstants.ENDPOINT), ids.get(GridftpEndpointConstants.DATA_MOVEMENT_INTERFACE_ID)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
			return gridftpEndpoint != null;
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
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public GridftpDataMovementResource getGridftpDataMovementResource() {
		return gridftpDataMovementResource;
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint=endpoint;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}
	
	public void setGridftpDataMovementResource(GridftpDataMovementResource gridftpDataMovementResource) {
		this.gridftpDataMovementResource=gridftpDataMovementResource;
	}
}