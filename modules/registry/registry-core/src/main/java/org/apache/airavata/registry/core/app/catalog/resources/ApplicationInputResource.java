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
import org.apache.airavata.registry.core.app.catalog.model.AppInput_PK;
import org.apache.airavata.registry.core.app.catalog.model.ApplicationIntInput;
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

public class ApplicationInputResource extends AppCatAbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationInputResource.class);

    private String interfaceID;
    private String inputKey;
    private String dataType;
    private String inputVal;
    private String metadata;
    private String appArgument;
    private String userFriendlyDesc;
    private int inputOrder;
    private boolean standardInput;
    private boolean isRequired;
    private boolean requiredToCMD;
    private boolean dataStaged;
    private boolean isReadOnly;

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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            generator.setParameter(AppInputConstants.INTERFACE_ID, ids.get(AppInputConstants.INTERFACE_ID));
            if (ids.get(AppInputConstants.INPUT_KEY) != null){
                generator.setParameter(AppInputConstants.INPUT_KEY, ids.get(AppInputConstants.INPUT_KEY));
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
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            generator.setParameter(AppInputConstants.INTERFACE_ID, ids.get(AppInputConstants.INTERFACE_ID));
            generator.setParameter(AppInputConstants.INPUT_KEY, ids.get(AppInputConstants.INPUT_KEY));
            Query q = generator.selectQuery(em);
            ApplicationIntInput applicationInput = (ApplicationIntInput) q.getSingleResult();
            ApplicationInputResource applicationInputResource =
                    (ApplicationInputResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.APPLICATION_INPUT
                            , applicationInput);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return applicationInputResource;
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            List results;
            if (fieldName.equals(AppInputConstants.INTERFACE_ID)) {
                generator.setParameter(AppInputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntInput applicationInput = (ApplicationIntInput) result;
                        ApplicationInputResource applicationInputResource =
                                (ApplicationInputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_INPUT, applicationInput);
                        appInputResources.add(applicationInputResource);
                    }
                }
            } else if (fieldName.equals(AppInputConstants.INPUT_KEY)) {
                generator.setParameter(AppInputConstants.INPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntInput applicationInput = (ApplicationIntInput) result;
                        ApplicationInputResource applicationInputResource =
                                (ApplicationInputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_INPUT, applicationInput);
                        appInputResources.add(applicationInputResource);
                    }
                }
            } else if (fieldName.equals(AppInputConstants.DATA_TYPE)) {
                generator.setParameter(AppInputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntInput applicationInput = (ApplicationIntInput) result;
                        ApplicationInputResource applicationInputResource =
                                (ApplicationInputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.APPLICATION_INPUT, applicationInput);
                        appInputResources.add(applicationInputResource);
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
                logger.error("Unsupported field name for AppInput Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for AppInput Resource.");
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
        List<String> appInputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(APPLICATION_INPUT);
            List results;
            if (fieldName.equals(AppInputConstants.INTERFACE_ID)) {
                generator.setParameter(AppInputConstants.INTERFACE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntInput applicationInput = (ApplicationIntInput) result;
                        appInputResourceIDs.add(applicationInput.getInterfaceID());
                    }
                }
            } else if (fieldName.equals(AppInputConstants.INPUT_KEY)) {
                generator.setParameter(AppInputConstants.INPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntInput applicationInput = (ApplicationIntInput) result;
                        appInputResourceIDs.add(applicationInput.getInterfaceID());
                    }
                }
            } else if (fieldName.equals(AppInputConstants.DATA_TYPE)) {
                generator.setParameter(AppInputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ApplicationIntInput applicationInput = (ApplicationIntInput) result;
                        appInputResourceIDs.add(applicationInput.getInterfaceID());
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
                logger.error("Unsupported field name for AppInput resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for AppInput Resource.");
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
        return appInputResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            ApplicationIntInput existingApplicationInput = em.find(ApplicationIntInput.class, new AppInput_PK(interfaceID, inputKey));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            ApplicationIntInput applicationInput;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingApplicationInput == null) {
                applicationInput = new ApplicationIntInput();
            } else {
            	applicationInput=existingApplicationInput;
            }
            ApplicationInterface applicationInterface = em.find(ApplicationInterface.class, interfaceID);
            applicationInput.setApplicationInterface(applicationInterface);
            applicationInput.setInterfaceID(applicationInterface.getInterfaceID());
            applicationInput.setDataType(dataType);
            applicationInput.setInputKey(inputKey);
            applicationInput.setInputVal(inputVal);
            applicationInput.setMetadata(metadata);
            applicationInput.setAppArgument(appArgument);
            applicationInput.setUserFriendlyDesc(userFriendlyDesc);
            applicationInput.setStandardInput(standardInput);
            applicationInput.setInputOrder(inputOrder);
            applicationInput.setRequiredToCMD(requiredToCMD);
            applicationInput.setRequired(isRequired);
            applicationInput.setDataStaged(dataStaged);
            applicationInput.setReadOnly(isReadOnly);
            if (existingApplicationInput == null) {
                em.persist(applicationInput);
            } else {
                em.merge(applicationInput);
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
            ApplicationIntInput applicationInput = em.find(ApplicationIntInput.class, new AppInput_PK(
                    ids.get(AppInputConstants.INTERFACE_ID),
                    ids.get(AppInputConstants.INPUT_KEY)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return applicationInput != null;
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

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getInputVal() {
        return inputVal;
    }

    public void setInputVal(String inputVal) {
        this.inputVal = inputVal;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public String getUserFriendlyDesc() {
        return userFriendlyDesc;
    }

    public void setUserFriendlyDesc(String userFriendlyDesc) {
        this.userFriendlyDesc = userFriendlyDesc;
    }

    public AppInterfaceResource getAppInterfaceResource() {
        return appInterfaceResource;
    }

    public void setAppInterfaceResource(AppInterfaceResource appInterfaceResource) {
        this.appInterfaceResource = appInterfaceResource;
    }

    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public boolean getRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean getRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public boolean isDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
}
