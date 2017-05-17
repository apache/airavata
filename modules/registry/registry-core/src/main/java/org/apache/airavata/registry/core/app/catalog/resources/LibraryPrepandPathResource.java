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
import org.apache.airavata.registry.core.app.catalog.model.ApplicationDeployment;
import org.apache.airavata.registry.core.app.catalog.model.LibraryPrepandPath;
import org.apache.airavata.registry.core.app.catalog.model.LibraryPrepandPath_PK;
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

public class LibraryPrepandPathResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(LibraryPrepandPathResource.class);
    private String deploymentId;
    private String name;
    private String value;
    private AppDeploymentResource appDeploymentResource;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AppDeploymentResource getAppDeploymentResource() {
        return appDeploymentResource;
    }

    public void setAppDeploymentResource(AppDeploymentResource appDeploymentResource) {
        this.appDeploymentResource = appDeploymentResource;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
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
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(LIBRARY_PREPAND_PATH);
            generator.setParameter(LibraryPrepandPathConstants.DEPLOYMENT_ID, ids.get(LibraryPrepandPathConstants.DEPLOYMENT_ID));
            if (ids.get(LibraryPrepandPathConstants.NAME) != null){
                generator.setParameter(LibraryPrepandPathConstants.NAME, ids.get(LibraryPrepandPathConstants.NAME));
            }
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
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public AppCatalogResource get(Object identifier) throws AppCatalogException {
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
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(LIBRARY_PREPAND_PATH);
            generator.setParameter(LibraryPrepandPathConstants.DEPLOYMENT_ID, ids.get(LibraryPrepandPathConstants.DEPLOYMENT_ID));
            generator.setParameter(LibraryPrepandPathConstants.NAME, ids.get(LibraryPrepandPathConstants.NAME));
            Query q = generator.selectQuery(em);
            LibraryPrepandPath libraryPrepandPath = (LibraryPrepandPath) q.getSingleResult();
            LibraryPrepandPathResource resource =
                    (LibraryPrepandPathResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.LIBRARY_PREPAND_PATH, libraryPrepandPath);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return resource;
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
        List<AppCatalogResource> libPrepPathList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(LIBRARY_PREPAND_PATH);
            List results;
            if (fieldName.equals(LibraryPrepandPathConstants.DEPLOYMENT_ID)) {
                generator.setParameter(LibraryPrepandPathConstants.DEPLOYMENT_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        LibraryPrepandPath prepandPath = (LibraryPrepandPath) result;
                        LibraryPrepandPathResource resource =
                                (LibraryPrepandPathResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.LIBRARY_PREPAND_PATH, prepandPath);
                        libPrepPathList.add(resource);
                    }
                }
            } else if (fieldName.equals(LibraryPrepandPathConstants.NAME)) {
                generator.setParameter(LibraryPrepandPathConstants.NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        LibraryPrepandPath prepandPath = (LibraryPrepandPath) result;
                        LibraryPrepandPathResource resource =
                                (LibraryPrepandPathResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.LIBRARY_PREPAND_PATH, prepandPath);
                        libPrepPathList.add(resource);
                    }
                }
            }else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for libraryPrepandPath resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for libraryPrepandPath resource.");
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
        return libPrepPathList;
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
        logger.error("Unsupported for objects with a composite identifier");
        throw new AppCatalogException("Unsupported for objects with a composite identifier");
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            LibraryPrepandPath existigPrepPath = em.find(LibraryPrepandPath.class, new LibraryPrepandPath_PK(deploymentId, name));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ApplicationDeployment deployment = em.find(ApplicationDeployment.class, deploymentId);
            if (existigPrepPath !=  null){
                existigPrepPath.setValue(value);
                existigPrepPath.setApplicationDeployment(deployment);
                em.merge(existigPrepPath);
            }else {
                LibraryPrepandPath prepandPath = new LibraryPrepandPath();
                prepandPath.setDeploymentID(deploymentId);
                prepandPath.setName(name);
                prepandPath.setValue(value);
                prepandPath.setApplicationDeployment(deployment);
                em.persist(prepandPath);
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
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
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
            LibraryPrepandPath prepandPath = em.find(LibraryPrepandPath.class,
                                                          new LibraryPrepandPath_PK(ids.get(LibraryPrepandPathConstants.DEPLOYMENT_ID),
                                                                                    ids.get(LibraryPrepandPathConstants.NAME)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return prepandPath != null;
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
