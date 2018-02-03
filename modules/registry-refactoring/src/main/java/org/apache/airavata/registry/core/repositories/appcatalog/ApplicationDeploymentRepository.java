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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.appdeployment.*;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ApplicationDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationDeploymentRepository extends AppCatAbstractRepository<ApplicationDeploymentDescription, ApplicationDeploymentEntity, String> implements ApplicationDeployment {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationDeploymentRepository.class);

    public ApplicationDeploymentRepository() {
        super(ApplicationDeploymentDescription.class, ApplicationDeploymentEntity.class);
    }

    @Override
    public String addApplicationDeployment(ApplicationDeploymentDescription deploymentDescription, String gatewayId) throws AppCatalogException {
        ApplicationDeploymentEntity deploymentResource = new ApplicationDeploymentEntity();
        ComputeResourceEntity computeHostResource = new ComputeResourceEntity();
        ApplicationModuleEntity moduleResource = new ApplicationModuleEntity();
        if (!isExists(deploymentDescription.getComputeHostId())){
            logger.error("Compute host does not exist in the system. Please create a Compute host first...");
            throw new AppCatalogException("Compute host does not exist in the system. Please create a Compute host first...");
        }
        if (!isExists(deploymentDescription.getAppModuleId())){
            logger.error("Application module does not exist in the system. Please create an application module first...");
            throw new AppCatalogException("Application module does not exist in the system. Please create an application module first...");
        }
        ComputeResourceEntity hostResource = (ComputeResourceEntity) computeHostResource.get(deploymentDescription.getComputeHostId());
        ComputeResourceEntity computeResourceEntity = get(deploymentDescription.getComputeHostId());
        deploymentResource.setDeploymentId(hostResource.getHostName() + "_" + deploymentDescription.getAppModuleId());
        deploymentResource.setApplicationModuleId(deploymentDescription.getAppModuleId());
        //deploymentResource.setModuleResource(module);
        deploymentResource.setComputeHostId(deploymentDescription.getComputeHostId());
        //deploymentResource.setHostResource(hostResource);
        deploymentResource.setApplicationDesc(deploymentDescription.getAppDeploymentDescription());
        deploymentResource.setExecutablePath(deploymentDescription.getExecutablePath());
        deploymentResource.setGatewayId(gatewayId);
        deploymentResource.setDefaultQueueName(deploymentDescription.getDefaultQueueName());
        deploymentResource.setDefaultCPUCount(deploymentDescription.getDefaultCPUCount());
        deploymentResource.setDefaultNodeCount(deploymentDescription.getDefaultNodeCount());
        deploymentResource.setDefaultWallTime(deploymentDescription.getDefaultWalltime());
        deploymentResource.setEditableByUser(deploymentDescription.isEditableByUser());
        ApplicationParallelismType parallelism = deploymentDescription.getParallelism();
        if (parallelism != null){
            deploymentResource.setParallelism(parallelism.toString());
        }
        //deploymentResource.save();
        execute(entityManager -> entityManager.merge(deploymentResource));
        deploymentDescription.setAppDeploymentId(deploymentResource.getDeploymentId());

        List<CommandObject> moduleLoadCmds = deploymentDescription.getModuleLoadCmds();
        if (moduleLoadCmds != null && !moduleLoadCmds.isEmpty()){
            for (CommandObject cmd : moduleLoadCmds){
                ModuleLoadCmdEntity cmdResource = new ModuleLoadCmdEntity();
                cmdResource.setAppdeploymentId(deploymentDescription.getAppDeploymentId());
                cmdResource.setCommandOrder(cmd.getCommandOrder());
                cmdResource.setCommand(cmd.getCommand());
                //cmdResource.save();
                execute(entityManager -> entityManager.merge(cmdResource));
            }
        }

        List<CommandObject> preJobCommands = deploymentDescription.getPreJobCommands();
        if (preJobCommands != null && !preJobCommands.isEmpty()){
            for (CommandObject cmd : preJobCommands){
                PrejobCommandEntity cmdResource = new PrejobCommandEntity();
                cmdResource.setAppdeploymentId(deploymentDescription.getAppDeploymentId());
                cmdResource.setCommand(cmd.getCommand());
                cmdResource.setCommandOrder(cmd.getCommandOrder());
                //cmdResource.save();
                execute(entityManager -> entityManager.merge(cmdResource));
            }
        }

        List<CommandObject> postJobCommands = deploymentDescription.getPostJobCommands();
        if (postJobCommands != null && !postJobCommands.isEmpty()){
            for (CommandObject cmd : postJobCommands){
                PostjobCommandEntity cmdResource = new PostjobCommandEntity();
                cmdResource.setAppdeploymentId(deploymentDescription.getAppDeploymentId());
                cmdResource.setCommand(cmd.getCommand());
                cmdResource.setCommandOrder(cmd.getCommandOrder());
                //cmdResource.save();
                execute(entityManager -> entityManager.merge(cmdResource));
            }
        }

        List<SetEnvPaths> libPrependPaths = deploymentDescription.getLibPrependPaths();
        if (libPrependPaths != null && !libPrependPaths.isEmpty()){
            for (SetEnvPaths path : libPrependPaths){
                LibraryPrependPathEntity prepandPathResource = new LibraryPrependPathEntity();
                prepandPathResource.setApplicationDeployment(deploymentResource);
                prepandPathResource.setName(path.getName());
                prepandPathResource.setValue(path.getValue());
                prepandPathResource.setDeploymentId(deploymentResource.getDeploymentId());
                //prepandPathResource.save();
                execute(entityManager -> entityManager.merge(prepandPathResource));
            }
        }

        List<SetEnvPaths> libApendPaths = deploymentDescription.getLibAppendPaths();
        if (libApendPaths != null && !libApendPaths.isEmpty()){
            for (SetEnvPaths path : libApendPaths){
                LibraryApendPathEntity apendPathResource = new LibraryApendPathEntity();
                apendPathResource.setApplicationDeployment(deploymentResource);
                apendPathResource.setName(path.getName());
                apendPathResource.setValue(path.getValue());
                apendPathResource.setDeploymentId(deploymentResource.getDeploymentId());
                //apendPathResource.save();
                execute(entityManager -> entityManager.merge(apendPathResource));
            }
        }
        List<SetEnvPaths> setEnvironment = deploymentDescription.getSetEnvironment();
        if (setEnvironment != null && !setEnvironment.isEmpty()){
            for (SetEnvPaths path : setEnvironment){
                AppEnvironmentEntity environmentResource = new AppEnvironmentEntity();
                environmentResource.setApplicationDeployment(deploymentResource);
                environmentResource.setName(path.getName());
                environmentResource.setValue(path.getValue());
                environmentResource.setDeploymentId(deploymentResource.getDeploymentId());
                environmentResource.setEnvOrder(path.getEnvPathOrder());
                //environmentResource.save();
                execute(entityManager -> entityManager.merge(environmentResource));
            }
        }
        return deploymentResource.getDeploymentId();
    }

    @Override
    public void updateApplicationDeployment(String deploymentId, ApplicationDeploymentDescription updatedDeployment) throws AppCatalogException {
        ApplicationDeploymentEntity deploymentResource = new ApplicationDeploymentEntity();
        ApplicationDeploymentEntity existingDep = (ApplicationDeploymentEntity)deploymentResource.get(deploymentId);
        ComputeResourceEntity computeHostResource = new ComputeResourceEntity();
        ApplicationModuleEntity moduleResource = new ApplicationModuleEntity();
        if (!isExists(updatedDeployment.getComputeHostId())){
            logger.error("Compute host does not exist in the system. Please create a Compute host first...");
            throw new AppCatalogException("Compute host does not exist in the system. Please create a Compute host first...");
        }
        if (!isExists(updatedDeployment.getAppModuleId())){
            logger.error("Application module does not exist in the system. Please create an application module first...");
            throw new AppCatalogException("Application module does not exist in the system. Please create an application module first...");
        }
        ApplicationModuleEntity module = (ApplicationModuleEntity)moduleResource.get(updatedDeployment.getAppModuleId());
        existingDep.setApplicationModuleId(updatedDeployment.getAppModuleId());
        //existingDep.setModuleResource(module);
        existingDep.setComputeHostId(updatedDeployment.getComputeHostId());
        //existingDep.setHostResource((ComputeResourceEntity)computeHostResource.get(updatedDeployment.getComputeHostId()));
        existingDep.setApplicationDesc(updatedDeployment.getAppDeploymentDescription());
        existingDep.setExecutablePath(updatedDeployment.getExecutablePath());
        existingDep.setDefaultQueueName(updatedDeployment.getDefaultQueueName());
        existingDep.setDefaultCPUCount(updatedDeployment.getDefaultCPUCount());
        existingDep.setDefaultNodeCount(updatedDeployment.getDefaultNodeCount());
        existingDep.setDefaultWallTime(updatedDeployment.getDefaultWalltime());
        existingDep.setEditableByUser(updatedDeployment.isEditableByUser());
        if (updatedDeployment.getParallelism() != null){
            existingDep.setParallelism(updatedDeployment.getParallelism().toString());
        }

        //existingDep.save();
        execute(entityManager -> entityManager.merge(existingDep));
        execute(entityManager -> entityManager.merge(existingDep));

        // remove existing module load commands
        ModuleLoadCmdEntity cmdResource = new ModuleLoadCmdEntity();
        Map<String, String> ids = new HashMap<String, String>();
        ids.put(AppCatAbstractResource.ModuleLoadCmdConstants.APP_DEPLOYMENT_ID, deploymentId);
        cmdResource.remove(ids);
        List<CommandObject> moduleLoadCmds = updatedDeployment.getModuleLoadCmds();
        if (moduleLoadCmds != null && !moduleLoadCmds.isEmpty()){
            for (CommandObject cmd : moduleLoadCmds){
                ids = new HashMap<String, String>();
                ids.put(AppCatAbstractResource.ModuleLoadCmdConstants.APP_DEPLOYMENT_ID, deploymentId);
                ids.put(AppCatAbstractResource.ModuleLoadCmdConstants.CMD, cmd.getCommand());
                if (cmdResource.isExists(ids)){
                    cmdResource = (ModuleLoadCmdEntity)cmdResource.get(ids);
                }
                ModuleLoadCmdPK moduleLoadCmdPK = new ModuleLoadCmdPK();
                moduleLoadCmdPK.setAppdeploymentId(deploymentId);
                moduleLoadCmdPK.setCommand(cmd.getCommand());
                cmdResource.setAppdeploymentId(deploymentId);
                cmdResource.setCommand(cmd.getCommand());
                cmdResource.setApplicationDeployment(existingDep);
                cmdResource.setCommandOrder(cmd.getCommandOrder());
                //cmdResource.save();
                execute(entityManager -> entityManager.merge(cmdResource));
            }
        }

        PrejobCommandEntity preJobCommandResource = new PrejobCommandEntity();
        ids = new HashMap<String, String>();
        ids.put(AppCatAbstractResource.PreJobCommandConstants.DEPLOYMENT_ID, deploymentId);
        preJobCommandResource.remove(ids);
        List<CommandObject> preJobCommands = updatedDeployment.getPreJobCommands();
        if (preJobCommands != null && !preJobCommands.isEmpty()){
            for (CommandObject cmd : preJobCommands){
                ids = new HashMap<String, String>();
                ids.put(AppCatAbstractResource.PreJobCommandConstants.DEPLOYMENT_ID, deploymentId);
                ids.put(AppCatAbstractResource.PreJobCommandConstants.COMMAND, cmd.getCommand());
                if (preJobCommandResource.isExists(ids)){
                    preJobCommandResource = (PrejobCommandEntity)preJobCommandResource.get(ids);
                }
                preJobCommandResource.setAppdeploymentId(deploymentId);
                preJobCommandResource.setCommand(cmd.getCommand());
                preJobCommandResource.setApplicationDeployment(existingDep);
                preJobCommandResource.setCommandOrder(cmd.getCommandOrder());
                //preJobCommandResource.save();
                execute(entityManager -> entityManager.merge(preJobCommandResource));
            }
        }

        PostjobCommandEntity postJobCommandResource = new PostjobCommandEntity();
        ids = new HashMap<String, String>();
        ids.put(AppCatAbstractResource.PostJobCommandConstants.DEPLOYMENT_ID, deploymentId);
        postJobCommandResource.remove(ids);
        List<CommandObject> postJobCommands = updatedDeployment.getPostJobCommands();
        if (postJobCommands != null && !postJobCommands.isEmpty()){
            for (CommandObject cmd : postJobCommands){
                ids = new HashMap<String, String>();
                ids.put(AppCatAbstractResource.PostJobCommandConstants.DEPLOYMENT_ID, deploymentId);
                ids.put(AppCatAbstractResource.PostJobCommandConstants.COMMAND, cmd.getCommand());
                if (postJobCommandResource.isExists(ids)){
                    postJobCommandResource = (PostjobCommandEntity) postJobCommandResource.get(ids);
                }
                postJobCommandResource.setAppdeploymentId(deploymentId);
                postJobCommandResource.setCommand(cmd.getCommand());
                postJobCommandResource.setApplicationDeployment(existingDep);
                postJobCommandResource.setCommandOrder(cmd.getCommandOrder());
                //postJobCommandResource.save();
                execute(entityManager -> entityManager.merge(postJobCommandResource));
            }
        }

        // remove existing lib prepand paths
        LibraryPrependPathEntity prepandPathResource = new LibraryPrependPathEntity();
        ids = new HashMap<String, String>();
        ids.put(AppCatAbstractResource.LibraryPrepandPathConstants.DEPLOYMENT_ID, deploymentId);
        prepandPathResource.remove(ids);
        List<SetEnvPaths> libPrependPaths = updatedDeployment.getLibPrependPaths();
        if (libPrependPaths != null && !libPrependPaths.isEmpty()){
            for (SetEnvPaths path : libPrependPaths){
                ids = new HashMap<String, String>();
                ids.put(AppCatAbstractResource.LibraryPrepandPathConstants.DEPLOYMENT_ID, deploymentId);
                ids.put(AppCatAbstractResource.LibraryPrepandPathConstants.NAME, path.getName());
                if (prepandPathResource.isExists(ids)){
                    prepandPathResource = (LibraryPrependPathEntity) prepandPathResource.get(ids);
                }
                prepandPathResource.setApplicationDeployment(existingDep);
                prepandPathResource.setName(path.getName());
                prepandPathResource.setValue(path.getValue());
                prepandPathResource.setDeploymentId(deploymentId);
                //prepandPathResource.save();
                execute(entityManager -> entityManager.merge(prepandPathResource));
            }
        }

        List<SetEnvPaths> libApendPaths = updatedDeployment.getLibAppendPaths();
        // remove lib append paths
        LibraryApendPathEntity apendPathResource = new LibraryApendPathEntity();
        ids = new HashMap<String, String>();
        ids.put(AppCatAbstractResource.LibraryApendPathConstants.DEPLOYMENT_ID, deploymentId);
        apendPathResource.remove(ids);
        if (libApendPaths != null && !libApendPaths.isEmpty()){
            for (SetEnvPaths path : libApendPaths){
                ids = new HashMap<String, String>();
                ids.put(AppCatAbstractResource.LibraryApendPathConstants.DEPLOYMENT_ID, deploymentId);
                ids.put(AppCatAbstractResource.LibraryApendPathConstants.NAME, path.getName());
                if (apendPathResource.isExists(ids)){
                    apendPathResource = (LibraryApendPathEntity) apendPathResource.get(ids);
                }
                apendPathResource.setApplicationDeployment(existingDep);
                apendPathResource.setName(path.getName());
                apendPathResource.setValue(path.getValue());
                apendPathResource.setDeploymentId(deploymentId);
                //apendPathResource.save();
                execute(entityManager -> entityManager.merge(apendPathResource));
            }
        }

        List<SetEnvPaths> setEnvironment = updatedDeployment.getSetEnvironment();
        // remove existing setEnvPaths
        AppEnvironmentEntity environmentResource = new AppEnvironmentEntity();
        ids = new HashMap<String, String>();
        ids.put(AppCatAbstractResource.AppEnvironmentConstants.DEPLOYMENT_ID, deploymentId);
        environmentResource.remove(ids);
        if (setEnvironment != null && !setEnvironment.isEmpty()){
            for (SetEnvPaths path : setEnvironment){
                ids = new HashMap<String, String>();
                ids.put(AppCatAbstractResource.AppEnvironmentConstants.DEPLOYMENT_ID, deploymentId);
                ids.put(AppCatAbstractResource.AppEnvironmentConstants.NAME, path.getName());
                if (environmentResource.isExists(ids)){
                    environmentResource = (AppEnvironmentEntity) environmentResource.get(ids);
                }
                environmentResource.setApplicationDeployment(existingDep);
                environmentResource.setName(path.getName());
                environmentResource.setValue(path.getValue());
                environmentResource.setDeploymentId(deploymentId);
                environmentResource.setEnvOrder(path.getEnvPathOrder());
                //environmentResource.save();
                execute(entityManager -> entityManager.merge(environmentResource));
            }
        }
    }

    @Override
    public ApplicationDeploymentDescription getApplicationDeployement(String deploymentId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.APPLICATION_DEPLOYMENT_ID, deploymentId);
        ApplicationDeploymentDescription applicationDeploymentDescription = (ApplicationDeploymentDescription) select(QueryConstants.FIND_APPLICATION_DEPLOYMENT, -1, 0, queryParameters);
        return applicationDeploymentDescription;
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployements(Map<String, String> filters) throws AppCatalogException {
        if(filters.containsKey(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, filters.get(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID));
            List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = select(QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_APPLICATION_MODULE_ID, -1, 0, queryParameters);
            return applicationDeploymentDescriptionList;
        } else if(filters.containsKey(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID)) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, filters.get(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID));
            List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = select(QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_COMPUTE_HOST_ID, -1, 0, queryParameters);
            return applicationDeploymentDescriptionList;
        } else {
            logger.error("Unsupported field name for app deployment.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for app deployment.");
        }
    }

    @Override
    public List<ApplicationDeploymentDescription> getAllApplicationDeployements(String gatewayId) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ApplicationDeployment.GATEWAY_ID, gatewayId);
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = select(QueryConstants.FIND_APPLICATION_DEPLOYMENTS_FOR_GATEWAY_ID, -1, 0, queryParameters);
        return applicationDeploymentDescriptionList;
    }

    @Override
    public List<String> getAllApplicationDeployementIds() throws AppCatalogException {
        List<String> applicationDeploymentIds = new ArrayList<>();
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = select(QueryConstants.GET_ALL_APPLICATION_DEPLOYMENTS, 0);
        if (applicationDeploymentDescriptionList != null && !applicationDeploymentDescriptionList.isEmpty()) {
            for (ApplicationDeploymentDescription applicationDeploymentDescription: applicationDeploymentDescriptionList) {
                applicationDeploymentIds.add(applicationDeploymentDescription.getAppDeploymentId());
            }
        }
        return applicationDeploymentIds;
    }

    @Override
    public boolean isAppDeploymentExists(String deploymentId) throws AppCatalogException {
        return isExists(deploymentId);
    }

    @Override
    public void removeAppDeployment(String deploymentId) throws AppCatalogException {
        delete(deploymentId);
    }

}
