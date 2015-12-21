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

package org.apache.airavata.registry.core.app.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
import org.apache.airavata.registry.core.app.catalog.model.GatewayClientCredential;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.Gateway;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GatewayClientCredentialResource extends AppCatAbstractResource {
	private final static Logger logger = LoggerFactory.getLogger(GatewayClientCredentialResource.class);
	private String clientKey;
	private String clientSecret;
	private String gatewayId;
	private String userName;

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public void remove(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_CLIENT_CREDENTIAL);
			generator.setParameter(GatewayClientCredentialConstants.CLIENT_KEY, identifier);
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
	public AppCatalogResource get(Object identifier) throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_CLIENT_CREDENTIAL);
			generator.setParameter(GatewayClientCredentialConstants.CLIENT_KEY, identifier);
			Query q = generator.selectQuery(em);
			GatewayClientCredential gatewayClientCredential = (GatewayClientCredential) q.getSingleResult();
			GatewayClientCredentialResource gatewayClientCredentialResource = (GatewayClientCredentialResource)
					AppCatalogJPAUtils.getResource(AppCatalogResourceType.GATEWAY_CLIENT_CREDENTIAL, gatewayClientCredential);
			em.getTransaction().commit();
			em.close();
			return gatewayClientCredentialResource;
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
		List<AppCatalogResource> gatewayClientCredentialResources = new ArrayList<AppCatalogResource>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_CLIENT_CREDENTIAL);
			Query q;
			if ((fieldName.equals(GatewayClientCredentialConstants.GATEWAY_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					GatewayClientCredential gatewayClientCredential = (GatewayClientCredential) result;
					GatewayClientCredentialResource gatewayClientCredentialResource = (GatewayClientCredentialResource)
							AppCatalogJPAUtils.getResource(AppCatalogResourceType.GATEWAY_CLIENT_CREDENTIAL, gatewayClientCredential);
					gatewayClientCredentialResources.add(gatewayClientCredentialResource);
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Gateway Client Credential Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Gateway Client Credential Resource.");
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
		return gatewayClientCredentialResources;
	}

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> computeResourceResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE);
            Query q = generator.selectQuery(em);
            List<?> results = q.getResultList();
            for (Object result : results) {
                ComputeResource computeResource = (ComputeResource) result;
                GatewayClientCredentialResource computeResourceResource = (GatewayClientCredentialResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE, computeResource);
                computeResourceResources.add(computeResourceResource);
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
        return computeResourceResources;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        List<String> gatewayClientCredentials = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_CLIENT_CREDENTIAL);
            Query q = generator.selectQuery(em);
            List<?> results = q.getResultList();
            for (Object result : results) {
                GatewayClientCredential gatewayClientCredential = (GatewayClientCredential) result;
                gatewayClientCredentials.add(gatewayClientCredential.getClientKey());
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
        return gatewayClientCredentials;
    }

    @Override
	public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
		List<String> computeResourceResourceIDs = new ArrayList<String>();
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GATEWAY_CLIENT_CREDENTIAL);
			Query q;
			if ((fieldName.equals(GatewayClientCredentialConstants.GATEWAY_ID))) {
				generator.setParameter(fieldName, value);
				q = generator.selectQuery(em);
				List<?> results = q.getResultList();
				for (Object result : results) {
					GatewayClientCredential gatewayClientCredential = (GatewayClientCredential) result;
					computeResourceResourceIDs.add(gatewayClientCredential.getClientKey());
				}
			} else {
				em.getTransaction().commit();
					em.close();
				logger.error("Unsupported field name for Gateway Client Credential Resource.", new IllegalArgumentException());
				throw new IllegalArgumentException("Unsupported field name for Gateway Client Credential Resource.");
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
		return computeResourceResourceIDs;
	}
	
	@Override
	public void save() throws AppCatalogException {
		EntityManager em = null;
		try {
			em = AppCatalogJPAUtils.getEntityManager();
			GatewayClientCredential existingGatewayClientCredential = em.find(GatewayClientCredential.class, clientKey);
			em.close();
			GatewayClientCredential gatewayClientCredential;
			em = AppCatalogJPAUtils.getEntityManager();
			em.getTransaction().begin();
			if (existingGatewayClientCredential == null) {
				gatewayClientCredential = new GatewayClientCredential();
			} else {
				gatewayClientCredential = existingGatewayClientCredential;
			}
			gatewayClientCredential.setClientKey(clientKey);
			gatewayClientCredential.setClientSecret(clientSecret);
			gatewayClientCredential.setGatewayId(gatewayId);
			if (existingGatewayClientCredential == null) {
				em.persist(gatewayClientCredential);
			} else {
				em.merge(gatewayClientCredential);
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
			GatewayClientCredential gatewayClientCredential = em.find(GatewayClientCredential.class, identifier);
			em.close();
			return gatewayClientCredential != null;
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
}