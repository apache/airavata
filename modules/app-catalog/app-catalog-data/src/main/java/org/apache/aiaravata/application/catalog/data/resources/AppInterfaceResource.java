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
import org.apache.aiaravata.application.catalog.data.model.ApplicationInterface;
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

public class AppInterfaceResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(AppInterfaceResource.class);
    private String interfaceId;
    private String appName;

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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INTERFACE);
            generator.setParameter(ApplicationInterfaceConstants.INTERFACE_ID, identifier);
            Query q = generator.selectQuery(em);
            ApplicationInterface applicationInterface = (ApplicationInterface) q.getSingleResult();
            AppInterfaceResource resource =
                    (AppInterfaceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_INTERFACE, applicationInterface);
            em.getTransaction().commit();
            em.close();
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
    public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
        List<Resource> resourceList = new ArrayList<Resource>();
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
                em.close();
                logger.error("Unsupported field name for application interface.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for application interface.");
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
                em.close();
                logger.error("Unsupported field name for application interface.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for application interface.");
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
        return resourceList;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationInterface existigAppInterface = em.find(ApplicationInterface.class, interfaceId);
            em.close();

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();

            if (existigAppInterface !=  null){
                existigAppInterface.setAppName(appName);
                em.merge(existigAppInterface);
            }else {
                ApplicationInterface applicationInterface = new ApplicationInterface();
                applicationInterface.setInterfaceID(interfaceId);
                applicationInterface.setAppName(appName);
                em.persist(applicationInterface);
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
            ApplicationInterface existigAppInterface = em.find(ApplicationInterface.class, identifier);
            em.close();
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
