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
import org.apache.airavata.registry.core.app.catalog.model.GSISSHExport;
import org.apache.airavata.registry.core.app.catalog.model.GSISSHExportPK;
import org.apache.airavata.registry.core.app.catalog.model.GSISSHSubmission;
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

public class GSISSHExportResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GSISSHExportResource.class);

    private String submissionID;
    private String export;

    private GSISSHSubmissionResource gsisshSubmissionResource;


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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_EXPORT);
            generator.setParameter(GSISSHExportConstants.EXPORT, ids.get(GSISSHExportConstants.EXPORT));
            generator.setParameter(GSISSHExportConstants.SUBMISSION_ID, ids.get(GSISSHExportConstants.SUBMISSION_ID));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_EXPORT);
            generator.setParameter(GSISSHExportConstants.SUBMISSION_ID, ids.get(GSISSHExportConstants.SUBMISSION_ID));
            generator.setParameter(GSISSHExportConstants.EXPORT, ids.get(GSISSHExportConstants.EXPORT));
            Query q = generator.selectQuery(em);
            GSISSHExport gsisshExport = (GSISSHExport) q.getSingleResult();
            GSISSHExportResource gsisshExportResource =
                    (GSISSHExportResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GSISSH_EXPORT
                            , gsisshExport);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gsisshExportResource;
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
        List<AppCatalogResource> gsiSSHExportResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_EXPORT);
            List results;
            if (fieldName.equals(GSISSHExportConstants.EXPORT)) {
                generator.setParameter(GSISSHExportConstants.EXPORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHExport gsisshExport = (GSISSHExport) result;
                        GSISSHExportResource gsisshExportResource =
                                (GSISSHExportResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GSISSH_EXPORT, gsisshExport);
                        gsiSSHExportResources.add(gsisshExportResource);
                    }
                }
            } else if (fieldName.equals(GSISSHExportConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHExportConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHExport gsisshExport = (GSISSHExport) result;
                        GSISSHExportResource gsisshExportResource =
                                (GSISSHExportResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GSISSH_EXPORT, gsisshExport);
                        gsiSSHExportResources.add(gsisshExportResource);
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
                logger.error("Unsupported field name for GSISSH Export Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Export Resource.");
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
        return gsiSSHExportResources;
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
        List<String> gsiSSHExportIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_EXPORT);
            List results;
            if (fieldName.equals(GSISSHExportConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHExportConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHExport gsisshExport = (GSISSHExport) result;
                        gsiSSHExportIDs.add(gsisshExport.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHExportConstants.EXPORT)) {
                generator.setParameter(GSISSHExportConstants.EXPORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHExport gsisshExport = (GSISSHExport) result;
                        gsiSSHExportIDs.add(gsisshExport.getSubmissionID());
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
                logger.error("Unsupported field name for GSISSH Export resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Export Resource.");
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
        return gsiSSHExportIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GSISSHExport existingGSIExport = em.find(GSISSHExport.class, new GSISSHExportPK(submissionID, export));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GSISSHSubmission gsisshSubmission = em.find(GSISSHSubmission.class, submissionID);
            if (existingGSIExport != null) {
                existingGSIExport.setSubmissionID(submissionID);
                existingGSIExport.setExport(export);
                existingGSIExport.setGsisshJobSubmission(gsisshSubmission);
                em.merge(existingGSIExport);
            } else {
                GSISSHExport gsisshExport = new GSISSHExport();
                gsisshExport.setSubmissionID(submissionID);
                gsisshExport.setExport(export);
                gsisshExport.setGsisshJobSubmission(gsisshSubmission);
                em.persist(gsisshExport);
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
            GSISSHExport gsisshExport = em.find(GSISSHExport.class, new GSISSHExportPK(ids.get(GSISSHExportConstants.SUBMISSION_ID),
                    ids.get(GSISSHExportConstants.EXPORT)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gsisshExport != null;
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

    public String getExport() {
        return export;
    }

    public void setExport(String export) {
        this.export = export;
    }

    public GSISSHSubmissionResource getGsisshSubmissionResource() {
        return gsisshSubmissionResource;
    }

    public void setGsisshSubmissionResource(GSISSHSubmissionResource gsisshSubmissionResource) {
        this.gsisshSubmissionResource = gsisshSubmissionResource;
    }
}
