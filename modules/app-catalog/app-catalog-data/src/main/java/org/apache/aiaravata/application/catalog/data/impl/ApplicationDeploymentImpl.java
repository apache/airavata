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
import org.airavata.appcatalog.cpi.ApplicationDeployment;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ApplicationDeploymentImpl implements ApplicationDeployment {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationDeploymentImpl.class);

    @Override
    public String addApplicationDeployment(ApplicationDeploymentDescription deploymentDescription) throws AppCatalogException {
        try {
            AppDeploymentResource deploymentResource = new AppDeploymentResource();
            ComputeHostResource computeHostResource = new ComputeHostResource();
            AppModuleResource moduleResource = new AppModuleResource();
            if (!computeHostResource.isExists(deploymentDescription.getComputeHostId())){
                logger.error("Compute host does not exist in the system. Please create a Compute host first...");
                throw new AppCatalogException("Compute host does not exist in the system. Please create a Compute host first...");
            }
            if (!moduleResource.isExists(deploymentDescription.getAppModuleId())){
                logger.error("Application module does not exist in the system. Please create an application module first...");
                throw new AppCatalogException("Application module does not exist in the system. Please create an application module first...");
            }
            AppModuleResource module = (AppModuleResource)moduleResource.get(deploymentDescription.getAppModuleId());
            deploymentResource.setDeploymentId(AppCatalogUtils.getID(module.getModuleName()));
            deploymentResource.setAppModuleId(deploymentDescription.getAppModuleId());
            deploymentResource.setModuleResource(module);
            deploymentResource.setHostId(deploymentDescription.getComputeHostId());
            deploymentResource.setHostResource((ComputeHostResource)computeHostResource.get(deploymentDescription.getComputeHostId()));
            deploymentResource.setAppDes(deploymentDescription.getAppDeploymentDescription());
            deploymentResource.setExecutablePath(deploymentDescription.getExecutablePath());
            deploymentResource.setEnvModuleLoadCMD(deploymentDescription.getModuleLoadCmd());
            deploymentResource.save();
            deploymentDescription.setAppDeploymentId(deploymentResource.getDeploymentId());

            List<SetEnvPaths> libPrependPaths = deploymentDescription.getLibPrependPaths();
            if (libPrependPaths != null && !libPrependPaths.isEmpty()){
                for (SetEnvPaths path : libPrependPaths){
                    LibraryPrepandPathResource prepandPathResource = new LibraryPrepandPathResource();
                    prepandPathResource.setAppDeploymentResource(deploymentResource);
                    prepandPathResource.setName(path.getName());
                    prepandPathResource.setValue(path.getValue());
                    prepandPathResource.setDeploymentId(deploymentResource.getDeploymentId());
                    prepandPathResource.save();
                }
            }

            List<SetEnvPaths> libApendPaths = deploymentDescription.getLibAppendPaths();
            if (libApendPaths != null && !libApendPaths.isEmpty()){
                for (SetEnvPaths path : libApendPaths){
                    LibraryApendPathResource apendPathResource = new LibraryApendPathResource();
                    apendPathResource.setAppDeploymentResource(deploymentResource);
                    apendPathResource.setName(path.getName());
                    apendPathResource.setValue(path.getValue());
                    apendPathResource.setDeploymentId(deploymentResource.getDeploymentId());
                    apendPathResource.save();
                }
            }
            List<SetEnvPaths> setEnvironment = deploymentDescription.getSetEnvironment();
            if (setEnvironment != null && !setEnvironment.isEmpty()){
                for (SetEnvPaths path : setEnvironment){
                    AppEnvironmentResource environmentResource = new AppEnvironmentResource();
                    environmentResource.setAppDeploymentResource(deploymentResource);
                    environmentResource.setName(path.getName());
                    environmentResource.setValue(path.getValue());
                    environmentResource.setDeploymentId(deploymentResource.getDeploymentId());
                    environmentResource.save();
                }
            }
            return deploymentResource.getDeploymentId();
        }catch (Exception e) {
            logger.error("Error while saving application deployment...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateApplicationDeployment(String deploymentId, ApplicationDeploymentDescription updatedDeployment) {

    }

    @Override
    public ApplicationDeploymentDescription getApplicationDeployement(String deploymentId) throws AppCatalogException {
        try {
            AppDeploymentResource deploymentResource = new AppDeploymentResource();
            AppDeploymentResource appDep = (AppDeploymentResource)deploymentResource.get(deploymentId);
            return AppCatalogThriftConversion.getApplicationDeploymentDescription(appDep);
        }catch (Exception e) {
            logger.error("Error while retrieving application deployment...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployements(Map<String, String> filters) throws AppCatalogException {
        try {
            AppDeploymentResource resource = new AppDeploymentResource();
            for (String fieldName : filters.keySet() ){
                if (fieldName.equals(AbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID)){
                    List<Resource> resources = resource.get(AbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getAppDepDescList(resources);
                    }
                }else if (fieldName.equals(AbstractResource.ApplicationDeploymentConstants.COMPUTE_HOST_ID)){
                    List<Resource> resources = resource.get(AbstractResource.ApplicationDeploymentConstants.COMPUTE_HOST_ID, filters.get(fieldName));
                    if (resources != null && !resources.isEmpty()){
                        return AppCatalogThriftConversion.getAppDepDescList(resources);
                    }
                } else {
                    logger.error("Unsupported field name for app deployment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported field name for app deployment.");
                }
            }
        }catch (Exception e){
            logger.error("Error while retrieving app deployment list...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public boolean isAppDeploymentExists(String deploymentId) throws AppCatalogException {
        try {
           AppDeploymentResource deploymentResource = new AppDeploymentResource();
            return deploymentResource.isExists(deploymentId);
        }catch (Exception e){
            logger.error("Error while retrieving app deployment...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeAppDeployment(String deploymentId) throws AppCatalogException {
        try {
            AppDeploymentResource deploymentResource = new AppDeploymentResource();
            deploymentResource.remove(deploymentId);
        }catch (Exception e){
            logger.error("Error while deleting app deployment...", e);
            throw new AppCatalogException(e);
        }
    }
}
