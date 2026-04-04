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
 * Registry operations for application modules, deployments, interfaces, and parsers.
 */
public interface AppCatalogRegistry {

    // --- Application deployment operations ---
    ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws Exception;

    boolean deleteApplicationDeployment(String appDeploymentId) throws Exception;

    List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId) throws Exception;

    List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws Exception;

    List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws Exception;

    List<String> getAppModuleDeployedResources(String appModuleId) throws Exception;

    List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws Exception;

    boolean updateApplicationDeployment(String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws Exception;

    String registerApplicationDeployment(String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws Exception;

    // --- Application interface operations ---
    ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws Exception;

    boolean deleteApplicationInterface(String appInterfaceId) throws Exception;

    Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws Exception;

    List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws Exception;

    List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws Exception;

    List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws Exception;

    Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId) throws Exception;

    boolean updateApplicationInterface(String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws Exception;

    String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws Exception;

    // --- Application module operations ---
    ApplicationModule getApplicationModule(String appModuleId) throws Exception;

    List<ApplicationModule> getAllAppModules(String gatewayId) throws Exception;

    List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws Exception;

    boolean deleteApplicationModule(String appModuleId) throws Exception;

    boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule) throws Exception;

    String registerApplicationModule(String gatewayId, ApplicationModule applicationModule) throws Exception;

    // --- Parser operations ---
    Parser getParser(String parserId, String gatewayId) throws Exception;

    ParserInput getParserInput(String parserInputId, String gatewayId) throws Exception;

    List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId) throws Exception;

    String saveParser(Parser parser) throws Exception;

    List<Parser> listAllParsers(String gatewayId) throws Exception;

    void removeParser(String parserId, String gatewayId) throws Exception;

    ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws Exception;

    ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws Exception;

    String saveParsingTemplate(ParsingTemplate parsingTemplate) throws Exception;

    List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws Exception;

    void removeParsingTemplate(String templateId, String gatewayId) throws Exception;
}
