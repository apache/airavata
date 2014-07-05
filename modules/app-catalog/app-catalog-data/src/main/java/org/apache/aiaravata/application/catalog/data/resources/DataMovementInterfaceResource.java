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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.model.DataMovementInterface;
import org.apache.aiaravata.application.catalog.data.model.DataMovementInterface_PK;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMovementInterfaceResource extends AbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(DataMovementInterfaceResource.class);
	private String computeResourceId;
	private ComputeResourceResource computeHostResource;
	private String dataMovementProtocol;
	private String dataMovementInterfaceId;
	private int priorityOrder;
	
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_INTERFACE);
			generator.setParameter(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID, ids.get(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID));
			generator.setParameter(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID, ids.get(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID));
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
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_INTERFACE);
			generator.setParameter(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID, ids.get(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID));
			generator.setParameter(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID, ids.get(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID));
			Query q = generator.selectQuery(em);
			DataMovementInterface dataMovementInterface = (DataMovementInterface) q.getSingleResult();
			DataMovementInterfaceResource dataMovementInterfaceResource = (DataMovementInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_INTERFACE, dataMovementInterface);
			em.getTransaction().commit();
			em.close();
			return dataMovementInterfaceResource;
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
		List<Resource> dataMovementInterfaceResources = new ArrayList<Resource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_INTERFACE);
			Query q;
			if ((fieldName.equals(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(DataMovementInterfaceConstants.DATA_MOVEMENT_PROTOCOL)) || (fieldName.equals(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID)) || (fieldName.equals(DataMovementInterfaceConstants.PRIORITY_ORDER))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					DataMovementInterface dataMovementInterface = (DataMovementInterface) result;
					DataMovementInterfaceResource dataMovementInterfaceResource = (DataMovementInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_INTERFACE, dataMovementInterface);
					dataMovementInterfaceResources.add(dataMovementInterfaceResource);
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Data Movement Interface Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Data Movement Interface Resource.");
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
		return dataMovementInterfaceResources;
	}
	
	@Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> dataMovementInterfaceResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(DATA_MOVEMENT_INTERFACE);
			Query q;
			if ((fieldName.equals(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID)) || (fieldName.equals(DataMovementInterfaceConstants.DATA_MOVEMENT_PROTOCOL)) || (fieldName.equals(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID)) || (fieldName.equals(DataMovementInterfaceConstants.PRIORITY_ORDER))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					DataMovementInterface dataMovementInterface = (DataMovementInterface) result;
					DataMovementInterfaceResource dataMovementInterfaceResource = (DataMovementInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.DATA_MOVEMENT_INTERFACE, dataMovementInterface);
					dataMovementInterfaceResourceIDs.add(dataMovementInterfaceResource.getComputeResourceId());
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Data Movement Interface Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Data Movement Interface Resource.");
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
		return dataMovementInterfaceResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			DataMovementInterface existingDataMovementInterface = em.find(DataMovementInterface.class, new DataMovementInterface_PK(computeResourceId, dataMovementInterfaceId));
			em.close();
			DataMovementInterface dataMovementInterface;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingDataMovementInterface == null) {
				dataMovementInterface = new DataMovementInterface();
			} else {
				dataMovementInterface = existingDataMovementInterface;
			}
			dataMovementInterface.setComputeResourceId(getComputeResourceId());
			ComputeResource computeResource = em.find(ComputeResource.class, getComputeResourceId());
			dataMovementInterface.setComputeResource(computeResource);
			dataMovementInterface.setDataMovementProtocol(getDataMovementProtocol());
			dataMovementInterface.setDataMovementInterfaceId(getDataMovementInterfaceId());
			dataMovementInterface.setPriorityOrder(getPriorityOrder());
			if (existingDataMovementInterface == null) {
				em.persist(dataMovementInterface);
			} else {
				em.merge(dataMovementInterface);
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
			DataMovementInterface dataMovementInterface = em.find(DataMovementInterface.class, new DataMovementInterface_PK(ids.get(DataMovementInterfaceConstants.COMPUTE_RESOURCE_ID), ids.get(DataMovementInterfaceConstants.DATA_MOVEMENT_INTERFACE_ID)));
			em.close();
			return dataMovementInterface != null;
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
	
	public String getDataMovementProtocol() {
		return dataMovementProtocol;
	}
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public int getPriorityOrder() {
		return priorityOrder;
	}
	
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId=computeResourceId;
	}
	
	public void setComputeHostResource(ComputeResourceResource computeHostResource) {
		this.computeHostResource=computeHostResource;
	}
	
	public void setDataMovementProtocol(String dataMovementProtocol) {
		this.dataMovementProtocol=dataMovementProtocol;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}
	
	public void setPriorityOrder(int priorityOrder) {
		this.priorityOrder=priorityOrder;
	}
}
