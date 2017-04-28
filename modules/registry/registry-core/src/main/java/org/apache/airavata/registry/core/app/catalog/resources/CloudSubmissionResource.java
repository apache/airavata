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
import org.apache.airavata.registry.core.app.catalog.model.CloudJobSubmission;
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

public class CloudSubmissionResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(LocalSubmissionResource.class);
    private String jobSubmissionInterfaceId;
    private String securityProtocol;
    private String nodeId;
    private String executableType;
    private String providerName;
    private String userAccountName;

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(CLOUD_JOB_SUBMISSION);
            generator.setParameter(LocalSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID, identifier);
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
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(CLOUD_JOB_SUBMISSION);
            generator.setParameter(LocalSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID, identifier);
            Query q = generator.selectQuery(em);
            CloudJobSubmission cloudJobSubmission = (CloudJobSubmission) q.getSingleResult();
            CloudSubmissionResource localSubmissionResource = (CloudSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.ClOUD_SUBMISSION, cloudJobSubmission);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return localSubmissionResource;
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
        List<AppCatalogResource> localSubmissionResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(CLOUD_JOB_SUBMISSION);
            Query q;
            if ((fieldName.equals(LocalSubmissionConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(LocalSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    CloudJobSubmission localSubmission = (CloudJobSubmission) result;
                    CloudSubmissionResource localSubmissionResource = (CloudSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.ClOUD_SUBMISSION, localSubmission);
                    localSubmissionResources.add(localSubmissionResource);
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Local Submission Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Local Submission Resource.");
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
        return localSubmissionResources;
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
        List<String> localSubmissionResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(CLOUD_JOB_SUBMISSION);
            Query q;
            if ((fieldName.equals(LocalSubmissionConstants.RESOURCE_JOB_MANAGER_ID)) || (fieldName.equals(LocalSubmissionConstants.JOB_SUBMISSION_INTERFACE_ID))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    CloudJobSubmission localSubmission = (CloudJobSubmission) result;
                    LocalSubmissionResource localSubmissionResource = (LocalSubmissionResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.ClOUD_SUBMISSION, localSubmission);
                    localSubmissionResourceIDs.add(localSubmissionResource.getJobSubmissionInterfaceId());
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Local Submission Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Local Submission Resource.");
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
        return localSubmissionResourceIDs;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            CloudJobSubmission existingLocalSubmission = em.find(CloudJobSubmission.class, jobSubmissionInterfaceId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            CloudJobSubmission cloudJobSubmission;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingLocalSubmission == null) {
                cloudJobSubmission = new CloudJobSubmission();
            } else {
                cloudJobSubmission = existingLocalSubmission;
            }
            cloudJobSubmission.setExecutableType(getExecutableType());
            cloudJobSubmission.setNodeId(getNodeId());
            cloudJobSubmission.setJobSubmissionInterfaceId(getJobSubmissionInterfaceId());
            cloudJobSubmission.setSecurityProtocol(getSecurityProtocol());
            cloudJobSubmission.setJobSubmissionInterfaceId(getJobSubmissionInterfaceId());
            cloudJobSubmission.setUserAccountName(getUserAccountName());
            cloudJobSubmission.setProviderName(getProviderName());
            if (existingLocalSubmission == null) {
                em.persist(cloudJobSubmission);
            } else {
                em.merge(cloudJobSubmission);
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
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            CloudJobSubmission localSubmission = em.find(CloudJobSubmission.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return localSubmission != null;
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

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getExecutableType() {
        return executableType;
    }

    public void setExecutableType(String executableType) {
        this.executableType = executableType;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getUserAccountName() {
        return userAccountName;
    }

    public void setUserAccountName(String userAccountName) {
        this.userAccountName = userAccountName;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId=jobSubmissionInterfaceId;
    }
}
