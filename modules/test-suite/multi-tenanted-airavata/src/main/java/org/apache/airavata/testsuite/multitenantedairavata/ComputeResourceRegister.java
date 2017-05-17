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
package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.testsuite.multitenantedairavata.utils.ComputeResourceProperties;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.*;
import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalEchoComputeResource.*;

public class ComputeResourceRegister {
    private Airavata.Client airavata;
    private Map<String, String> loginNamesWithResourceMap;
    private Map<String, String> loginNamesWithResourceIds;
    private final static Logger logger = LoggerFactory.getLogger(ComputeResourceRegister.class);
    private TestFrameworkProps properties;
    private AuthzToken authzToken;

    public ComputeResourceRegister(Airavata.Client airavata, TestFrameworkProps props) throws Exception {
        this.airavata = airavata;
        this.properties = props;
        loginNamesWithResourceMap = getLoginNamesMap();
        authzToken = new AuthzToken("emptyToken");

    }

    public Map<String, String> getLoginNamesMap() throws Exception {
        loginNamesWithResourceMap = new HashMap<String, String>();
        TestFrameworkProps.Resource[] resourcesWithloginName = properties.getResources();
        if (resourcesWithloginName != null){
            for (TestFrameworkProps.Resource resource : resourcesWithloginName){
                loginNamesWithResourceMap.put(resource.getName(), resource.getLoginUser());
            }
        }
        return loginNamesWithResourceMap;
    }

    public Map<String, String> getLoginNamesWithResourceIDs() throws Exception {
        loginNamesWithResourceIds = new HashMap<String, String>();
        Map<String, String> allComputeResourceNames = airavata.getAllComputeResourceNames(authzToken);
        for (String resourceId : allComputeResourceNames.keySet()) {
            String resourceName = allComputeResourceNames.get(resourceId);
            loginNamesWithResourceIds.put(resourceId, loginNamesWithResourceMap.get(resourceName));
        }

        return loginNamesWithResourceIds;
    }

    public ComputeResourceProperties addComputeResources () throws Exception {
        ComputeResourceProperties computeResourceProperties = null;
        try {
//            Map<String, String> computeResources = airavata.getAllComputeResourceNames(authzToken);
//            for(Map.Entry<String, String> computeResource: computeResources.entrySet()){
//                if(computeResource.getValue().contains("localhost")){
//                    localResourceId =  computeResource.getKey();
//                    System.out.println("Existing Local Resource Id " + localResourceId);
//                    return localResourceId;
//                }
//            }

            for (String resourceName : loginNamesWithResourceMap.keySet()) {
                if (resourceName.contains(RESOURCE_NAME)) {
                    computeResourceProperties = registerComputeHost(HOST_NAME, HOST_DESC,
                            ResourceJobManagerType.FORK, null, "", SecurityProtocol.LOCAL, TestFrameworkConstants.LocalEchoProperties.LocalEchoComputeResource.JOB_MANAGER_COMMAND);
                    System.out.println("Local Resource Id " + computeResourceProperties.getComputeResourceId());
                }
            }

        }catch (Exception e) {
            logger.error("Error occured while adding compute resources", e);
            throw new Exception("Error occured while adding compute resources", e);
        }
        return computeResourceProperties;
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception {
        return airavata.getComputeResource(authzToken, computeResourceId);

    }

    public ComputeResourceProperties registerComputeHost(String hostName, String hostDesc,
                                                         ResourceJobManagerType resourceJobManagerType,
                                                         String monitoringEndPoint, String jobMangerBinPath,
                                                         SecurityProtocol securityProtocol, String jobManagerCommand) throws TException {

        ComputeResourceDescription computeResourceDescription = createComputeResourceDescription(hostName, hostDesc, null, null);

        ComputeResourceProperties computeResourceProperties = new ComputeResourceProperties();

        String computeResourceId = airavata.registerComputeResource(authzToken, computeResourceDescription);
        computeResourceProperties.setComputeResourceId(computeResourceId);

        if (computeResourceId.isEmpty()) throw new AiravataClientException();

        ResourceJobManager resourceJobManager = createResourceJobManager(resourceJobManagerType, monitoringEndPoint, jobMangerBinPath, null);

        if (jobManagerCommand != null) {
            Map<JobManagerCommand, String> jobManagerCommandStringMap = new HashMap<JobManagerCommand, String>();
            jobManagerCommandStringMap.put(JobManagerCommand.SUBMISSION, jobManagerCommand);
            resourceJobManager.setJobManagerCommands(jobManagerCommandStringMap);
        }

        LOCALSubmission localobSubmission = new LOCALSubmission();
        localobSubmission.setResourceJobManager(resourceJobManager);
        localobSubmission.setSecurityProtocol(securityProtocol);

        String localJobSubmissionId = airavata.addLocalSubmissionDetails(authzToken, computeResourceId, 0, localobSubmission);
        computeResourceProperties.setJobSubmissionId(localJobSubmissionId);

        airavata.addLocalDataMovementDetails(authzToken, computeResourceId, DMType.COMPUTE_RESOURCE, 0, new LOCALDataMovement());

        return computeResourceProperties;
    }

    public LOCALSubmission getLocalSubmission(String jobSubmissionId) throws Exception {
        return airavata.getLocalJobSubmission(authzToken, jobSubmissionId);

    }

    public ComputeResourceDescription createComputeResourceDescription(
            String hostName, String hostDesc, List<String> hostAliases, List<String> ipAddresses) {
        ComputeResourceDescription host = new ComputeResourceDescription();
        host.setHostName(hostName);
        host.setResourceDescription(hostDesc);
        host.setIpAddresses(ipAddresses);
        host.setHostAliases(hostAliases);
        host.setEnabled(true);
        return host;
    }

    public ResourceJobManager createResourceJobManager(
            ResourceJobManagerType resourceJobManagerType, String pushMonitoringEndpoint, String jobManagerBinPath,
            Map<JobManagerCommand, String> jobManagerCommands) {
        ResourceJobManager resourceJobManager = new ResourceJobManager();
        resourceJobManager.setResourceJobManagerType(resourceJobManagerType);
        resourceJobManager.setPushMonitoringEndpoint(pushMonitoringEndpoint);
        resourceJobManager.setJobManagerBinPath(jobManagerBinPath);
        resourceJobManager.setJobManagerCommands(jobManagerCommands);
        return resourceJobManager;
    }

    public void registerGatewayResourceProfile(String computeResourceId) throws Exception{
        try {
            ComputeResourcePreference localResourcePreference = null;

            loginNamesWithResourceIds = getLoginNamesWithResourceIDs();
            List<GatewayResourceProfile> allGatewayComputeResources = airavata.getAllGatewayResourceProfiles(authzToken);
            for (GatewayResourceProfile gatewayResourceProfile : allGatewayComputeResources) {
                for (String resourceId : loginNamesWithResourceIds.keySet()) {
                    String loginUserName = loginNamesWithResourceIds.get(resourceId);
                    if (resourceId.equals(computeResourceId) && loginUserName.equals(LOGIN_USER)){
                        localResourcePreference = createComputeResourcePreference(resourceId, ALLOCATION_PROJECT_NUMBER, true, BATCH_QUEUE, JobSubmissionProtocol.LOCAL,
                                DataMovementProtocol.LOCAL, TestFrameworkConstants.SCRATCH_LOCATION, loginUserName);
                        airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, localResourcePreference);

                    }
                }
            }
        } catch (TException e) {
            logger.error("Error occured while updating gateway resource profiles", e);
            throw new Exception("Error occured while updating gateway resource profiles", e);
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayId, String computeResourceId) throws Exception{
       return airavata.getGatewayComputeResourcePreference(authzToken, gatewayId, computeResourceId);
    }

    public ComputeResourcePreference createComputeResourcePreference(String computeResourceId, String allocationProjectNumber,
                                    boolean overridebyAiravata, String preferredBatchQueue,
                                    JobSubmissionProtocol preferredJobSubmissionProtocol,
                                    DataMovementProtocol preferredDataMovementProtocol,
                                    String scratchLocation,
                                    String loginUserName) {
        ComputeResourcePreference computeResourcePreference = new ComputeResourcePreference();
        computeResourcePreference.setComputeResourceId(computeResourceId);
        computeResourcePreference.setOverridebyAiravata(overridebyAiravata);
        computeResourcePreference.setAllocationProjectNumber(allocationProjectNumber);
        computeResourcePreference.setPreferredBatchQueue(preferredBatchQueue);
        computeResourcePreference.setPreferredDataMovementProtocol(preferredDataMovementProtocol);
        computeResourcePreference.setPreferredJobSubmissionProtocol(preferredJobSubmissionProtocol);
        computeResourcePreference.setScratchLocation(scratchLocation);
        computeResourcePreference.setLoginUserName(loginUserName);
        return computeResourcePreference;
    }
}
