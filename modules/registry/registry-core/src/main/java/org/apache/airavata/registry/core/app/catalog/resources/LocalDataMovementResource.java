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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.LocalDataMovement;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDataMovementResource extends AbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(LocalDataMovementResource.class);
	private String dataMovementInterfaceId;
	
	@Override
	public void remove(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(LOCAL_DATA_MOVEMENT);
			generator.setParameter(LocalDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID, identifier);
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(LOCAL_DATA_MOVEMENT);
			generator.setParameter(LocalDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID, identifier);
			Query q = generator.selectQuery(em);
			LocalDataMovement localDataMovement = (LocalDataMovement) q.getSingleResult();
			LocalDataMovementResource localDataMovementResource = (LocalDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.LOCAL_DATA_MOVEMENT, localDataMovement);
			em.getTransaction().commit();
			em.close();
			return localDataMovementResource;
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
		List<Resource> localDataMovementResources = new ArrayList<Resource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(LOCAL_DATA_MOVEMENT);
			Query q;
			if ((fieldName.equals(LocalDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					LocalDataMovement localDataMovement = (LocalDataMovement) result;
					LocalDataMovementResource localDataMovementResource = (LocalDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.LOCAL_DATA_MOVEMENT, localDataMovement);
					localDataMovementResources.add(localDataMovementResource);
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Local Data Movement Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Local Data Movement Resource.");
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
		return localDataMovementResources;
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
		List<String> localDataMovementResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(LOCAL_DATA_MOVEMENT);
			Query q;
			if ((fieldName.equals(LocalDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					LocalDataMovement localDataMovement = (LocalDataMovement) result;
					LocalDataMovementResource localDataMovementResource = (LocalDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.LOCAL_DATA_MOVEMENT, localDataMovement);
					localDataMovementResourceIDs.add(localDataMovementResource.getDataMovementInterfaceId());
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Local Data Movement Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Local Data Movement Resource.");
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
		return localDataMovementResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			LocalDataMovement existingLocalDataMovement = em.find(LocalDataMovement.class, dataMovementInterfaceId);
			em.close();
			LocalDataMovement localDataMovement;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingLocalDataMovement == null) {
				localDataMovement = new LocalDataMovement();
			} else {
				localDataMovement = existingLocalDataMovement;
			}
			localDataMovement.setDataMovementInterfaceId(getDataMovementInterfaceId());
			if (existingLocalDataMovement == null) {
				em.persist(localDataMovement);
			} else {
				em.merge(localDataMovement);
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
			LocalDataMovement localDataMovement = em.find(LocalDataMovement.class, identifier);
			em.close();
			return localDataMovement != null;
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
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}
}
