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
import org.apache.airavata.registry.core.app.catalog.model.AppOutput_PK;
import org.apache.airavata.registry.core.app.catalog.model.ApplicationIntOutput;
import org.apache.airavata.registry.core.app.catalog.model.ApplicationInterface;
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

public class ApplicationOutputResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationOutputResource.class);

    private String interfaceID;
    private String outputKey;
    private String outputVal;
    private String dataType;
    private boolean isRequired;
    private boolean dataMovement;
    private String dataNameLocation;
    private boolean requiredToCMD;
    private String searchQuery;
    private String appArgument;
    private boolean outputStreaming;

    private AppInterfaceResource appInterfaceResource;

    public void remove(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            generator.setParameter(AppOutputConstants.INTERFACE_ID, ids.get(AppOutputConstants.INTERFACE_ID));
            if (ids.get(AppOutputConstants.OUTPUT_KEY) != null){
                generator.setParameter(AppOutputConstants.OUTPUT_KEY, ids.get(AppOutputConstants.OUTPUT_KEY));
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

    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;

        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            generator.setParameter(AppOutputConstants.INTERFACE_ID, ids.get(AppOutputConstants.INTERFACE_ID));
            generator.setParameter(AppOutputConstants.OUTPUT_KEY, ids.get(AppOutputConstants.OUTPUT_KEY));
            Query q = generator.selectQuery(em);
            ApplicationIntOutput applicationOutput = (ApplicationIntOutput) q.getSingleResult();
            ApplicationOutputResource applicationOutputResource =
                    (ApplicationOutputResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_OUTPUT
                            , applicationOutput);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return applicationOutputResource;
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
        List<AppCatalogResource> appInputResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            List results;
            if (fieldName.equals(AppOutputConstants.INTERFACE_ID)) {
                generator.setParameter(AppOutputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntOutput applicationOutput = (ApplicationIntOutput) result;
                        ApplicationOutputResource applicationOutputResource =
                                (ApplicationOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_OUTPUT, applicationOutput);
                        appInputResources.add(applicationOutputResource);
                    }
                }
            } else if (fieldName.equals(AppOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(AppOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntOutput applicationOutput = (ApplicationIntOutput) result;
                        ApplicationOutputResource applicationOutputResource =
                                (ApplicationOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_OUTPUT, applicationOutput);
                        appInputResources.add(applicationOutputResource);
                    }
                }
            } else if (fieldName.equals(AppOutputConstants.DATA_TYPE)) {
                generator.setParameter(AppOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntOutput applicationOutput = (ApplicationIntOutput) result;
                        ApplicationOutputResource applicationOutputResource =
                                (ApplicationOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_OUTPUT, applicationOutput);
                        appInputResources.add(applicationOutputResource);
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
                logger.error("Unsupported field name for App Output Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for App Output Resource.");
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
        return appInputResources;
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
        List<String> appOutputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_OUTPUT);
            List results;
            if (fieldName.equals(AppOutputConstants.INTERFACE_ID)) {
                generator.setParameter(AppOutputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntOutput applicationOutput = (ApplicationIntOutput) result;
                        appOutputResourceIDs.add(applicationOutput.getInterfaceID());
                    }
                }
            }
            if (fieldName.equals(AppOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(AppOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntOutput applicationOutput = (ApplicationIntOutput) result;
                        appOutputResourceIDs.add(applicationOutput.getInterfaceID());
                    }
                }
            } else if (fieldName.equals(AppOutputConstants.DATA_TYPE)) {
                generator.setParameter(AppOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntOutput applicationOutput = (ApplicationIntOutput) result;
                        appOutputResourceIDs.add(applicationOutput.getInterfaceID());
                    }
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for App Output resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for App Output Resource.");
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
        return appOutputResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationIntOutput existingApplicationOutput = em.find(ApplicationIntOutput.class,
                    new AppOutput_PK(interfaceID, outputKey));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            ApplicationIntOutput applicationOutput;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingApplicationOutput == null) {
                applicationOutput = new ApplicationIntOutput();
            } else {
                applicationOutput = existingApplicationOutput;
            }
            ApplicationInterface applicationInterface = em.find(ApplicationInterface.class, interfaceID);
            applicationOutput.setApplicationInterface(applicationInterface);
            applicationOutput.setInterfaceID(applicationInterface.getInterfaceID());
            applicationOutput.setDataType(dataType);
            applicationOutput.setOutputKey(outputKey);
            applicationOutput.setOutputVal(outputVal);
            applicationOutput.setRequired(isRequired);
            applicationOutput.setRequiredToCMD(requiredToCMD);
            applicationOutput.setDataMovement(dataMovement);
            applicationOutput.setDataNameLocation(dataNameLocation);
            applicationOutput.setSearchQuery(searchQuery);
            applicationOutput.setApplicationArgument(appArgument);
            applicationOutput.setOutputStreaming(outputStreaming);
            em.merge(applicationOutput);
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

    public boolean isExists(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationIntOutput applicationOutput = em.find(ApplicationIntOutput.class, new AppOutput_PK(
                    ids.get(AppOutputConstants.INTERFACE_ID),
                    ids.get(AppOutputConstants.OUTPUT_KEY)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return applicationOutput != null;
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

    public String getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(String interfaceID) {
        this.interfaceID = interfaceID;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getOutputVal() {
        return outputVal;
    }

    public void setOutputVal(String outputVal) {
        this.outputVal = outputVal;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public AppInterfaceResource getAppInterfaceResource() {
        return appInterfaceResource;
    }

    public void setAppInterfaceResource(AppInterfaceResource appInterfaceResource) {
        this.appInterfaceResource = appInterfaceResource;
    }

    public boolean getRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
    }

    public boolean getRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public boolean isOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }
}
