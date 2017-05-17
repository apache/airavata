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
import org.apache.airavata.registry.core.app.catalog.model.UnicoreDataMovement;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class UnicoreDataMovementResource extends AppCatAbstractResource {
	
	private final static Logger logger = LoggerFactory.getLogger(UnicoreDataMovementResource.class);
	
	private String dataMovementId;
	private String securityProtocol;
	private String unicoreEndpointUrl;

	 public void remove(Object identifier) throws AppCatalogException {
	        EntityManager em = null;
	        try {
	            em = AppCatalogJPAUtils.getEntityManager();
	            em.getTransaction().begin();
	            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(UNICORE_DATA_MOVEMENT);
	            generator.setParameter(UnicoreDataMovementConstants.DATAMOVEMENT_ID, identifier);
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

	 public AppCatalogResource get(Object identifier) throws AppCatalogException {
		 EntityManager em = null;
	        try {
	            em = AppCatalogJPAUtils.getEntityManager();
	            em.getTransaction().begin();
	            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(UNICORE_DATA_MOVEMENT);
	            generator.setParameter(UnicoreDataMovementConstants.DATAMOVEMENT_ID, identifier);
	            Query q = generator.selectQuery(em);
	            UnicoreDataMovement unicoreDataMovement = (UnicoreDataMovement) q.getSingleResult();
	            UnicoreDataMovementResource dataMovementResource =
	            			(UnicoreDataMovementResource) AppCatalogJPAUtils
	            			.getResource(AppCatalogResourceType.UNICORE_DATA_MOVEMENT,
							unicoreDataMovement);
	            em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
	            return dataMovementResource;
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
	 

	    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
	        List<AppCatalogResource> unicoreDMResourceList = new ArrayList<AppCatalogResource>();
	        EntityManager em = null;
	        try {
	            em = AppCatalogJPAUtils.getEntityManager();
	            em.getTransaction().begin();
	            Query q;
	            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(UNICORE_DATA_MOVEMENT);
	            List results;
	            if (fieldName.equals(UnicoreDataMovementConstants.UNICORE_ENDPOINT_URL)) {
	                generator.setParameter(UnicoreDataMovementConstants.UNICORE_ENDPOINT_URL, value);
	                q = generator.selectQuery(em);
	                results = q.getResultList();
	                if (results.size() != 0) {
	                    for (Object result : results) {
	                        UnicoreDataMovement dataMovement = (UnicoreDataMovement) result;
	                        UnicoreDataMovementResource unicoreJobSubmissionResource =
	                                (UnicoreDataMovementResource) AppCatalogJPAUtils.getResource(
	                                        AppCatalogResourceType.UNICORE_DATA_MOVEMENT, dataMovement);
	                        unicoreDMResourceList.add(unicoreJobSubmissionResource);
	                    }
	                }
	            } else if (fieldName.equals(UnicoreDataMovementConstants.SECURITY_PROTOCAL)) {
	                generator.setParameter(UnicoreDataMovementConstants.SECURITY_PROTOCAL, value);
	                q = generator.selectQuery(em);
	                results = q.getResultList();
	                if (results.size() != 0) {
	                    for (Object result : results) {
	                        UnicoreDataMovement dataMovement = (UnicoreDataMovement) result;
	                        UnicoreDataMovementResource dataMovementResource =
	                                (UnicoreDataMovementResource) AppCatalogJPAUtils.getResource(
	                                        AppCatalogResourceType.UNICORE_DATA_MOVEMENT, dataMovement);
	                        unicoreDMResourceList.add(dataMovementResource);
	                    }
	                }
	            } else {
	                em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
	                logger.error("Unsupported field name for Unicore data movement resource.", new IllegalArgumentException());
	                throw new IllegalArgumentException("Unsupported field name for Unicore data movement resource.");
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
	        return unicoreDMResourceList;
	    }

	@Override
	public List<AppCatalogResource> getAll() throws AppCatalogException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllIds() throws AppCatalogException {
		// TODO Auto-generated method stub
		return null;
	}

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        return null;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            UnicoreDataMovement existingDataMovement = em.find(UnicoreDataMovement.class, dataMovementId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingDataMovement != null) {
                existingDataMovement.setDataMovementId(dataMovementId);;
                existingDataMovement.setUnicoreEndpointUrl(unicoreEndpointUrl);
                existingDataMovement.setSecurityProtocol(securityProtocol);
                em.merge(existingDataMovement);
            } else {
                UnicoreDataMovement unicoreJobSubmission = new UnicoreDataMovement();
                unicoreJobSubmission.setDataMovementId(dataMovementId);
                unicoreJobSubmission.setUnicoreEndpointUrl(unicoreEndpointUrl);
                unicoreJobSubmission.setSecurityProtocol(securityProtocol);
                em.persist(unicoreJobSubmission);
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

    public boolean isExists(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            UnicoreDataMovement dataMovement = em.find(UnicoreDataMovement.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return dataMovement != null;
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


    public String getDataMovementId() {
        return dataMovementId;
    }

    public void setDataMovementId(String dataMovementId) {
        this.dataMovementId = dataMovementId;
    }

    public String getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}

	public String getUnicoreEndpointUrl() {
		return unicoreEndpointUrl;
	}

	public void setUnicoreEndpointUrl(String unicoreEndpointUrl) {
		this.unicoreEndpointUrl = unicoreEndpointUrl;
	}
	
	
}
