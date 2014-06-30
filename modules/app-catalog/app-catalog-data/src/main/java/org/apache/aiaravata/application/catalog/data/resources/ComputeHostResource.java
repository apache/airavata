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

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ComputeResource;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class ComputeHostResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ComputeHostResource.class);
    private String resoureId;
    private String hostName;
    private String description;
    private String preferredJobSubmissionProtocol;

    public String getResoureId() {
        return resoureId;
    }

    public void setResoureId(String resoureId) {
        this.resoureId = resoureId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(String preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(COMPUTE_RESOURCE);
            generator.setParameter(ComputeResourceConstants.RESOURCE_ID, identifier);
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE);
            generator.setParameter(ComputeResourceConstants.RESOURCE_ID, identifier);
            Query q = generator.selectQuery(em);
            ComputeResource computeResource = (ComputeResource) q.getSingleResult();
            ComputeHostResource computeHostResource =
                    (ComputeHostResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE, computeResource);
            em.getTransaction().commit();
            em.close();
            return computeHostResource;
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
        List<Resource> computeHostResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE);
            List results;
            if (fieldName.equals(ComputeResourceConstants.HOST_NAME)) {
                generator.setParameter(ComputeResourceConstants.HOST_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResource computeResource = (ComputeResource) result;
                        ComputeHostResource computeHostResource =
                                (ComputeHostResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE, computeResource);
                        computeHostResources.add(computeHostResource);
                    }
                }
            } else if (fieldName.equals(ComputeResourceConstants.PREFERED_SUBMISSION_PROTOCOL)) {
                generator.setParameter(ComputeResourceConstants.PREFERED_SUBMISSION_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResource computeResource = (ComputeResource) result;
                        ComputeHostResource projectResource =
                                (ComputeHostResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE, computeResource);
                        computeHostResources.add(projectResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for compute resource.");
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
        return computeHostResources;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> computeHostResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE);
            List results;
            if (fieldName.equals(ComputeResourceConstants.HOST_NAME)) {
                generator.setParameter(ComputeResourceConstants.HOST_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResource computeResource = (ComputeResource) result;
                        computeHostResourceIDs.add(computeResource.getResourceID());
                    }
                }
            } else if (fieldName.equals(ComputeResourceConstants.PREFERED_SUBMISSION_PROTOCOL)) {
                generator.setParameter(ComputeResourceConstants.PREFERED_SUBMISSION_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResource computeResource = (ComputeResource) result;
                        computeHostResourceIDs.add(computeResource.getResourceID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for compute resource.");
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
        return computeHostResourceIDs;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ComputeResource existingComputeResource = em.find(ComputeResource.class, resoureId);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingComputeResource !=  null){
                existingComputeResource.setHostName(hostName);
                existingComputeResource.setDescription(description);
                existingComputeResource.setPreferredJobSubProtocol(preferredJobSubmissionProtocol);
                em.merge(existingComputeResource);
            }else {
                ComputeResource computeResource = new ComputeResource();
                computeResource.setResourceID(resoureId);
                computeResource.setHostName(hostName);
                computeResource.setDescription(description);
                computeResource.setPreferredJobSubProtocol(preferredJobSubmissionProtocol);
                em.persist(computeResource);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
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
            ComputeResource computeResource = em.find(ComputeResource.class, identifier);
            em.close();
            return computeResource != null;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
