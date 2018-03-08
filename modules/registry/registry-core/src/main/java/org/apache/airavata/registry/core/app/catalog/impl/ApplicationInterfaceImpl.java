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
package org.apache.airavata.registry.core.app.catalog.impl;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.application_interface_modelConstants;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogUtils;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ApplicationInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationInterfaceImpl implements ApplicationInterface {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationInterfaceImpl.class);

    @Override
    public String addApplicationModule(ApplicationModule applicationModule, String gatewayId) throws AppCatalogException {
        try {
            AppModuleResource moduleResource = new AppModuleResource();
            moduleResource.setModuleName(applicationModule.getAppModuleName());
            moduleResource.setGatewayId(gatewayId);
            if (!applicationModule.getAppModuleId().equals("") && !applicationModule.getAppModuleId().equals(application_interface_modelConstants.DEFAULT_ID)){
                moduleResource.setModuleId(applicationModule.getAppModuleId());
            }else {
                moduleResource.setModuleId(AppCatalogUtils.getID(applicationModule.getAppModuleName()));
            }
            moduleResource.setModuleDesc(applicationModule.getAppModuleDescription());
            moduleResource.setModuleVersion(applicationModule.getAppModuleVersion());
            moduleResource.save();
            applicationModule.setAppModuleId(moduleResource.getModuleId());
            return moduleResource.getModuleId();
        }catch (Exception e) {
            logger.error("Error while adding application module "+applicationModule.getAppModuleName()+" ["+applicationModule.getAppModuleVersion()+"]", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String addApplicationInterface(ApplicationInterfaceDescription applicationInterfaceDescription, String gatewayId) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            resource.setAppName(applicationInterfaceDescription.getApplicationName());
            if (!applicationInterfaceDescription.getApplicationInterfaceId().equals("") && !applicationInterfaceDescription.getApplicationInterfaceId().equals(application_interface_modelConstants.DEFAULT_ID)){
                resource.setInterfaceId(applicationInterfaceDescription.getApplicationInterfaceId());
            }else {
                resource.setInterfaceId(AppCatalogUtils.getID(applicationInterfaceDescription.getApplicationName()));
            }
            resource.setAppDescription(applicationInterfaceDescription.getApplicationDescription());
            resource.setGatewayId(gatewayId);
            resource.setArchiveWorkingDirectory(applicationInterfaceDescription.isArchiveWorkingDirectory());
            resource.setHasOptionalFileInputs(applicationInterfaceDescription.isHasOptionalFileInputs());
            resource.save();
            applicationInterfaceDescription.setApplicationInterfaceId(resource.getInterfaceId());

            List<String> applicationModules = applicationInterfaceDescription.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules){
                    AppModuleResource appModuleResource = new AppModuleResource();
                    AppModuleMappingAppCatalogResourceAppCat moduleMappingResource = new AppModuleMappingAppCatalogResourceAppCat();
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
                    inputResource.setStandardInput(input.isStandardInput());
                    inputResource.setAppArgument(input.getApplicationArgument());
                    inputResource.setInputOrder(input.getInputOrder());
                    inputResource.setRequired(input.isIsRequired());
                    inputResource.setRequiredToCMD(input.isRequiredToAddedToCommandLine());
                    inputResource.save();
                }
            }

            List<OutputDataObjectType> applicationOutputs = applicationInterfaceDescription.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType output : applicationOutputs) {
                    ApplicationOutputResource outputResource = new ApplicationOutputResource();
                    outputResource.setInterfaceID(resource.getInterfaceId());
                    outputResource.setAppInterfaceResource(resource);
                    outputResource.setOutputKey(output.getName());
                    outputResource.setOutputVal(output.getValue());
                    outputResource.setDataType(output.getType().toString());
                    outputResource.setRequired(output.isIsRequired());
                    outputResource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
                    outputResource.setDataMovement(output.isDataMovement());
                    outputResource.setDataNameLocation(output.getLocation());
                    outputResource.setAppArgument(output.getApplicationArgument());
                    outputResource.setSearchQuery(output.getSearchQuery());
                    outputResource.setOutputStreaming(output.isOutputStreaming());
                    outputResource.save();
                }
            }
            return resource.getInterfaceId();
        }catch (Exception e) {
            logger.error("Error while adding application interface "+applicationInterfaceDescription.getApplicationName(), e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void addApplicationModuleMapping(String moduleId, String interfaceId) throws AppCatalogException {
        try {
            AppModuleResource appModuleResource = new AppModuleResource();
            AppInterfaceResource interfaceResource = new AppInterfaceResource();
            AppModuleMappingAppCatalogResourceAppCat moduleMappingResource = new AppModuleMappingAppCatalogResourceAppCat();
            moduleMappingResource.setInterfaceId(interfaceId);
            moduleMappingResource.setModuleId(moduleId);
            moduleMappingResource.setModuleResource((AppModuleResource)appModuleResource.get(moduleId));
            moduleMappingResource.setAppInterfaceResource((AppInterfaceResource)interfaceResource.get(interfaceId));
            moduleMappingResource.save();
        }catch (Exception e) {
            logger.error("Error while saving application module mapping "+moduleId, e);
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
            logger.error("Error while updating application module "+moduleId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateApplicationInterface(String interfaceId, ApplicationInterfaceDescription updatedInterface) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            AppInterfaceResource existingInterface = (AppInterfaceResource) resource.get(interfaceId);
            existingInterface.setAppName(updatedInterface.getApplicationName());
            existingInterface.setAppDescription(updatedInterface.getApplicationDescription());
            existingInterface.setHasOptionalFileInputs(updatedInterface.isHasOptionalFileInputs());
            existingInterface.setArchiveWorkingDirectory(updatedInterface.isArchiveWorkingDirectory());
            existingInterface.save();

            // remove existing modules before adding
            Map<String, String> ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.AppModuleMappingConstants.INTERFACE_ID, interfaceId);
            AppModuleMappingAppCatalogResourceAppCat moduleMappingResource = new AppModuleMappingAppCatalogResourceAppCat();
            moduleMappingResource.remove(ids);
            List<String> applicationModules = updatedInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()) {
                for (String moduleId : applicationModules) {
                    AppModuleResource appModuleResource = new AppModuleResource();
                    moduleMappingResource = new AppModuleMappingAppCatalogResourceAppCat();
                    ids = new HashMap<String, String>();
                    ids.put(AppCatAbstractResource.AppModuleMappingConstants.MODULE_ID, moduleId);
                    ids.put(AppCatAbstractResource.AppModuleMappingConstants.INTERFACE_ID, interfaceId);
                    AppModuleMappingAppCatalogResourceAppCat existingMapping;
                    if (!moduleMappingResource.isExists(ids)) {
                        existingMapping = new AppModuleMappingAppCatalogResourceAppCat();
                    } else {
                        existingMapping = (AppModuleMappingAppCatalogResourceAppCat) moduleMappingResource.get(ids);
                    }
                    existingMapping.setInterfaceId(interfaceId);
                    existingMapping.setModuleId(moduleId);
                    existingMapping.setModuleResource((AppModuleResource) appModuleResource.get(moduleId));
                    existingMapping.setAppInterfaceResource(existingInterface);
                    existingMapping.save();
                }
            }

            // remove existing application inputs
            ApplicationInputResource inputResource = new ApplicationInputResource();
            ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
            inputResource.remove(ids);
            List<InputDataObjectType> applicationInputs = updatedInterface.getApplicationInputs();
            if (applicationInputs != null && !applicationInputs.isEmpty()) {
                for (InputDataObjectType input : applicationInputs) {
                    inputResource = new ApplicationInputResource();
                    ids = new HashMap<String, String>();
                    ids.put(AppCatAbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
                    ids.put(AppCatAbstractResource.AppInputConstants.INPUT_KEY, input.getName());
                    if (inputResource.isExists(ids)) {
                        inputResource = (ApplicationInputResource) inputResource.get(ids);
                    }
                    inputResource.setAppInterfaceResource(existingInterface);
                    inputResource.setInterfaceID(interfaceId);
                    inputResource.setUserFriendlyDesc(input.getUserFriendlyDescription());
                    inputResource.setInputKey(input.getName());
                    inputResource.setInputVal(input.getValue());
                    inputResource.setDataType(input.getType().toString());
                    inputResource.setMetadata(input.getMetaData());
                    inputResource.setStandardInput(input.isStandardInput());
                    inputResource.setAppArgument(input.getApplicationArgument());
                    inputResource.setInputOrder(input.getInputOrder());
                    inputResource.setRequired(input.isIsRequired());
                    inputResource.setRequiredToCMD(input.isRequiredToAddedToCommandLine());
                    inputResource.setDataStaged(input.isDataStaged());
                    inputResource.setIsReadOnly(input.isIsReadOnly());
                    inputResource.save();
                }
            }

            // remove existing app outputs before adding
            ApplicationOutputResource outputResource = new ApplicationOutputResource();
            ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
            outputResource.remove(ids);
            List<OutputDataObjectType> applicationOutputs = updatedInterface.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType output : applicationOutputs) {
                    outputResource = new ApplicationOutputResource();
                    ids = new HashMap<String, String>();
                    ids.put(AppCatAbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
                    ids.put(AppCatAbstractResource.AppOutputConstants.OUTPUT_KEY, output.getName());
                    if (outputResource.isExists(ids)) {
                        outputResource = (ApplicationOutputResource) outputResource.get(ids);
                    }
                    outputResource.setInterfaceID(interfaceId);
                    outputResource.setAppInterfaceResource(existingInterface);
                    outputResource.setOutputKey(output.getName());
                    outputResource.setOutputVal(output.getValue());
                    outputResource.setDataType(output.getType().toString());
                    outputResource.setRequired(output.isIsRequired());
                    outputResource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
                    outputResource.setDataMovement(output.isDataMovement());
                    outputResource.setDataNameLocation(output.getLocation());
                    outputResource.setAppArgument(output.getApplicationArgument());
                    outputResource.setSearchQuery(output.getSearchQuery());
                    outputResource.setOutputStreaming(output.isOutputStreaming());
                    outputResource.save();
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating application interface " + interfaceId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ApplicationModule getApplicationModule(String moduleId) throws AppCatalogException {
        try {
            AppModuleResource appModuleResource = new AppModuleResource();
            return AppCatalogThriftConversion.getApplicationModuleDesc((AppModuleResource) appModuleResource.get(moduleId));
        }catch (Exception e) {
            logger.error("Error while retrieving application module "+moduleId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        try {
            AppInterfaceResource interfaceResource = new AppInterfaceResource();
            return AppCatalogThriftConversion.getApplicationInterfaceDescription((AppInterfaceResource)interfaceResource.get(interfaceId));
        }catch (Exception e) {
            logger.error("Error while retrieving application interface '"+interfaceId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        List<ApplicationModule> modules = new ArrayList<ApplicationModule>();
        try {
            AppModuleResource resource = new AppModuleResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AppCatAbstractResource.ApplicationModuleConstants.MODULE_NAME)){
                    List<AppCatalogResource> resources = resource.get(AppCatAbstractResource.ApplicationModuleConstants.MODULE_NAME, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        modules = AppCatalogThriftConversion.getAppModules(resources);
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
        return modules;
    }

    @Override
    public List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException {
        List<ApplicationModule> applicationModules = new ArrayList<ApplicationModule>();
        try {
            AppModuleResource resource = new AppModuleResource();
            resource.setGatewayId(gatewayId);
            List<AppCatalogResource> resources = resource.getAll();
            if (resources != null && !resources.isEmpty()){
                applicationModules = AppCatalogThriftConversion.getAppModules(resources);
            }
        }catch (Exception e){
            logger.error("Error while retrieving compute resource list...", e);
            throw new AppCatalogException(e);
        }
        return applicationModules;
    }

    @Override
    public List<ApplicationModule> getAccessibleApplicationModules(String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds) throws AppCatalogException {
        List<ApplicationModule> applicationModules = new ArrayList<ApplicationModule>();
        try {
            AppModuleResource resource = new AppModuleResource();
            resource.setGatewayId(gatewayId);
            resource.setAccessibleApplicationDeploymentIds(accessibleAppIds);
            resource.setAccessibleComputeResourceIds(accessibleComputeResourceIds);
            List<AppCatalogResource> resources = resource.getAll();
            if (resources != null && !resources.isEmpty()){
                applicationModules = AppCatalogThriftConversion.getAppModules(resources);
            }
        }catch (Exception e){
            logger.error("Error while retrieving application module list...", e);
            throw new AppCatalogException(e);
        }
        return applicationModules;
    }

    @Override
    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters) throws AppCatalogException {
        List<ApplicationInterfaceDescription> appInterfaces = new ArrayList<ApplicationInterfaceDescription>();
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AppCatAbstractResource.ApplicationInterfaceConstants.APPLICATION_NAME)){
                    List<AppCatalogResource> resources = resource.get(AppCatAbstractResource.ApplicationInterfaceConstants.APPLICATION_NAME, filters.get(fieldName));
                    appInterfaces = AppCatalogThriftConversion.getAppInterfaceDescList(resources);
                }else {
                    logger.error("Unsupported field name for app interface.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name '"+fieldName+"' for app interface.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving app interface list...", e);
            throw new AppCatalogException(e);
        }
        return appInterfaces;
    }

    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            resource.setGatewayId(gatewayId);
            List<AppCatalogResource> resources = resource.getAll();
            return AppCatalogThriftConversion.getAppInterfaceDescList(resources);
        }catch (Exception e){
            logger.error("Error while retrieving app interface list...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<String> getAllApplicationInterfaceIds() throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            return resource.getAllIds();
        }catch (Exception e){
            logger.error("Error while retrieving app interface list...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean removeApplicationInterface(String interfaceId) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            resource.remove(interfaceId);
            return true;
        }catch (Exception e){
            logger.error("Error while removing app interface "+interfaceId, e);
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
            logger.error("Error while removing app module "+moduleId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException {
        try {
            AppInterfaceResource resource = new AppInterfaceResource();
            return resource.isExists(interfaceId);
        }catch (Exception e){
            logger.error("Error while checking app interface existence "+interfaceId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean isApplicationModuleExists(String moduleId) throws AppCatalogException {
        try {
            AppModuleResource resource = new AppModuleResource();
            return resource.isExists(moduleId);
        }catch (Exception e){
            logger.error("Error while checking app module existence "+moduleId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<InputDataObjectType> getApplicationInputs(String interfaceId) throws AppCatalogException {
        try {
            ApplicationInputResource resource = new ApplicationInputResource();
            List<AppCatalogResource> resources = resource.get(AppCatAbstractResource.AppInputConstants.INTERFACE_ID, interfaceId);
            return AppCatalogThriftConversion.getAppInputs(resources);
        }catch (Exception e){
            logger.error("Error while retrieving app inputs for application "+interfaceId, e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String interfaceId) throws AppCatalogException {
        try {
            ApplicationOutputResource resource = new ApplicationOutputResource();
            List<AppCatalogResource> resources = resource.get(AppCatAbstractResource.AppOutputConstants.INTERFACE_ID, interfaceId);
            return AppCatalogThriftConversion.getAppOutputs(resources);
        }catch (Exception e){
            logger.error("Error while retrieving app outputs for application "+interfaceId, e);
            throw new AppCatalogException(e);
        }
    }
}
