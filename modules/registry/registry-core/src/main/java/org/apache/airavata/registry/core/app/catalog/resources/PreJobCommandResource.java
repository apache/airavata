/**
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

package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.ApplicationDeployment;
import org.apache.aiaravata.application.catalog.data.model.PreJobCommand;
import org.apache.aiaravata.application.catalog.data.model.PreJobCommandPK;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreJobCommandResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(PreJobCommandResource.class);

    private String appDeploymentId;
    private String command;

    private AppDeploymentResource appDeploymentResource;


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
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(PRE_JOBCOMMAND);
            generator.setParameter(PreJobCommandConstants.DEPLOYMENT_ID,
                    ids.get(PreJobCommandConstants.DEPLOYMENT_ID));
            if (ids.get(PreJobCommandConstants.COMMAND) != null){
                generator.setParameter(PreJobCommandConstants.COMMAND, ids.get(PreJobCommandConstants.COMMAND));
            }
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

    public Resource get(Object identifier) throws AppCatalogException {
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PRE_JOBCOMMAND);
            generator.setParameter(PreJobCommandConstants.DEPLOYMENT_ID,
                    ids.get(PreJobCommandConstants.DEPLOYMENT_ID));
            generator.setParameter(PreJobCommandConstants.COMMAND, ids.get(PreJobCommandConstants.COMMAND));
            Query q = generator.selectQuery(em);
            PreJobCommand preJobCommand = (PreJobCommand) q.getSingleResult();
            PreJobCommandResource preJobCommandResource =
                    (PreJobCommandResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.PRE_JOBCOMMAND, preJobCommand);
            em.getTransaction().commit();
            em.close();
            return preJobCommandResource;
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

    public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
        List<Resource> gsiSSHPreJobResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PRE_JOBCOMMAND);
            List results;
            if (fieldName.equals(PreJobCommandConstants.DEPLOYMENT_ID)) {
                generator.setParameter(PreJobCommandConstants.DEPLOYMENT_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PreJobCommand preJobCommand = (PreJobCommand) result;
                        PreJobCommandResource preJobCommandResource =
                                (PreJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.PRE_JOBCOMMAND, preJobCommand);
                        gsiSSHPreJobResources.add(preJobCommandResource);
                    }
                }
            } else if (fieldName.equals(PreJobCommandConstants.COMMAND)) {
                generator.setParameter(PreJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PreJobCommand preJobCommand = (PreJobCommand) result;
                        PreJobCommandResource preJobCommandResource =
                                (PreJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.PRE_JOBCOMMAND, preJobCommand);
                        gsiSSHPreJobResources.add(preJobCommandResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Pre Job Command Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Pre Job Command Resource.");
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
        return gsiSSHPreJobResources;
    }

    @Override
    public List<Resource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> gsiSSHPreJobResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(PRE_JOBCOMMAND);
            List results;
            if (fieldName.equals(PreJobCommandConstants.DEPLOYMENT_ID)) {
                generator.setParameter(PreJobCommandConstants.DEPLOYMENT_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PreJobCommand preJobCommand = (PreJobCommand) result;
                        gsiSSHPreJobResourceIDs.add(preJobCommand.getDeploymentId());
                    }
                }
            } else if (fieldName.equals(PreJobCommandConstants.COMMAND)) {
                generator.setParameter(PreJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PreJobCommand preJobCommand = (PreJobCommand) result;
                        gsiSSHPreJobResourceIDs.add(preJobCommand.getDeploymentId());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Pre Job resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Pre JOb Resource.");
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
        return gsiSSHPreJobResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            PreJobCommand existingGSIsshPreJobCommand = em.find(PreJobCommand.class,
                    new PreJobCommandPK(appDeploymentId, command));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ApplicationDeployment deployment = em.find(ApplicationDeployment.class, appDeploymentId);
            if (existingGSIsshPreJobCommand !=  null){
                existingGSIsshPreJobCommand.setDeploymentId(appDeploymentId);
                existingGSIsshPreJobCommand.setCommand(command);
                existingGSIsshPreJobCommand.setApplicationDeployment(deployment);
                em.merge(existingGSIsshPreJobCommand);
            }else {
                PreJobCommand preJobCommand = new PreJobCommand();
                preJobCommand.setDeploymentId(appDeploymentId);
                preJobCommand.setCommand(command);
                preJobCommand.setApplicationDeployment(deployment);
                em.persist(preJobCommand);
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
            PreJobCommand preJobCommand = em.find(PreJobCommand.class, new PreJobCommandPK(
                    ids.get(PreJobCommandConstants.DEPLOYMENT_ID),
                    ids.get(PreJobCommandConstants.COMMAND)));

            em.close();
            return preJobCommand != null;
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

    public String getAppDeploymentId() {
        return appDeploymentId;
    }

    public void setAppDeploymentId(String appDeploymentId) {
        this.appDeploymentId = appDeploymentId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public AppDeploymentResource getAppDeploymentResource() {
        return appDeploymentResource;
    }

    public void setAppDeploymentResource(AppDeploymentResource appDeploymentResource) {
        this.appDeploymentResource = appDeploymentResource;
    }
}
