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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.ApplicationInterface;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AppInterfaceResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(AppInterfaceResource.class);
    private String interfaceId;
    private String appName;
    private String appDescription;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String gatewayId;
    private boolean archiveWorkingDirectory;
    private boolean hasOptionalFileInputs;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public boolean isArchiveWorkingDirectory() {
        return archiveWorkingDirectory;
    }

    public void setArchiveWorkingDirectory(boolean archiveWorkingDirectory) {
        this.archiveWorkingDirectory = archiveWorkingDirectory;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public boolean isHasOptionalFileInputs() {
        return hasOptionalFileInputs;
    }

    public void setHasOptionalFileInputs(boolean hasOptionalFileInputs) {
        this.hasOptionalFileInputs = hasOptionalFileInputs;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            generator.setParameter(ApplicationInterfaceConstants.INTERFACE_ID, identifier);
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
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            generator.setParameter(ApplicationInterfaceConstants.INTERFACE_ID, identifier);
            Query q = generator.selectQuery(em);
            ApplicationInterface applicationInterface = (ApplicationInterface) q.getSingleResult();
            AppInterfaceResource resource =
                    (AppInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_INTERFACE, applicationInterface);
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
        List<AppCatalogResource> resourceList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            List results;
            if (fieldName.equals(ApplicationInterfaceConstants.APPLICATION_NAME)) {
                generator.setParameter(ApplicationInterfaceConstants.APPLICATION_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInterface appInterface = (ApplicationInterface) result;
                        AppInterfaceResource resource =
                                (AppInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_INTERFACE, appInterface);
                        resourceList.add(resource);
                    }
                }
            }  else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for application interface.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for application interface.");
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
        return resourceList;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> resourceList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            generator.setParameter(ApplicationInterfaceConstants.GATEWAY_ID, gatewayId);
            Query   q = generator.selectQuery(em);
            List results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInterface appInterface = (ApplicationInterface) result;
                        AppInterfaceResource resource =
                                (AppInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_INTERFACE, appInterface);
                        resourceList.add(resource);
                    }
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
        return resourceList;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        List<String> resourceList = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            Query   q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    ApplicationInterface appInterface = (ApplicationInterface) result;
                    resourceList.add(appInterface.getInterfaceID());
                }
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
        return resourceList;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> resourceList = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            List results;
            if (fieldName.equals(ApplicationInterfaceConstants.APPLICATION_NAME)) {
                generator.setParameter(ApplicationInterfaceConstants.APPLICATION_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationInterface appInterface = (ApplicationInterface) result;
                        resourceList.add(appInterface.getInterfaceID());
                    }
                }
            }  else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for application interface.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for application interface.");
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
        return resourceList;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationInterface existigAppInterface = em.find(ApplicationInterface.class, interfaceId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existigAppInterface !=  null){
                existigAppInterface.setAppName(appName);
                existigAppInterface.setAppDescription(appDescription);
                existigAppInterface.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                existigAppInterface.setGatewayId(gatewayId);
                existigAppInterface.setArchiveWorkingDirectory(archiveWorkingDirectory);
                existigAppInterface.setHasOptionalFileInputs(hasOptionalFileInputs);
                em.merge(existigAppInterface);
            }else {
                ApplicationInterface applicationInterface = new ApplicationInterface();
                applicationInterface.setInterfaceID(interfaceId);
                applicationInterface.setAppName(appName);
                applicationInterface.setAppDescription(appDescription);
                applicationInterface.setCreationTime(AiravataUtils.getCurrentTimestamp());
                applicationInterface.setGatewayId(gatewayId);
                applicationInterface.setArchiveWorkingDirectory(archiveWorkingDirectory);
                applicationInterface.setHasOptionalFileInputs(hasOptionalFileInputs);
                em.persist(applicationInterface);
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
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationInterface existigAppInterface = em.find(ApplicationInterface.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return existigAppInterface != null;
        }catch (Exception e) {
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
