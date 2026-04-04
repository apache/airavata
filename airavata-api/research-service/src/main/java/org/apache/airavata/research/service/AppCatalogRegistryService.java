/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.service;

import java.util.*;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.AppCatalogDataAccess;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.research.repository.ExperimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class AppCatalogRegistryService implements AppCatalogRegistry {
    private static final Logger logger = LoggerFactory.getLogger(AppCatalogRegistryService.class);

    private final AppCatalogDataAccess appCatalogDataAccess;
    private final ExperimentRepository experimentRepository = new ExperimentRepository();
    private final GatewayRepository gatewayRepository = new GatewayRepository();

    public AppCatalogRegistryService(AppCatalogDataAccess appCatalogDataAccess) {
        this.appCatalogDataAccess = appCatalogDataAccess;
    }

    // =========================================================================
    // AppCatalogRegistry interface methods
    // =========================================================================

    @Override
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws Exception {
        try {
            ApplicationDeploymentDescription deployement =
                    appCatalogDataAccess.getApplicationDeployment(appDeploymentId);
            logger.debug("Airavata registered application deployment for deployment id : " + appDeploymentId);
            return deployement;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while retrieving application deployment...", e);
            throw new RegistryException("Error while retrieving application deployment. More info : " + e.getMessage());
        }
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws Exception {
        try {
            ApplicationInterfaceDescription interfaceDescription =
                    appCatalogDataAccess.getApplicationInterface(appInterfaceId);
            logger.debug("Airavata retrieved application interface with interface id : " + appInterfaceId);
            return interfaceDescription;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application interface...", e);
            throw new RegistryException("Error while retrieving application interface. More info : " + e.getMessage());
        }
    }

    @Override
    public Parser getParser(String parserId, String gatewayId) throws Exception {
        try {
            if (!appCatalogDataAccess.isParserExists(parserId)) {
                final String message = "No Parser Info entry exists for " + parserId;
                logger.error(message);
                throw new RegistryException(message);
            }
            return appCatalogDataAccess.getParser(parserId);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            final String message = "Error while retrieving parser with id " + parserId + ".";
            logger.error(message, e);
            throw new RegistryException(message + " More info: " + e.getMessage());
        }
    }

    @Override
    public ParserInput getParserInput(String parserInputId, String gatewayId) throws Exception {
        try {
            ParserInput parserInput = appCatalogDataAccess.getParserInput(parserInputId);
            return parserInput;
        } catch (Exception e) {
            logger.error("Failed to fetch parser input " + parserInputId + " for gateway " + gatewayId, e);
            throw new RegistryException("Failed to fetch parser input " + parserInputId + " for gateway " + gatewayId
                    + " More info: " + e.getMessage());
        }
    }

    @Override
    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws Exception {
        try {
            ExperimentModel experiment = experimentRepository.getExperiment(experimentId);
            List<ProcessModel> processes = experiment.getProcessesList();
            if (processes.size() > 0) {
                return appCatalogDataAccess.getParsingTemplatesForApplication(
                        processes.get(processes.size() - 1).getApplicationInterfaceId());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            final String message = "Error while retrieving parsing templates for experiment id " + experimentId;
            logger.error(message, e);
            throw new RegistryException(message + " More info: " + e.getMessage());
        }
    }

    // =========================================================================
    // Additional app catalog methods (not yet on the interface)
    // =========================================================================

    public ApplicationModule getApplicationModule(String appModuleId) throws Exception {
        try {
            return appCatalogDataAccess.getApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application module...", e);
            throw new RegistryException(
                    "Error while retrieving the adding application module. More info : " + e.getMessage());
        }
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.getAllApplicationModules(gatewayId);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while retrieving all application modules. More info : " + e.getMessage());
        }
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.getAccessibleApplicationModules(
                    gatewayId, accessibleAppIds, accessibleComputeResourceIds);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while retrieving all application modules. More info : " + e.getMessage());
        }
    }

    public boolean deleteApplicationModule(String appModuleId) throws Exception {
        try {
            return appCatalogDataAccess.removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting the application module. More info : " + e.getMessage());
        }
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws Exception {
        try {
            appCatalogDataAccess.removeApplicationDeployment(appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting application deployment. More info : " + e.getMessage());
        }
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId) throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.getAllApplicationDeployments(gatewayId);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while retrieving application deployments. More info : " + e.getMessage());
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.getAccessibleApplicationDeployments(
                    gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while retrieving application deployments. More info : " + e.getMessage());
        }
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.getAccessibleApplicationDeployments(
                    gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while retrieving application deployments. More info : " + e.getMessage());
        }
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws Exception {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            List<ApplicationDeploymentDescription> applicationDeployments =
                    appCatalogDataAccess.getApplicationDeployments(filters);
            List<String> appDeployments = new ArrayList<>();
            for (ApplicationDeploymentDescription d : applicationDeployments) {
                appDeployments.add(d.getAppDeploymentId());
            }
            return appDeployments;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application deployment. More info : " + e.getMessage());
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws Exception {
        try {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, appModuleId);
            return appCatalogDataAccess.getApplicationDeployments(filters);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application deployment. More info : " + e.getMessage());
        }
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws Exception {
        try {
            return appCatalogDataAccess.removeApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting application interface. More info : " + e.getMessage());
        }
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            List<ApplicationInterfaceDescription> allApplicationInterfaces =
                    appCatalogDataAccess.getAllApplicationInterfaces(gatewayId);
            Map<String, String> allApplicationInterfacesMap = new HashMap<>();
            if (allApplicationInterfaces != null) {
                for (ApplicationInterfaceDescription i : allApplicationInterfaces) {
                    allApplicationInterfacesMap.put(i.getApplicationInterfaceId(), i.getApplicationName());
                }
            }
            return allApplicationInterfacesMap;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application interfaces. More info : " + e.getMessage());
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.getAllApplicationInterfaces(gatewayId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application interfaces. More info : " + e.getMessage());
        }
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws Exception {
        try {
            return appCatalogDataAccess.getApplicationInputs(appInterfaceId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application inputs. More info : " + e.getMessage());
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws Exception {
        try {
            return appCatalogDataAccess.getApplicationOutputs(appInterfaceId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application outputs. More info : " + e.getMessage());
        }
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId) throws Exception {
        try {
            Map<String, String> allComputeResources = appCatalogDataAccess.getAvailableComputeResourceIdList();
            Map<String, String> availableComputeResources = new HashMap<>();
            ApplicationInterfaceDescription applicationInterface =
                    appCatalogDataAccess.getApplicationInterface(appInterfaceId);
            HashMap<String, String> filters = new HashMap<>();
            for (String moduleId : applicationInterface.getApplicationModulesList()) {
                filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, moduleId);
                for (ApplicationDeploymentDescription d : appCatalogDataAccess.getApplicationDeployments(filters)) {
                    if (allComputeResources.get(d.getComputeHostId()) != null) {
                        availableComputeResources.put(
                                d.getComputeHostId(), allComputeResources.get(d.getComputeHostId()));
                    }
                }
            }
            return availableComputeResources;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while saving compute resource. More info : " + e.getMessage());
        }
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws Exception {
        try {
            appCatalogDataAccess.updateApplicationInterface(appInterfaceId, applicationInterface);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while updating application interface. More info : " + e.getMessage());
        }
    }

    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.addApplicationInterface(applicationInterface, gatewayId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding application interface. More info : " + e.getMessage());
        }
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws Exception {
        try {
            appCatalogDataAccess.updateApplicationDeployment(appDeploymentId, applicationDeployment);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while updating application deployment. More info : " + e.getMessage());
        }
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.addApplicationDeployment(applicationDeployment, gatewayId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding application deployment. More info : " + e.getMessage());
        }
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule) throws Exception {
        try {
            appCatalogDataAccess.updateApplicationModule(appModuleId, applicationModule);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while updating application module. More info : " + e.getMessage());
        }
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule) throws Exception {
        if (!isGatewayExistInternal(gatewayId)) {
            throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
        }
        try {
            return appCatalogDataAccess.addApplicationModule(applicationModule, gatewayId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding application module. More info : " + e.getMessage());
        }
    }

    // --- Parser methods ---

    public String saveParser(Parser parser) throws Exception {
        try {
            return appCatalogDataAccess.saveParser(parser).getId();
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while saving parser with id " + parser.getId() + " More info: " + e.getMessage());
        }
    }

    public List<Parser> listAllParsers(String gatewayId) throws Exception {
        try {
            return appCatalogDataAccess.getAllParsers(gatewayId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while listing parsers for gateway " + gatewayId + " More info: " + e.getMessage());
        }
    }

    public void removeParser(String parserId, String gatewayId) throws Exception {
        try {
            boolean exists = appCatalogDataAccess.isParserExists(parserId);
            if (exists
                    && !gatewayId.equals(
                            appCatalogDataAccess.getParser(parserId).getGatewayId())) {
                appCatalogDataAccess.deleteParser(parserId);
            } else {
                throw new RegistryException("Parser " + parserId + " does not exist");
            }
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while removing parser with id " + parserId + ". More info: " + e.getMessage());
        }
    }

    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws Exception {
        try {
            return appCatalogDataAccess.getParserOutput(parserOutputId);
        } catch (Exception e) {
            throw new RegistryException("Failed to fetch parser output " + parserOutputId + " for gateway " + gatewayId
                    + " More info: " + e.getMessage());
        }
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws Exception {
        try {
            if (!appCatalogDataAccess.isParsingTemplateExists(templateId)) {
                throw new RegistryException("No Parsing Template entry exists for " + templateId);
            }
            return appCatalogDataAccess.getParsingTemplate(templateId);
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while retrieving Parsing Template for id " + templateId + ". More info: " + e.getMessage());
        }
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws Exception {
        try {
            return appCatalogDataAccess.createParsingTemplate(parsingTemplate).getId();
        } catch (Exception e) {
            throw new RegistryException("Error while saving parsing template with id " + parsingTemplate.getId()
                    + " More info: " + e.getMessage());
        }
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws Exception {
        try {
            return appCatalogDataAccess.getAllParsingTemplates(gatewayId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while listing parsing templates for gateway " + gatewayId + " More info: " + e.getMessage());
        }
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws Exception {
        try {
            boolean exists = appCatalogDataAccess.isParsingTemplateExists(templateId);
            if (exists
                    && !gatewayId.equals(
                            appCatalogDataAccess.getParsingTemplate(templateId).getGatewayId())) {
                appCatalogDataAccess.deleteParsingTemplate(templateId);
            } else {
                throw new RegistryException("Parsing tempolate " + templateId + " does not exist");
            }
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while removing parsing template with id " + templateId + " More info: " + e.getMessage());
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private boolean isGatewayExistInternal(String gatewayId) throws Exception {
        try {
            return gatewayRepository.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            throw new RegistryException("Error while getting gateway. More info : " + e.getMessage());
        }
    }
}
