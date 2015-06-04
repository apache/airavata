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
import org.apache.aiaravata.application.catalog.data.model.PostJobCommand;
import org.apache.aiaravata.application.catalog.data.model.PostJobCommandPK;
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

public class PostJobCommandResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(PostJobCommandResource.class);

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
            AppCatalogQueryGenerator generator= new AppCatalogQueryGenerator(POST_JOBCOMMAND);
            generator.setParameter(PostJobCommandConstants.DEPLOYMENT_ID,
                    ids.get(PostJobCommandConstants.DEPLOYMENT_ID));
            if (ids.get(PostJobCommandConstants.COMMAND) != null){
                generator.setParameter(PostJobCommandConstants.COMMAND, ids.get(PostJobCommandConstants.COMMAND));
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(POST_JOBCOMMAND);
            generator.setParameter(PostJobCommandConstants.DEPLOYMENT_ID,
                    ids.get(PostJobCommandConstants.DEPLOYMENT_ID));
            generator.setParameter(PostJobCommandConstants.COMMAND, ids.get(PostJobCommandConstants.COMMAND));
            Query q = generator.selectQuery(em);
            PostJobCommand postJobCommand = (PostJobCommand) q.getSingleResult();
            PostJobCommandResource postJobCommandResource =
                    (PostJobCommandResource) AppCatalogJPAUtils.getResource(
                            AppCatalogResourceType.POST_JOBCOMMAND, postJobCommand);
            em.getTransaction().commit();
            em.close();
            return postJobCommandResource;
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
        List<Resource> gsiSSHPostJobCommandResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(POST_JOBCOMMAND);
            List results;
            if (fieldName.equals(PostJobCommandConstants.DEPLOYMENT_ID)) {
                generator.setParameter(PostJobCommandConstants.DEPLOYMENT_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PostJobCommand postJobCommand = (PostJobCommand) result;
                        PostJobCommandResource postJobCommandResource =
                                (PostJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.POST_JOBCOMMAND, postJobCommand);
                        gsiSSHPostJobCommandResources.add(postJobCommandResource);
                    }
                }
            } else if (fieldName.equals(PostJobCommandConstants.COMMAND)) {
                generator.setParameter(PostJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PostJobCommand postJobCommand = (PostJobCommand) result;
                        PostJobCommandResource postJobCommandResource =
                                (PostJobCommandResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.POST_JOBCOMMAND, postJobCommand);
                        gsiSSHPostJobCommandResources.add(postJobCommandResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Post Job Command Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Post Job Command Resource.");
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
        return gsiSSHPostJobCommandResources;
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
        List<String> gsiSSHPostJobResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(POST_JOBCOMMAND);
            List results;
            if (fieldName.equals(PostJobCommandConstants.DEPLOYMENT_ID)) {
                generator.setParameter(PostJobCommandConstants.DEPLOYMENT_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PostJobCommand postJobCommand = (PostJobCommand) result;
                        gsiSSHPostJobResourceIDs.add(postJobCommand.getDeploymentId());
                    }
                }
            } else if (fieldName.equals(PostJobCommandConstants.COMMAND)) {
                generator.setParameter(PostJobCommandConstants.COMMAND, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        PostJobCommand postJobCommand = (PostJobCommand) result;
                        gsiSSHPostJobResourceIDs.add(postJobCommand.getDeploymentId());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for GSISSH Post Job resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for GSISSH Post JOb Resource.");
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
        return gsiSSHPostJobResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            PostJobCommand existingPostJobCommand = em.find(PostJobCommand.class,
                    new PostJobCommandPK(appDeploymentId, command));
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ApplicationDeployment deployment = em.find(ApplicationDeployment.class, appDeploymentId);
            if (existingPostJobCommand !=  null){
                existingPostJobCommand.setDeploymentId(appDeploymentId);
                existingPostJobCommand.setCommand(command);
                existingPostJobCommand.setDeployment(deployment);
                em.merge(existingPostJobCommand);
            }else {
                PostJobCommand postJobCommand = new PostJobCommand();
                postJobCommand.setDeploymentId(appDeploymentId);
                postJobCommand.setCommand(command);
                postJobCommand.setDeployment(deployment);
                em.persist(postJobCommand);
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
            PostJobCommand postJobCommand = em.find(PostJobCommand.class, new PostJobCommandPK(
                    ids.get(PostJobCommandConstants.DEPLOYMENT_ID),
                    ids.get(PostJobCommandConstants.COMMAND)));

            em.close();
            return postJobCommand != null;
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
