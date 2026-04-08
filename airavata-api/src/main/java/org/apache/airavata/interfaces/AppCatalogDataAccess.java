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
package org.apache.airavata.interfaces;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;

/**
 * Abstraction over compute-service repositories for use by research-service.
 * All methods use proto model types (not JPA entities) so research-service
 * does not need to depend on compute-service's concrete repository classes.
 */
public interface AppCatalogDataAccess {

    // --- Application Deployment ---
    ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws AppCatalogException;

    void removeApplicationDeployment(String appDeploymentId) throws AppCatalogException;

    List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId) throws AppCatalogException;

    List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException;

    List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws AppCatalogException;

    List<ApplicationDeploymentDescription> getApplicationDeployments(Map<String, String> filters)
            throws AppCatalogException;

    String addApplicationDeployment(ApplicationDeploymentDescription deployment, String gatewayId)
            throws AppCatalogException;

    void updateApplicationDeployment(String appDeploymentId, ApplicationDeploymentDescription deployment)
            throws AppCatalogException;

    // --- Application Interface ---
    ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AppCatalogException;

    boolean removeApplicationInterface(String appInterfaceId) throws AppCatalogException;

    List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws AppCatalogException;

    List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws AppCatalogException;

    List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws AppCatalogException;

    String addApplicationInterface(ApplicationInterfaceDescription iface, String gatewayId) throws AppCatalogException;

    void updateApplicationInterface(String appInterfaceId, ApplicationInterfaceDescription iface)
            throws AppCatalogException;

    // --- Application Module ---
    ApplicationModule getApplicationModule(String appModuleId) throws AppCatalogException;

    List<ApplicationModule> getAllApplicationModules(String gatewayId) throws AppCatalogException;

    List<ApplicationModule> getAccessibleApplicationModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws AppCatalogException;

    boolean removeApplicationModule(String appModuleId) throws AppCatalogException;

    String addApplicationModule(ApplicationModule module, String gatewayId) throws AppCatalogException;

    void updateApplicationModule(String appModuleId, ApplicationModule module) throws AppCatalogException;

    // --- Parser ---
    boolean isParserExists(String parserId) throws Exception;

    Parser getParser(String parserId) throws Exception;

    Parser saveParser(Parser parser) throws Exception;

    List<Parser> getAllParsers(String gatewayId) throws Exception;

    void deleteParser(String parserId) throws Exception;

    // --- Parser Input / Output ---
    ParserInput getParserInput(String parserInputId) throws Exception;

    ParserOutput getParserOutput(String parserOutputId) throws Exception;

    // --- Parsing Template ---
    boolean isParsingTemplateExists(String templateId) throws Exception;

    ParsingTemplate getParsingTemplate(String templateId) throws Exception;

    ParsingTemplate createParsingTemplate(ParsingTemplate template) throws Exception;

    List<ParsingTemplate> getParsingTemplatesForApplication(String appInterfaceId) throws Exception;

    List<ParsingTemplate> getAllParsingTemplates(String gatewayId) throws Exception;

    void deleteParsingTemplate(String templateId) throws Exception;

    // --- Compute Resource ---
    Map<String, String> getAvailableComputeResourceIdList() throws AppCatalogException;
}
