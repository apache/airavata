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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.registry.core.app.catalog.model.GlobusJobSubmission;
import org.apache.airavata.registry.core.app.catalog.model.UnicoreJobSubmission;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicoreJobSubmissionResource extends AppCatAbstractResource {
	
	private final static Logger logger = LoggerFactory.getLogger(UnicoreJobSubmissionResource.class);
	
	private String jobSubmissionInterfaceId;
	private String securityProtocol;
	private String unicoreEndpointUrl;

	public void remove(Object identifier) throws AppCatalogException {
	        EntityManager em = null;
	        try {
	            em = AppCatalogJPAUtils.getEntityManager();
	            em.getTransaction().begin();
	            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(UNICORE_JOB_SUBMISSION);
	            generator.setParameter(UnicoreJobSubmissionConstants.SUBMISSION_ID, identifier);
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
		 HashMap<String, String> ids;
		 EntityManager em = null;
	        try {
	            em = AppCatalogJPAUtils.getEntityManager();
	            em.getTransaction().begin();
	            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(UNICORE_JOB_SUBMISSION);
	            generator.setParameter(UnicoreJobSubmissionConstants.SUBMISSION_ID, identifier);
	            Query q = generator.selectQuery(em);
	            UnicoreJobSubmission unicoreJobSubmission = (UnicoreJobSubmission) q.getSingleResult();
	            UnicoreJobSubmissionResource unicoreSubmissionResource =
	            			(UnicoreJobSubmissionResource) AppCatalogJPAUtils
	            			.getResource(AppCatalogResourceType.UNICORE_JOB_SUBMISSION,
							unicoreJobSubmission);
	            em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
	            return unicoreSubmissionResource;
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
	        List<AppCatalogResource> unicoreSubmissionResourceList = new ArrayList<AppCatalogResource>();
	        EntityManager em = null;
	        try {
	            em = AppCatalogJPAUtils.getEntityManager();
	            em.getTransaction().begin();
	            Query q;
	            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(UNICORE_JOB_SUBMISSION);
	            List results;
	            if (fieldName.equals(UnicoreJobSubmissionConstants.UNICORE_ENDPOINT_URL)) {
	                generator.setParameter(UnicoreJobSubmissionConstants.UNICORE_ENDPOINT_URL, value);
	                q = generator.selectQuery(em);
	                results = q.getResultList();
	                if (results.size() != 0) {
	                    for (Object result : results) {
	                        UnicoreJobSubmission unicoreJobSubmission = (UnicoreJobSubmission) result;
	                        UnicoreJobSubmissionResource unicoreJobSubmissionResource =
	                                (UnicoreJobSubmissionResource) AppCatalogJPAUtils.getResource(
	                                        AppCatalogResourceType.UNICORE_JOB_SUBMISSION, unicoreJobSubmission);
	                        unicoreSubmissionResourceList.add(unicoreJobSubmissionResource);
	                    }
	                }
	            } else if (fieldName.equals(UnicoreJobSubmissionConstants.SECURITY_PROTOCAL)) {
	                generator.setParameter(UnicoreJobSubmissionConstants.SECURITY_PROTOCAL, value);
	                q = generator.selectQuery(em);
	                results = q.getResultList();
	                if (results.size() != 0) {
	                    for (Object result : results) {
	                        UnicoreJobSubmission unicoreJobSubmission = (UnicoreJobSubmission) result;
	                        UnicoreJobSubmissionResource unicoreJobSubmissionResource =
	                                (UnicoreJobSubmissionResource) AppCatalogJPAUtils.getResource(
	                                        AppCatalogResourceType.UNICORE_JOB_SUBMISSION, unicoreJobSubmission);
	                        unicoreSubmissionResourceList.add(unicoreJobSubmissionResource);
	                    }
	                }
	            }        
	            else {
	                em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
	                logger.error("Unsupported field name for Unicore submission resource.", new IllegalArgumentException());
	                throw new IllegalArgumentException("Unsupported field name for Unicore Submission resource.");
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
	        return unicoreSubmissionResourceList;
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
        List<String> globusSubmissionResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_SUBMISSION);
            List results;
            if (fieldName.equals(GlobusJobSubmissionConstants.SUBMISSION_ID)) {
                generator.setParameter(GlobusJobSubmissionConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) result;
                        globusSubmissionResourceIDs.add(globusJobSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GlobusJobSubmissionConstants.GLOBUS_GATEKEEPER_EP)) {
                generator.setParameter(GlobusJobSubmissionConstants.GLOBUS_GATEKEEPER_EP, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) result;
                        globusSubmissionResourceIDs.add(globusJobSubmission.getSubmissionID());
                    }
                }
            }
            else if (fieldName.equals(GlobusJobSubmissionConstants.SECURITY_PROTOCAL)) {
                generator.setParameter(GlobusJobSubmissionConstants.SECURITY_PROTOCAL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) result;
                        globusSubmissionResourceIDs.add(globusJobSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER)) {
                generator.setParameter(GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) result;
                        globusSubmissionResourceIDs.add(globusJobSubmission.getSubmissionID());
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
                logger.error("Unsupported field name for Globus Submission resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Globus Submission resource.");
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
        return globusSubmissionResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            UnicoreJobSubmission existingUnicoreSubmission = em.find(UnicoreJobSubmission.class, jobSubmissionInterfaceId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingUnicoreSubmission != null) {
                existingUnicoreSubmission.setSubmissionID(jobSubmissionInterfaceId);;
                existingUnicoreSubmission.setUnicoreEndpointUrl(unicoreEndpointUrl);
                existingUnicoreSubmission.setSecurityProtocol(securityProtocol);

                em.merge(existingUnicoreSubmission);
            } else {
            	UnicoreJobSubmission unicoreJobSubmission = new UnicoreJobSubmission();
                unicoreJobSubmission.setSubmissionID(jobSubmissionInterfaceId);
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
            UnicoreJobSubmission unicoreJobSubmission = em.find(UnicoreJobSubmission.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return unicoreJobSubmission != null;
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


	public String getjobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}

	public void setjobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
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
