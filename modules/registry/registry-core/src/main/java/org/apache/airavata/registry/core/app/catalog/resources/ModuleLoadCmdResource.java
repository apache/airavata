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
import org.apache.airavata.registry.core.app.catalog.model.ModuleLoadCmd;
import org.apache.airavata.registry.core.app.catalog.model.ModuleLoadCmd_PK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class ModuleLoadCmdResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ModuleLoadCmdResource.class);
    private String cmd;
    private String appDeploymentId;
    private Integer order;
    private AppDeploymentResource appDeploymentResource;

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(MODULE_LOAD_CMD);
            generator.setParameter(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID, ids.get(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID));
            if (ids.get(ModuleLoadCmdConstants.CMD) != null){
                generator.setParameter(ModuleLoadCmdConstants.CMD, ids.get(ModuleLoadCmdConstants.CMD));
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
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(MODULE_LOAD_CMD);
            generator.setParameter(ModuleLoadCmdConstants.CMD, ids.get(ModuleLoadCmdConstants.CMD));
            generator.setParameter(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID, ids.get(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID));
            Query q = generator.selectQuery(em);
            ModuleLoadCmd moduleLoadCmd = (ModuleLoadCmd) q.getSingleResult();
            ModuleLoadCmdResource moduleLoadCmdResource = (ModuleLoadCmdResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.MODULE_LOAD_CMD, moduleLoadCmd);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return moduleLoadCmdResource;
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
        List<AppCatalogResource> moduleLoadCmdResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(MODULE_LOAD_CMD);
            Query q;
            if ((fieldName.equals(ModuleLoadCmdConstants.CMD)) || (fieldName.equals(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    ModuleLoadCmd moduleLoadCmd = (ModuleLoadCmd) result;
                    ModuleLoadCmdResource moduleLoadCmdResource = (ModuleLoadCmdResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.MODULE_LOAD_CMD, moduleLoadCmd);
                    moduleLoadCmdResources.add(moduleLoadCmdResource);
                }
                Collections.sort(moduleLoadCmdResources,
                        (o1, o2) -> ((ModuleLoadCmdResource)o1).getOrder()- ((ModuleLoadCmdResource)o2).getOrder());
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Module Load Cmd Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Module Load Cmd Resource.");
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
        return moduleLoadCmdResources;
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
        List<String> moduleLoadCmdResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(MODULE_LOAD_CMD);
            Query q;
            if ((fieldName.equals(ModuleLoadCmdConstants.CMD)) || (fieldName.equals(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    ModuleLoadCmd moduleLoadCmd = (ModuleLoadCmd) result;
                    ModuleLoadCmdResource moduleLoadCmdResource = (ModuleLoadCmdResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.MODULE_LOAD_CMD, moduleLoadCmd);
                    moduleLoadCmdResourceIDs.add(moduleLoadCmdResource.getAppDeploymentId());
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Module Load Cmd Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Module Load Cmd Resource.");
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
        return moduleLoadCmdResourceIDs;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ModuleLoadCmd existingModuleLoadCmd = em.find(ModuleLoadCmd.class, new ModuleLoadCmd_PK(cmd, appDeploymentId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            ModuleLoadCmd moduleLoadCmd;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingModuleLoadCmd == null) {
                moduleLoadCmd = new ModuleLoadCmd();
            } else {
                moduleLoadCmd = existingModuleLoadCmd;
            }
            moduleLoadCmd.setCmd(getCmd());
            moduleLoadCmd.setAppDeploymentId(getAppDeploymentId());
            moduleLoadCmd.setOrder(order);
            ApplicationDeployment applicationDeployment = em.find(ApplicationDeployment.class, getAppDeploymentId());
            moduleLoadCmd.setApplicationDeployment(applicationDeployment);
            if (existingModuleLoadCmd == null) {
                em.persist(moduleLoadCmd);
            } else {
                em.merge(moduleLoadCmd);
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
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ModuleLoadCmd moduleLoadCmd = em.find(ModuleLoadCmd.class, new ModuleLoadCmd_PK(ids.get(ModuleLoadCmdConstants.CMD), ids.get(ModuleLoadCmdConstants.APP_DEPLOYMENT_ID)));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return moduleLoadCmd != null;
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

    public String getCmd() {
        return cmd;
    }

    public String getAppDeploymentId() {
        return appDeploymentId;
    }

    public AppDeploymentResource getAppDeploymentResource() {
        return appDeploymentResource;
    }

    public void setCmd(String cmd) {
        this.cmd=cmd;
    }

    public void setAppDeploymentId(String appDeploymentId) {
        this.appDeploymentId=appDeploymentId;
    }

    public void setAppDeploymentResource(AppDeploymentResource appDeploymentResource) {
        this.appDeploymentResource=appDeploymentResource;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}


