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
import org.apache.airavata.registry.core.app.catalog.model.GlobusGKEndPointPK;
import org.apache.airavata.registry.core.app.catalog.model.GlobusGKEndpoint;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobusGKEndpointResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GlobusGKEndpointResource.class);

    private String submissionID;
    private String endpoint;

    private GlobusJobSubmissionResource globusJobSubmissionResource;


    public void remove(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            generator.setParameter(GlobusEPConstants.ENDPOINT, ids.get(GlobusEPConstants.ENDPOINT));
            generator.setParameter(GlobusEPConstants.SUBMISSION_ID, ids.get(GlobusEPConstants.SUBMISSION_ID));
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
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            generator.setParameter(GlobusEPConstants.SUBMISSION_ID, ids.get(GlobusEPConstants.SUBMISSION_ID));
            generator.setParameter(GlobusEPConstants.ENDPOINT, ids.get(GlobusEPConstants.ENDPOINT));
            Query q = generator.selectQuery(em);
            GlobusGKEndpoint gkEndpoint = (GlobusGKEndpoint) q.getSingleResult();
            GlobusGKEndpointResource gkEndpointResource =
                    (GlobusGKEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GLOBUS_GK_ENDPOINT, gkEndpoint);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gkEndpointResource;
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
        List<AppCatalogResource> resources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            List results;
            if (fieldName.equals(GlobusEPConstants.ENDPOINT)) {
                generator.setParameter(GlobusEPConstants.ENDPOINT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint gkEndpoint = (GlobusGKEndpoint) result;
                        GlobusGKEndpointResource gkEndpointResource =
                                (GlobusGKEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GLOBUS_GK_ENDPOINT, gkEndpoint);
                        resources.add(gkEndpointResource);
                    }
                }
            } else if (fieldName.equals(GlobusEPConstants.SUBMISSION_ID)) {
                generator.setParameter(GlobusEPConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint globusGKEndpoint = (GlobusGKEndpoint) result;
                        GlobusGKEndpointResource gkEndpointResource =
                                (GlobusGKEndpointResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GLOBUS_GK_ENDPOINT, globusGKEndpoint);
                        resources.add(gkEndpointResource);
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
                logger.error("Unsupported field name for Globus Endpoint Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Globus Endpoint Resource.");
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
        return resources;
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
        List<String> list = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GLOBUS_GK_ENDPOINT);
            List results;
            if (fieldName.equals(GlobusEPConstants.SUBMISSION_ID)) {
                generator.setParameter(GlobusEPConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint globusGKEndpoint = (GlobusGKEndpoint) result;
                        list.add(globusGKEndpoint.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GlobusEPConstants.ENDPOINT)) {
                generator.setParameter(GlobusEPConstants.ENDPOINT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GlobusGKEndpoint globusGKEndpoint = (GlobusGKEndpoint) result;
                        list.add(globusGKEndpoint.getSubmissionID());
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
                logger.error("Unsupported field name for Globus EP resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Globus EP Resource.");
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
        return list;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GlobusGKEndpoint existingGlobusEP = em.find(GlobusGKEndpoint.class, new GlobusGKEndPointPK(submissionID, endpoint));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GlobusJobSubmission globusJobSubmission = em.find(GlobusJobSubmission.class, submissionID);
            if (existingGlobusEP != null) {
                existingGlobusEP.setSubmissionID(submissionID);
                existingGlobusEP.setEndpoint(endpoint);
                existingGlobusEP.setGlobusSubmission(globusJobSubmission);
                em.merge(existingGlobusEP);
            } else {
                GlobusGKEndpoint globusGKEndpoint = new GlobusGKEndpoint();
                globusGKEndpoint.setSubmissionID(submissionID);
                globusGKEndpoint.setEndpoint(endpoint);
                globusGKEndpoint.setGlobusSubmission(globusJobSubmission);
                em.persist(globusGKEndpoint);
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
        HashMap<String, String> ids;
        if (identifier instanceof Map){
            ids = (HashMap)identifier;
        }else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GlobusGKEndpoint gkEndpoint = em.find(GlobusGKEndpoint.class, new GlobusGKEndPointPK(ids.get(GlobusEPConstants.SUBMISSION_ID),
                    ids.get(GlobusEPConstants.ENDPOINT)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gkEndpoint != null;
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
        }    }

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public GlobusJobSubmissionResource getGlobusJobSubmissionResource() {
        return globusJobSubmissionResource;
    }

    public void setGlobusJobSubmissionResource(GlobusJobSubmissionResource globusJobSubmissionResource) {
        this.globusJobSubmissionResource = globusJobSubmissionResource;
    }
}
