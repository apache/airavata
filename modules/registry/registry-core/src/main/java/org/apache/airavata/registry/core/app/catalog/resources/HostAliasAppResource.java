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
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
import org.apache.airavata.registry.core.app.catalog.model.HostAlias;
import org.apache.airavata.registry.core.app.catalog.model.HostAliasPK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class HostAliasAppResource extends AppCatAbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(HostAliasAppResource.class);

    private String resourceID;
    private String alias;
    private ComputeResourceResource computeHostResource;

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(HOST_ALIAS);
            generator.setParameter(HostAliasConstants.RESOURCE_ID, (String)identifier);
//            generator.setParameter(HostAliasConstants.ALIAS, ids.get(HostAliasConstants.ALIAS));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_ALIAS);
            generator.setParameter(HostAliasConstants.RESOURCE_ID, ids.get(HostAliasConstants.RESOURCE_ID));
            generator.setParameter(HostAliasConstants.ALIAS, ids.get(HostAliasConstants.ALIAS));
            Query q = generator.selectQuery(em);
            HostAlias hostAlias = (HostAlias) q.getSingleResult();
            HostAliasAppResource hostAliasResource =
                    (HostAliasAppResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_ALIAS, hostAlias);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return hostAliasResource;
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

        List<AppCatalogResource> hostAliasResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_ALIAS);
            List results;
            if (fieldName.equals(HostAliasConstants.ALIAS)) {
                generator.setParameter(HostAliasConstants.ALIAS, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        HostAliasAppResource hostAliasResource =
                                (HostAliasAppResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_ALIAS, hostAlias);
                        hostAliasResources.add(hostAliasResource);
                    }
                }
            } else if (fieldName.equals(HostAliasConstants.RESOURCE_ID)) {
                generator.setParameter(HostAliasConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        HostAliasAppResource hostAliasResource =
                                (HostAliasAppResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.HOST_ALIAS, hostAlias);
                        hostAliasResources.add(hostAliasResource);
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
                logger.error("Unsupported field name for Host Alias Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Host Alias Resource.");
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
        return hostAliasResources;
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

        List<String> hostAliasResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(HOST_ALIAS);
            List results;
            if (fieldName.equals(HostAliasConstants.ALIAS)) {
                generator.setParameter(HostAliasConstants.ALIAS, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        hostAliasResourceIDs.add(hostAlias.getResourceID());
                    }
                }
            } else if (fieldName.equals(HostAliasConstants.RESOURCE_ID)) {
                generator.setParameter(HostAliasConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        HostAlias hostAlias = (HostAlias) result;
                        hostAliasResourceIDs.add(hostAlias.getResourceID());
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
                logger.error("Unsupported field name for Host Alias resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Host Alias Resource.");
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
        return hostAliasResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            HostAlias existingHostAlias = em.find(HostAlias.class, new HostAliasPK(resourceID, alias));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ComputeResource computeResource = em.find(ComputeResource.class, resourceID);
            if (existingHostAlias !=  null){
                existingHostAlias.setAlias(alias);
                existingHostAlias.setComputeResource(computeResource);
                existingHostAlias.setResourceID(resourceID);
                em.merge(existingHostAlias);
            }else {
                HostAlias hostAlias = new HostAlias();
                hostAlias.setAlias(alias);
                hostAlias.setResourceID(resourceID);
                hostAlias.setComputeResource(computeResource);

                em.persist(hostAlias);
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
            HostAlias hostAlias = em.find(HostAlias.class, new HostAliasPK(ids.get(HostAliasConstants.RESOURCE_ID),
                    ids.get(HostAliasConstants.ALIAS)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return hostAlias != null;
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

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ComputeResourceResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeResourceResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }
}
