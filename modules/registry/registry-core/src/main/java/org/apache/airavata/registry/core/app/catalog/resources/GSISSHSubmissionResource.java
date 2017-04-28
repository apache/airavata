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
import java.util.List;

public class GSISSHSubmissionResource extends AppCatAbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(GSISSHSubmissionResource.class);

    private String submissionID;
    private String resourceJobManager;
    private int sshPort;
    private String installedPath;
    private String monitorMode;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_SUBMISSION);
            generator.setParameter(GSISSHSubmissionConstants.SUBMISSION_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_SUBMISSION);
            generator.setParameter(GSISSHSubmissionConstants.SUBMISSION_ID, identifier);
            Query q = generator.selectQuery(em);
            GSISSHSubmission gsisshSubmission = (GSISSHSubmission) q.getSingleResult();
            GSISSHSubmissionResource gsisshSubmissionResource =
                    (GSISSHSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.GSISSH_SUBMISSION
                            , gsisshSubmission);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gsisshSubmissionResource;
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
        List<AppCatalogResource> gsiSSHSubmissionResourceList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_SUBMISSION);
            List results;
            if (fieldName.equals(GSISSHSubmissionConstants.MONITOR_MODE)) {
                generator.setParameter(GSISSHSubmissionConstants.MONITOR_MODE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        GSISSHSubmissionResource gsisshSubmissionResource =
                                (GSISSHSubmissionResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_SUBMISSION, gsisshSubmission);
                        gsiSSHSubmissionResourceList.add(gsisshSubmissionResource);
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.INSTALLED_PATH)) {
                generator.setParameter(GSISSHSubmissionConstants.INSTALLED_PATH, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        GSISSHSubmissionResource gsisshSubmissionResource =
                                (GSISSHSubmissionResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_SUBMISSION, gsisshSubmission);
                        gsiSSHSubmissionResourceList.add(gsisshSubmissionResource);
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.SSH_PORT)) {
                generator.setParameter(GSISSHSubmissionConstants.SSH_PORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        GSISSHSubmissionResource gsisshSubmissionResource =
                                (GSISSHSubmissionResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_SUBMISSION, gsisshSubmission);
                        gsiSSHSubmissionResourceList.add(gsisshSubmissionResource);
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER)) {
                generator.setParameter(GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        GSISSHSubmissionResource gsisshSubmissionResource =
                                (GSISSHSubmissionResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.GSISSH_SUBMISSION, gsisshSubmission);
                        gsiSSHSubmissionResourceList.add(gsisshSubmissionResource);
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
                logger.error("Unsupported field name for GSISSH submission resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Submission resource.");
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
        return gsiSSHSubmissionResourceList;
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
        List<String> gsiSSHSubmissionResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(GSISSH_SUBMISSION);
            List results;
            if (fieldName.equals(GSISSHSubmissionConstants.SUBMISSION_ID)) {
                generator.setParameter(GSISSHSubmissionConstants.SUBMISSION_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        gsiSSHSubmissionResourceIDs.add(gsisshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.SSH_PORT)) {
                generator.setParameter(GSISSHSubmissionConstants.SSH_PORT, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        gsiSSHSubmissionResourceIDs.add(gsisshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.MONITOR_MODE)) {
                generator.setParameter(GSISSHSubmissionConstants.MONITOR_MODE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        gsiSSHSubmissionResourceIDs.add(gsisshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER)) {
                generator.setParameter(GSISSHSubmissionConstants.RESOURCE_JOB_MANAGER, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        gsiSSHSubmissionResourceIDs.add(gsisshSubmission.getSubmissionID());
                    }
                }
            } else if (fieldName.equals(GSISSHSubmissionConstants.INSTALLED_PATH)) {
                generator.setParameter(GSISSHSubmissionConstants.INSTALLED_PATH, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GSISSHSubmission gsisshSubmission = (GSISSHSubmission) result;
                        gsiSSHSubmissionResourceIDs.add(gsisshSubmission.getSubmissionID());
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
                logger.error("Unsupported field name for GSISSH Submission resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Submission resource.");
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
        return gsiSSHSubmissionResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            GSISSHSubmission existingGSISSHSubmission = em.find(GSISSHSubmission.class, submissionID);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingGSISSHSubmission != null) {
                existingGSISSHSubmission.setSubmissionID(submissionID);
                existingGSISSHSubmission.setSshPort(sshPort);
                existingGSISSHSubmission.setResourceJobManager(resourceJobManager);
                existingGSISSHSubmission.setInstalledPath(installedPath);
                existingGSISSHSubmission.setMonitorMode(monitorMode);
                em.merge(existingGSISSHSubmission);
            } else {
                GSISSHSubmission gsisshSubmission = new GSISSHSubmission();
                gsisshSubmission.setSubmissionID(submissionID);
                gsisshSubmission.setSshPort(sshPort);
                gsisshSubmission.setResourceJobManager(resourceJobManager);
                gsisshSubmission.setInstalledPath(installedPath);
                gsisshSubmission.setMonitorMode(monitorMode);
                em.persist(gsisshSubmission);
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
            GSISSHSubmission gsisshSubmission = em.find(GSISSHSubmission.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gsisshSubmission != null;
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

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getInstalledPath() {
        return installedPath;
    }

    public void setInstalledPath(String installedPath) {
        this.installedPath = installedPath;
    }

    public String getMonitorMode() {
        return monitorMode;
    }

    public void setMonitorMode(String monitorMode) {
        this.monitorMode = monitorMode;
    }

}
