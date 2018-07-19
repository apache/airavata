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
import org.apache.airavata.registry.core.app.catalog.model.ApplicationDeployment;
import org.apache.airavata.registry.core.app.catalog.model.ApplicationModule;
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

public class AppModuleResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(AppModuleResource.class);
    private String moduleId;
    private String moduleName;
    private String moduleVersion;
    private String moduleDesc;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String gatewayId;
    private List<String> accessibleApplicationDeploymentIds;
    private List<String> accessibleComputeResourceIds;

    public List<String> getAccessibleApplicationDeploymentIds() { return accessibleApplicationDeploymentIds; }

    public void setAccessibleApplicationDeploymentIds(List<String> accessibleApplicationDeploymentIds) { this.accessibleApplicationDeploymentIds = accessibleApplicationDeploymentIds; }

    public List<String> getAccessibleComputeResourceIds() { return accessibleComputeResourceIds; }

    public void setAccessibleComputeResourceIds(List<String> accessibleComputeResourceIds) { this.accessibleComputeResourceIds = accessibleComputeResourceIds; }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getModuleDesc() {
        return moduleDesc;
    }

    public void setModuleDesc(String moduleDesc) {
        this.moduleDesc = moduleDesc;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(APPLICATION_MODULE);
            generator.setParameter(ApplicationModuleConstants.MODULE_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_MODULE);
            generator.setParameter(ApplicationModuleConstants.MODULE_ID, identifier);
            Query q = generator.selectQuery(em);
            ApplicationModule applicationModule = (ApplicationModule) q.getSingleResult();
            AppModuleResource appModuleResource =
                    (AppModuleResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_MODULE, applicationModule);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return appModuleResource;
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
        List<AppCatalogResource> moduleResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_MODULE);
            List results;
            if (fieldName.equals(ApplicationModuleConstants.MODULE_NAME)) {
                generator.setParameter(ApplicationModuleConstants.MODULE_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationModule applicationModule = (ApplicationModule) result;
                        AppModuleResource moduleResource =
                                (AppModuleResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_MODULE, applicationModule);
                        moduleResources.add(moduleResource);
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
                logger.error("Unsupported field name for app module resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for app module resource.");
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
        return moduleResources;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> appModuleResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();

            String queryString = "SELECT appModule " +
                    "FROM " + APPLICATION_MODULE + " appModule " +
                    "WHERE appModule." + ApplicationModuleConstants.GATEWAY_ID + " = :" + ApplicationModuleConstants.GATEWAY_ID + " ";
            if (accessibleApplicationDeploymentIds != null && !accessibleApplicationDeploymentIds.isEmpty()) {
                queryString += "  AND appModule." + ApplicationModuleConstants.MODULE_ID + " IN (" +
                        "    SELECT appDeploy." + ApplicationDeploymentConstants.APP_MODULE_ID + " " +
                        "    FROM " + APPLICATION_DEPLOYMENT + " appDeploy " +
                        "    WHERE appDeploy." + ApplicationDeploymentConstants.DEPLOYMENT_ID + " IN :" + ApplicationDeploymentConstants.DEPLOYMENT_ID +
                        "    AND appDeploy." + ApplicationDeploymentConstants.COMPUTE_HOST_ID + " IN : " + ApplicationDeploymentConstants.COMPUTE_HOST_ID + ")";
            }
            Query q =  em.createQuery(queryString);
            q.setParameter(ApplicationModuleConstants.GATEWAY_ID, gatewayId);
            if (accessibleApplicationDeploymentIds != null && !accessibleApplicationDeploymentIds.isEmpty()) {
                q.setParameter(ApplicationDeploymentConstants.DEPLOYMENT_ID, accessibleApplicationDeploymentIds);
            }
            if (accessibleComputeResourceIds != null && !accessibleComputeResourceIds.isEmpty()) {
                q.setParameter(ApplicationDeploymentConstants.COMPUTE_HOST_ID, accessibleComputeResourceIds);
            }
            List<?> results = q.getResultList();
            for (Object result : results) {
                ApplicationModule module = (ApplicationModule) result;
                AppModuleResource appModuleResource = (AppModuleResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_MODULE, module);
                appModuleResources.add(appModuleResource);
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
        return appModuleResources;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> moduleResources = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_MODULE);
            List results;
            if (fieldName.equals(ApplicationModuleConstants.MODULE_NAME)) {
                generator.setParameter(ApplicationModuleConstants.MODULE_NAME, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationModule applicationModule = (ApplicationModule) result;
                        moduleResources.add(applicationModule.getModuleID());
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
                logger.error("Unsupported field name for app module resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for app module resource.");
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
        return moduleResources;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationModule existingModule = em.find(ApplicationModule.class, moduleId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingModule !=  null){
                existingModule.setModuleName(moduleName);
                existingModule.setModuleVersion(moduleVersion);
                existingModule.setModuleDesc(moduleDesc);
                existingModule.setGatewayId(gatewayId);
                existingModule.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                em.merge(existingModule);
            }else {
                ApplicationModule applicationModule = new ApplicationModule();
                applicationModule.setModuleID(moduleId);
                applicationModule.setModuleName(moduleName);
                applicationModule.setModuleVersion(moduleVersion);
                applicationModule.setModuleDesc(moduleDesc);
                applicationModule.setGatewayId(gatewayId);
                applicationModule.setCreationTime(AiravataUtils.getCurrentTimestamp());
                em.persist(applicationModule);
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
            ApplicationModule applicationModule = em.find(ApplicationModule.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return applicationModule != null;
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
