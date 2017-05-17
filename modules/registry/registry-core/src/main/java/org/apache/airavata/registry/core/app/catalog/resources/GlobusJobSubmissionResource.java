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
import org.apache.airavata.registry.core.app.catalog.model.GlobusJobSubmission;
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

public class GlobusJobSubmissionResource extends AppCatAbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(GlobusJobSubmissionResource.class);

    private String submissionID;
    private String resourceJobManager;
    private String securityProtocol;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_SUBMISSION);
            generator.setParameter(GlobusJobSubmissionConstants.SUBMISSION_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_SUBMISSION);
            generator.setParameter(GlobusJobSubmissionConstants.SUBMISSION_ID, identifier);
            Query q = generator.selectQuery(em);
            GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) q.getSingleResult();
            GlobusJobSubmissionResource globusJobSubmissionResource =
                    (GlobusJobSubmissionResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.GLOBUS_SUBMISSION, globusJobSubmission);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return globusJobSubmissionResource;
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
        List<AppCatalogResource> globusSubmissionResourceList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_SUBMISSION);
            List results;
            if (fieldName.equals(GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER)) {
                generator.setParameter(GlobusJobSubmissionConstants.RESOURCE_JOB_MANAGER, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) result;
                        GlobusJobSubmissionResource globusJobSubmissionResource =
                                (GlobusJobSubmissionResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GLOBUS_SUBMISSION, globusJobSubmission);
                        globusSubmissionResourceList.add(globusJobSubmissionResource);
                    }
                }
            } else if (fieldName.equals(GlobusJobSubmissionConstants.SECURITY_PROTOCAL)) {
                generator.setParameter(GlobusJobSubmissionConstants.SECURITY_PROTOCAL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusJobSubmission globusJobSubmission = (GlobusJobSubmission) result;
                        GlobusJobSubmissionResource globusJobSubmissionResource =
                                (GlobusJobSubmissionResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GLOBUS_SUBMISSION, globusJobSubmission);
                        globusSubmissionResourceList.add(globusJobSubmissionResource);
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
                logger.error("Unsupported field name for Globus submission resource.", new IllegalArgumentException());
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
        return globusSubmissionResourceList;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
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
            GlobusJobSubmission existingGlobusSubmission = em.find(GlobusJobSubmission.class, submissionID);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingGlobusSubmission != null) {
                existingGlobusSubmission.setSubmissionID(submissionID);
                existingGlobusSubmission.setResourceJobManager(resourceJobManager);
                existingGlobusSubmission.setSecurityProtocol(securityProtocol);
                em.merge(existingGlobusSubmission);
            } else {
                GlobusJobSubmission globusJobSubmission = new GlobusJobSubmission();
                globusJobSubmission.setSubmissionID(submissionID);
                globusJobSubmission.setSecurityProtocol(securityProtocol);
                globusJobSubmission.setResourceJobManager(resourceJobManager);
                em.persist(globusJobSubmission);
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
            GlobusJobSubmission globusJobSubmission = em.find(GlobusJobSubmission.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return globusJobSubmission != null;
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

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(String resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

}
