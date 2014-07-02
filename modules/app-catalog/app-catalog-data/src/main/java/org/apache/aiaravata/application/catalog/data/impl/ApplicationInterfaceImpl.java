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

package org.apache.aiaravata.application.catalog.data.impl;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ApplicationInterface;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationInterfaceImpl implements ApplicationInterface {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationInterfaceImpl.class);

    @Override
    public String addApplicationModule(ApplicationModule applicationModule) throws AppCatalogException {
        try {
            AppModuleResource moduleResource = new AppModuleResource();
            moduleResource.setModuleName(applicationModule.getAppModuleName());
            moduleResource.setModuleId(AppCatalogUtils.getID(applicationModule.getAppModuleName()));
            moduleResource.setModuleDesc(applicationModule.getAppModuleDescription());
            moduleResource.setModuleVersion(applicationModule.getAppModuleVersion());
            moduleResource.save();
            applicationModule.setAppModuleId(moduleResource.getModuleId());
            return moduleResource.getModuleId();
        }catch (Exception e) {
            logger.error("Error while saving application module...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addApplicationInterface(ApplicationInterfaceDescription applicationInterfaceDescription) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            resource.setAppName(applicationInterfaceDescription.getApplicationName());
            resource.setInterfaceId(AppCatalogUtils.getID(applicationInterfaceDescription.getApplicationName()));
            resource.save();
            applicationInterfaceDescription.setApplicationInterfaceId(resource.getInterfaceId());

            List<String> applicationModules = applicationInterfaceDescription.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules){
                    AppModuleResource appModuleResource = new AppModuleResource();
                    AppModuleMappingResource moduleMappingResource = new AppModuleMappingResource();
                    moduleMappingResource.setInterfaceId(resource.getInterfaceId());
                    moduleMappingResource.setModuleId(moduleId);
                    moduleMappingResource.setModuleResource((AppModuleResource)appModuleResource.get(moduleId));
                    moduleMappingResource.setAppInterfaceResource(resource);
                    moduleMappingResource.save();
                }
            }

            List<InputDataObjectType> applicationInputs = applicationInterfaceDescription.getApplicationInputs();
            if (applicationInputs != null && !applicationInputs.isEmpty()){
                for (InputDataObjectType input : applicationInputs){
                    ApplicationInputResource inputResource = new ApplicationInputResource();
                    inputResource.setAppInterfaceResource(resource);
                    inputResource.setInterfaceID(resource.getInterfaceId());
                    inputResource.setUserFriendlyDesc(input.getUserFriendlyDescription());
                    inputResource.setInputKey(input.getName());
                    inputResource.setInputVal(input.getValue());
                    inputResource.setDataType(input.getType().toString());
                    inputResource.setMetadata(input.getMetaData());
                    inputResource.setStandareInput(input.isStandardInput());
                    inputResource.setAppArgument(input.getApplicationArgument());
                    inputResource.save();
                }
            }

            List<OutputDataObjectType> applicationOutputs = applicationInterfaceDescription.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()){
                for (OutputDataObjectType output : applicationOutputs){
                    ApplicationOutputResource outputResource = new ApplicationOutputResource();
                    outputResource.setInterfaceID(resource.getInterfaceId());
                    outputResource.setAppInterfaceResource(resource);
                    outputResource.setOutputKey(output.getName());
                    outputResource.setOutputVal(output.getValue());
                    outputResource.setDataType(output.getType().toString());
                    outputResource.save();
                }
            }
            return resource.getInterfaceId();
        }catch (Exception e) {
            logger.error("Error while saving application interface...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addApplicationModuleMapping(String moduleId, String interfaceId) throws AppCatalogException {
        try {
            AppModuleResource appModuleResource = new AppModuleResource();
            AppInterfaceResource interfaceResource = new AppInterfaceResource();
            AppModuleMappingResource moduleMappingResource = new AppModuleMappingResource();
            moduleMappingResource.setInterfaceId(interfaceId);
            moduleMappingResource.setModuleId(moduleId);
            moduleMappingResource.setModuleResource((AppModuleResource)appModuleResource.get(moduleId));
            moduleMappingResource.setAppInterfaceResource((AppInterfaceResource)interfaceResource.get(interfaceId));
            moduleMappingResource.save();
        }catch (Exception e) {
            logger.error("Error while saving application module mapping...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateApplicationModule(String moduleId, ApplicationModule updatedModule) throws AppCatalogException {
        try {
            AppModuleResource moduleResource = new AppModuleResource();
            AppModuleResource existingModule = (AppModuleResource)moduleResource.get(moduleId);
            existingModule.setModuleName(updatedModule.getAppModuleName());
            existingModule.setModuleDesc(updatedModule.getAppModuleDescription());
            existingModule.setModuleVersion(updatedModule.getAppModuleVersion());
            existingModule.save();
        }catch (Exception e) {
            logger.error("Error while updating application module...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateApplicationInterface(String interfaceId, ApplicationInterfaceDescription updatedInterface) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            AppInterfaceResource existingInterface = (AppInterfaceResource)resource.get(interfaceId);
            existingInterface.setAppName(updatedInterface.getApplicationName());
            existingInterface.save();

            List<String> applicationModules = updatedInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules){
                    AppModuleResource appModuleResource = new AppModuleResource();
                    AppModuleMappingResource moduleMappingResource = new AppModuleMappingResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.AppModuleMappingConstants.MODULE_ID, moduleId);
                    ids.put(AbstractResource.AppModuleMappingConstants.INTERFACE_ID, interfaceId);
                    AppModuleMappingResource existingMapping = (AppModuleMappingResource)moduleMappingResource.get(ids);
                    existingMapping.setInterfaceId(interfaceId);
                    existingMapping.setModuleId(moduleId);
                    existingMapping.setModuleResource((AppModuleResource)appModuleResource.get(moduleId));
                    existingMapping.setAppInterfaceResource(existingInterface);
                    existingMapping.save();
                }
            }

            List<InputDataObjectType> applicationInputs = updatedInterface.getApplicationInputs();
            if (applicationInputs != null && !applicationInputs.isEmpty()){
                for (InputDataObjectType input : applicationInputs){
                    ApplicationInputResource inputResource = new ApplicationInputResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
                    ids.put(AbstractResource.AppInputConstants.INPUT_KEY, input.getName());
                    ApplicationInputResource existingResource = (ApplicationInputResource)inputResource.get(ids);
                    existingResource.setAppInterfaceResource(existingInterface);
                    existingResource.setInterfaceID(interfaceId);
                    existingResource.setUserFriendlyDesc(input.getUserFriendlyDescription());
                    existingResource.setInputKey(input.getName());
                    existingResource.setInputVal(input.getValue());
                    existingResource.setDataType(input.getType().toString());
                    existingResource.setMetadata(input.getMetaData());
                    existingResource.setStandareInput(input.isStandardInput());
                    existingResource.setAppArgument(input.getApplicationArgument());
                    existingResource.save();
                }
            }

            List<OutputDataObjectType> applicationOutputs = updatedInterface.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()){
                for (OutputDataObjectType output : applicationOutputs){
                    ApplicationOutputResource outputResource = new ApplicationOutputResource();
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
                    ids.put(AbstractResource.AppOutputConstants.OUTPUT_KEY, output.getName());
                    ApplicationOutputResource existingResource = (ApplicationOutputResource)outputResource.get(ids);
                    existingResource.setInterfaceID(interfaceId);
                    existingResource.setAppInterfaceResource(existingInterface);
                    existingResource.setOutputKey(output.getName());
                    existingResource.setOutputVal(output.getValue());
                    existingResource.setDataType(output.getType().toString());
                    existingResource.save();
                }
            }
        }catch (Exception e) {
            logger.error("Error while updating application interface...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ApplicationModule getApplicationModule(String moduleId) throws AppCatalogException {
        try {
            AppModuleResource appModuleResource = new AppModuleResource();
            return AppCatalogThriftConversion.getApplicationModuleDesc((AppModuleResource) appModuleResource.get(moduleId));
        }catch (Exception e) {
            logger.error("Error while retrieving application module...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        try {
            AppInterfaceResource interfaceResource = new AppInterfaceResource();
            return AppCatalogThriftConversion.getApplicationInterfaceDescription((AppInterfaceResource)interfaceResource.get(interfaceId));
        }catch (Exception e) {
            logger.error("Error while retrieving application interface...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        try {
            AppModuleResource resource = new AppModuleResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.ApplicationModuleConstants.MODULE_NAME)){
                    List<Resource> resources = resource.get(AbstractResource.ApplicationModuleConstants.MODULE_NAME, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getAppModules(resources);
                    }
                }else {
                    logger.error("Unsupported field name for app module.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for app module.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving app module list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.ApplicationInterfaceConstants.APPLICATION_NAME)){
                    List<Resource> resources = resource.get(AbstractResource.ApplicationInterfaceConstants.APPLICATION_NAME, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getAppInterfaceDescList(resources);
                    }
                }else {
                    logger.error("Unsupported field name for app interface.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for app interface.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving app interface list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public boolean removeApplicationInterface(String interfaceId) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            resource.remove(interfaceId);
            return true;
        }catch (Exception e){
            logger.error("Error while removing app interface...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean removeApplicationModule(String moduleId) throws AppCatalogException {
        try {
            AppModuleResource resource = new AppModuleResource();
            resource.remove(moduleId);
            return true;
        }catch (Exception e){
            logger.error("Error while removing app module...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            return resource.isExists(interfaceId);
        }catch (Exception e){
            logger.error("Error while retrieving app interface...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean isApplicationModuleExists(String moduleId) throws AppCatalogException {
        try {
            AppModuleResource resource = new AppModuleResource();
            return resource.isExists(moduleId);
        }catch (Exception e){
            logger.error("Error while retrieving app module...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<InputDataObjectType> getApplicationInputs(String interfaceId) throws AppCatalogException {
        try {
            ApplicationInputResource resource = new ApplicationInputResource();
            List<Resource> resources = resource.get(AbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
            return AppCatalogThriftConversion.getAppInputs(resources);
        }catch (Exception e){
            logger.error("Error while retrieving app inputs...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException {
        try {
            ApplicationOutputResource resource = new ApplicationOutputResource();
            List<Resource> resources = resource.get(AbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
            return AppCatalogThriftConversion.getAppOutputs(resources);
        }catch (Exception e){
            logger.error("Error while retrieving app outputs...", e);
            throw new AppCatalogException(e);
        }
    }
}
