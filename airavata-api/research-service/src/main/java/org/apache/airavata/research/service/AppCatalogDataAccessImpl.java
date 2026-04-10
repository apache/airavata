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

import java.util.List;
import java.util.Map;
import org.apache.airavata.interfaces.AppCatalogDataAccess;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.ComputeResource;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.research.repository.ApplicationDeploymentRepository;
import org.apache.airavata.research.repository.ApplicationInterfaceRepository;
import org.apache.airavata.research.repository.ParserInputRepository;
import org.apache.airavata.research.repository.ParserOutputRepository;
import org.apache.airavata.research.repository.ParserRepository;
import org.apache.airavata.research.repository.ParsingTemplateRepository;
import org.springframework.stereotype.Component;

@Component
public class AppCatalogDataAccessImpl implements AppCatalogDataAccess {

    private final ComputeResource computeResource;
    private final ApplicationDeploymentRepository applicationDeploymentRepository;
    private final ApplicationInterfaceRepository applicationInterfaceRepository = new ApplicationInterfaceRepository();
    private final ParserRepository parserRepository = new ParserRepository();
    private final ParserInputRepository parserInputRepository = new ParserInputRepository();
    private final ParserOutputRepository parserOutputRepository = new ParserOutputRepository();
    private final ParsingTemplateRepository parsingTemplateRepository = new ParsingTemplateRepository();

    public AppCatalogDataAccessImpl(ComputeResource computeResource) {
        this.computeResource = computeResource;
        this.applicationDeploymentRepository = new ApplicationDeploymentRepository(computeResource);
    }
    // --- Application Deployment ---

    @Override
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws AppCatalogException {
        return applicationDeploymentRepository.getApplicationDeployement(appDeploymentId);
    }

    @Override
    public void removeApplicationDeployment(String appDeploymentId) throws AppCatalogException {
        applicationDeploymentRepository.removeAppDeployment(appDeploymentId);
    }

    @Override
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId)
            throws AppCatalogException {
        return applicationDeploymentRepository.getAllApplicationDeployements(gatewayId);
    }

    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        return applicationDeploymentRepository.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        return applicationDeploymentRepository.getAccessibleApplicationDeployments(
                gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(Map<String, String> filters)
            throws AppCatalogException {
        return applicationDeploymentRepository.getApplicationDeployments(filters);
    }

    @Override
    public String addApplicationDeployment(ApplicationDeploymentDescription deployment, String gatewayId)
            throws AppCatalogException {
        return applicationDeploymentRepository.addApplicationDeployment(deployment, gatewayId);
    }

    @Override
    public void updateApplicationDeployment(String appDeploymentId, ApplicationDeploymentDescription deployment)
            throws AppCatalogException {
        applicationDeploymentRepository.updateApplicationDeployment(appDeploymentId, deployment);
    }

    // --- Application Interface ---

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AppCatalogException {
        return applicationInterfaceRepository.getApplicationInterface(appInterfaceId);
    }

    @Override
    public boolean removeApplicationInterface(String appInterfaceId) throws AppCatalogException {
        return applicationInterfaceRepository.removeApplicationInterface(appInterfaceId);
    }

    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AppCatalogException {
        return applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId);
    }

    @Override
    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws AppCatalogException {
        return applicationInterfaceRepository.getApplicationInputs(appInterfaceId);
    }

    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws AppCatalogException {
        return applicationInterfaceRepository.getApplicationOutputs(appInterfaceId);
    }

    @Override
    public String addApplicationInterface(ApplicationInterfaceDescription iface, String gatewayId)
            throws AppCatalogException {
        return applicationInterfaceRepository.addApplicationInterface(iface, gatewayId);
    }

    @Override
    public void updateApplicationInterface(String appInterfaceId, ApplicationInterfaceDescription iface)
            throws AppCatalogException {
        applicationInterfaceRepository.updateApplicationInterface(appInterfaceId, iface);
    }

    // --- Application Module ---

    @Override
    public ApplicationModule getApplicationModule(String appModuleId) throws AppCatalogException {
        return applicationInterfaceRepository.getApplicationModule(appModuleId);
    }

    @Override
    public List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException {
        return applicationInterfaceRepository.getAllApplicationModules(gatewayId);
    }

    @Override
    public List<ApplicationModule> getAccessibleApplicationModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException {
        return applicationInterfaceRepository.getAccessibleApplicationModules(
                gatewayId, accessibleAppIds, accessibleComputeResourceIds);
    }

    @Override
    public boolean removeApplicationModule(String appModuleId) throws AppCatalogException {
        return applicationInterfaceRepository.removeApplicationModule(appModuleId);
    }

    @Override
    public String addApplicationModule(ApplicationModule module, String gatewayId) throws AppCatalogException {
        return applicationInterfaceRepository.addApplicationModule(module, gatewayId);
    }

    @Override
    public void updateApplicationModule(String appModuleId, ApplicationModule module) throws AppCatalogException {
        applicationInterfaceRepository.updateApplicationModule(appModuleId, module);
    }

    // --- Parser ---

    @Override
    public boolean isParserExists(String parserId) throws Exception {
        return parserRepository.isExists(parserId);
    }

    @Override
    public Parser getParser(String parserId) throws Exception {
        return parserRepository.get(parserId);
    }

    @Override
    public Parser saveParser(Parser parser) throws Exception {
        return parserRepository.saveParser(parser);
    }

    @Override
    public List<Parser> getAllParsers(String gatewayId) throws Exception {
        return parserRepository.getAllParsers(gatewayId);
    }

    @Override
    public void deleteParser(String parserId) throws Exception {
        parserRepository.delete(parserId);
    }

    // --- Parser Input / Output ---

    @Override
    public ParserInput getParserInput(String parserInputId) throws Exception {
        return parserInputRepository.getParserInput(parserInputId);
    }

    @Override
    public ParserOutput getParserOutput(String parserOutputId) throws Exception {
        return parserOutputRepository.getParserOutput(parserOutputId);
    }

    // --- Parsing Template ---

    @Override
    public boolean isParsingTemplateExists(String templateId) throws Exception {
        return parsingTemplateRepository.isExists(templateId);
    }

    @Override
    public ParsingTemplate getParsingTemplate(String templateId) throws Exception {
        return parsingTemplateRepository.get(templateId);
    }

    @Override
    public ParsingTemplate createParsingTemplate(ParsingTemplate template) throws Exception {
        return parsingTemplateRepository.create(template);
    }

    @Override
    public List<ParsingTemplate> getParsingTemplatesForApplication(String appInterfaceId) throws Exception {
        return parsingTemplateRepository.getParsingTemplatesForApplication(appInterfaceId);
    }

    @Override
    public List<ParsingTemplate> getAllParsingTemplates(String gatewayId) throws Exception {
        return parsingTemplateRepository.getAllParsingTemplates(gatewayId);
    }

    @Override
    public void deleteParsingTemplate(String templateId) throws Exception {
        parsingTemplateRepository.delete(templateId);
    }

    // --- Compute Resource ---

    @Override
    public Map<String, String> getAvailableComputeResourceIdList() throws AppCatalogException {
        return computeResource.getAvailableComputeResourceIdList();
    }
}
