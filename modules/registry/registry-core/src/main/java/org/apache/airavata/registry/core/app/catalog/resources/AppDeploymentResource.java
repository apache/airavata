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
import org.apache.airavata.registry.core.app.catalog.model.ComputeResource;
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

public class AppDeploymentResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(AppDeploymentResource.class);
    private String deploymentId;
    private String appModuleId;
    private String hostId;
    private String executablePath;
    private String parallelism;
    private String appDes;
    private String gatewayId;
    private String defaultQueueName;
    private int defaultNodeCount;
    private int defaultCPUCount;
    private int defaultWalltime;
    private boolean editableByUser;
    private ComputeResourceResource hostResource;
    private AppModuleResource moduleResource;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private List<String> accessibleApplicationDeploymentIds;
    private List<String> accessibleComputeResourceIds;

    public List<String> getAccessibleApplicationDeploymentIds() { return accessibleApplicationDeploymentIds; }

    public void setAccessibleApplicationDeploymentIds(List<String> accessibleAppDeploymentIds) { this.accessibleApplicationDeploymentIds = accessibleAppDeploymentIds; }

    public List<String> getAccessibleComputeResourceIds() { return accessibleComputeResourceIds; }

    public void setAccessibleComputeResourceIds(List<String> accessibleComputeResourceIds) { this.accessibleComputeResourceIds = accessibleComputeResourceIds; }

    public String getGatewayId() { return gatewayId; }

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

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getAppDes() {
        return appDes;
    }

    public void setAppDes(String appDes) {
        this.appDes = appDes;
    }

    public ComputeResourceResource getHostResource() {
        return hostResource;
    }

    public void setHostResource(ComputeResourceResource hostResource) {
        this.hostResource = hostResource;
    }

    public AppModuleResource getModuleResource() {
        return moduleResource;
    }

    public void setModuleResource(AppModuleResource moduleResource) {
        this.moduleResource = moduleResource;
    }

    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    public void setDefaultQueueName(String defaultQueueName) {
        this.defaultQueueName = defaultQueueName;
    }

    public int getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(int defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public int getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(int defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public boolean isEditableByUser() {
        return editableByUser;
    }

    public void setEditableByUser(boolean editableByUser) {
        this.editableByUser = editableByUser;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(APPLICATION_DEPLOYMENT);
            generator.setParameter(ApplicationDeploymentConstants.DEPLOYMENT_ID, identifier);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_DEPLOYMENT);
            generator.setParameter(ApplicationDeploymentConstants.DEPLOYMENT_ID, identifier);
            Query q = generator.selectQuery(em);
            ApplicationDeployment deployment = (ApplicationDeployment) q.getSingleResult();
            AppDeploymentResource deploymentResource =
                    (AppDeploymentResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_DEPLOYMENT, deployment);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return deploymentResource;
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
        List<AppCatalogResource> appDeployments = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_DEPLOYMENT);
            List results;
            if (fieldName.equals(ApplicationDeploymentConstants.APP_MODULE_ID)) {
                generator.setParameter(ApplicationDeploymentConstants.APP_MODULE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationDeployment deployment = (ApplicationDeployment) result;
                        AppDeploymentResource deploymentResource =
                                (AppDeploymentResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_DEPLOYMENT, deployment);
                        appDeployments.add(deploymentResource);
                    }
                }
            } else if (fieldName.equals(ApplicationDeploymentConstants.COMPUTE_HOST_ID)) {
                generator.setParameter(ApplicationDeploymentConstants.COMPUTE_HOST_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationDeployment deployment = (ApplicationDeployment) result;
                        AppDeploymentResource deploymentResource =
                                (AppDeploymentResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_DEPLOYMENT, deployment);
                        appDeployments.add(deploymentResource);
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
                logger.error("Unsupported field name for app deployment resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for app deployment resource.");
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
        return appDeployments;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> appDeployments = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_DEPLOYMENT);
            generator.setParameter(ApplicationDeploymentConstants.GATEWAY_ID, gatewayId);
            if (accessibleApplicationDeploymentIds != null && !accessibleApplicationDeploymentIds.isEmpty()) {
                generator.setParameter(ApplicationDeploymentConstants.DEPLOYMENT_ID, accessibleApplicationDeploymentIds);
            }
            if (accessibleComputeResourceIds != null && !accessibleComputeResourceIds.isEmpty()) {
                generator.setParameter(ApplicationDeploymentConstants.COMPUTE_HOST_ID, accessibleComputeResourceIds);
            }
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationDeployment deployment = (ApplicationDeployment) result;
                        AppDeploymentResource deploymentResource =
                                (AppDeploymentResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_DEPLOYMENT, deployment);
                        appDeployments.add(deploymentResource);
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
        return appDeployments;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        List<String> appDeployments = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_DEPLOYMENT);
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    ApplicationDeployment deployment = (ApplicationDeployment) result;
                    appDeployments.add(deployment.getDeploymentID());
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
        return appDeployments;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> appDeployments = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_DEPLOYMENT);
            List results;
            if (fieldName.equals(ApplicationDeploymentConstants.APP_MODULE_ID)) {
                generator.setParameter(ApplicationDeploymentConstants.APP_MODULE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationDeployment deployment = (ApplicationDeployment) result;
                        appDeployments.add(deployment.getDeploymentID());
                    }
                }
            } else if (fieldName.equals(ApplicationDeploymentConstants.COMPUTE_HOST_ID)) {
                generator.setParameter(ApplicationDeploymentConstants.COMPUTE_HOST_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationDeployment deployment = (ApplicationDeployment) result;
                        appDeployments.add(deployment.getDeploymentID());
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
                logger.error("Unsupported field name for app deployment resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for app deployment resource.");
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
        return appDeployments;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationDeployment existingDeployment = em.find(ApplicationDeployment.class, deploymentId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ApplicationModule applicationModule = em.find(ApplicationModule.class, appModuleId);
            ComputeResource computeHost = em.find(ComputeResource.class, hostId);
            if (existingDeployment !=  null){
                existingDeployment.setDeploymentID(deploymentId);
                existingDeployment.setApplicationDesc(appDes);
                existingDeployment.setAppModuleID(appModuleId);
                existingDeployment.setApplicationModule(applicationModule);
                existingDeployment.setComputeResource(computeHost);
                existingDeployment.setHostID(hostId);
                existingDeployment.setExecutablePath(executablePath);
                existingDeployment.setParallelism(parallelism);
                existingDeployment.setGatewayId(gatewayId);
                existingDeployment.setDefaultQueueName(defaultQueueName);
                existingDeployment.setDefaultCPUCount(defaultCPUCount);
                existingDeployment.setDefaultNodeCount(defaultNodeCount);
                existingDeployment.setDefaultWalltime(defaultWalltime);
                existingDeployment.setEditableByUser(editableByUser);
                existingDeployment.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                em.merge(existingDeployment);
            }else {
                ApplicationDeployment deployment  = new ApplicationDeployment();
                deployment.setApplicationDesc(appDes);
                deployment.setDeploymentID(deploymentId);
                deployment.setAppModuleID(appModuleId);
                deployment.setHostID(hostId);
                deployment.setApplicationModule(applicationModule);
                deployment.setComputeResource(computeHost);
                deployment.setExecutablePath(executablePath);
                deployment.setParallelism(parallelism);
                deployment.setGatewayId(gatewayId);
                deployment.setDefaultQueueName(defaultQueueName);
                deployment.setDefaultCPUCount(defaultCPUCount);
                deployment.setDefaultNodeCount(defaultNodeCount);
                deployment.setDefaultWalltime(defaultWalltime);
                deployment.setEditableByUser(editableByUser);
                deployment.setCreationTime(AiravataUtils.getCurrentTimestamp());
                em.persist(deployment);
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
            ApplicationDeployment deployment = em.find(ApplicationDeployment.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return deployment != null;
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

	public String getParallelism() {
		return parallelism;
	}

	public void setParallelism(String parallelism) {
		this.parallelism = parallelism;
	}
}
