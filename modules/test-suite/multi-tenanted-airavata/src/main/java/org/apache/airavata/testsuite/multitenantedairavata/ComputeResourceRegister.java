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

package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyFileType;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyReader;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ComputeResourceRegister {
    private Airavata.Client airavata;
    private List<String> computeResourceIds;
    private PropertyReader propertyReader;
    private Map<String, String> loginNamesWithResourceMap;
    private Map<String, String> loginNamesWithResourceIds;
    private final static Logger logger = LoggerFactory.getLogger(ComputeResourceRegister.class);

    public ComputeResourceRegister(Airavata.Client airavata) throws Exception {
        this.airavata = airavata;
        computeResourceIds = new ArrayList<String>();
        propertyReader = new PropertyReader();
        loginNamesWithResourceMap = getLoginNamesMap();

    }

    public Map<String, String> getLoginNamesMap() throws Exception {
        loginNamesWithResourceMap = new HashMap<String, String>();
        List<String> loginNameList = new ArrayList<String>();
        List<String> computerResources = new ArrayList<String>();
        String loginNames = propertyReader.readProperty(TestFrameworkConstants.FrameworkPropertiesConstants.LOGIN_USERNAME_LIST, PropertyFileType.TEST_FRAMEWORK);
        if (loginNames != null && !loginNames.isEmpty()){
            String[] names = loginNames.split(",");
            loginNameList = Arrays.asList(names);
        }
        String clist = propertyReader.readProperty(TestFrameworkConstants.FrameworkPropertiesConstants.COMPUTE_RESOURCE_LIST, PropertyFileType.TEST_FRAMEWORK);
        if (clist != null && !clist.isEmpty()) {
            String[] resources = clist.split(",");
            computerResources = Arrays.asList(resources);
        }

        if (computerResources.size() == loginNameList.size()){
            for (int i=0; i < computerResources.size(); i++){
                loginNamesWithResourceMap.put(computerResources.get(i), loginNameList.get(i));
            }
        }else {
           logger.error("Each compute resource should have a login user name. Please check whether you specified them correctly " +
                   "in test-framework.properties files..");
            throw new Exception("Each compute resource should have a login user name. Please check whether you specified them correctly " +
                    "in test-framework.properties files..");
        }
        return loginNamesWithResourceMap;
    }

    public Map<String, String> getLoginNamesWithResourceIDs() throws Exception {
        loginNamesWithResourceIds = new HashMap<String, String>();
        Map<String, String> allComputeResourceNames = airavata.getAllComputeResourceNames();
        for (String resourceId : allComputeResourceNames.keySet()) {
            String resourceName = allComputeResourceNames.get(resourceId);
            loginNamesWithResourceIds.put(resourceId, loginNamesWithResourceMap.get(resourceName));
        }

        return loginNamesWithResourceIds;
    }

    public void addComputeResources () throws Exception {
        String stampedeResourceId = null;
        String trestlesResourceId = null;
        String bigredResourceId = null;
        try {
            for (String resourceName : loginNamesWithResourceMap.keySet()) {
                if (resourceName.contains("stampede")) {
                    // adding stampede
                    stampedeResourceId = registerComputeHost(resourceName, "TACC Stampede Cluster",
                            ResourceJobManagerType.SLURM, "push", "/usr/bin", SecurityProtocol.SSH_KEYS, 22, null);
                    System.out.println("Stampede Resource Id is " + stampedeResourceId);
                } else if (resourceName.contains("trestles")) {
                    //Register Trestles
                    trestlesResourceId = registerComputeHost("trestles.sdsc.xsede.org", "SDSC Trestles Cluster",
                            ResourceJobManagerType.PBS, "push", "/opt/torque/bin/", SecurityProtocol.SSH_KEYS, 22, null);
                    System.out.println("Trestles Resource Id is " + trestlesResourceId);
                } else if (resourceName.contains("bigred2")) {
                    //Register BigRedII
                    bigredResourceId = registerComputeHost("bigred2.uits.iu.edu", "IU BigRed II Cluster",
                            ResourceJobManagerType.PBS, "push", "/opt/torque/torque-4.2.3.1/bin/", SecurityProtocol.SSH_KEYS, 22, "aprun -n");
                    System.out.println("BigredII Resource Id is " + bigredResourceId);
                }
            }
            computeResourceIds.add(stampedeResourceId);
            computeResourceIds.add(trestlesResourceId);
            computeResourceIds.add(bigredResourceId);
        }catch (Exception e) {
            logger.error("Error occured while adding compute resources", e);
            throw new Exception("Error occured while adding compute resources", e);
        }

    }

    public String registerComputeHost(String hostName, String hostDesc,
                                      ResourceJobManagerType resourceJobManagerType,
                                      String monitoringEndPoint, String jobMangerBinPath,
                                      SecurityProtocol securityProtocol, int portNumber, String jobManagerCommand) throws TException {

        ComputeResourceDescription computeResourceDescription = createComputeResourceDescription(hostName, hostDesc, null, null);

        String computeResourceId = airavata.registerComputeResource(computeResourceDescription);

        if (computeResourceId.isEmpty()) throw new AiravataClientException();

        ResourceJobManager resourceJobManager = createResourceJobManager(resourceJobManagerType, monitoringEndPoint, jobMangerBinPath, null);

        if (jobManagerCommand != null) {
            Map<JobManagerCommand, String> jobManagerCommandStringMap = new HashMap<JobManagerCommand, String>();
            jobManagerCommandStringMap.put(JobManagerCommand.SUBMISSION, jobManagerCommand);
            resourceJobManager.setJobManagerCommands(jobManagerCommandStringMap);
        }

        SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
        sshJobSubmission.setResourceJobManager(resourceJobManager);
        sshJobSubmission.setSecurityProtocol(securityProtocol);
        sshJobSubmission.setSshPort(portNumber);
        airavata.addSSHJobSubmissionDetails(computeResourceId, 1, sshJobSubmission);

        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setSecurityProtocol(securityProtocol);
        scpDataMovement.setSshPort(portNumber);
        airavata.addSCPDataMovementDetails(computeResourceId, 1, scpDataMovement);

        return computeResourceId;
    }

    public ComputeResourceDescription createComputeResourceDescription(
            String hostName, String hostDesc, List<String> hostAliases, List<String> ipAddresses) {
        ComputeResourceDescription host = new ComputeResourceDescription();
        host.setHostName(hostName);
        host.setResourceDescription(hostDesc);
        host.setIpAddresses(ipAddresses);
        host.setHostAliases(hostAliases);
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

    public void registerGatewayResourceProfile() throws Exception{
        try {
            ComputeResourcePreference stampedeResourcePreferences = null;
            ComputeResourcePreference trestlesResourcePreferences = null;
            ComputeResourcePreference bigRedResourcePreferences = null;

            loginNamesWithResourceIds = getLoginNamesWithResourceIDs();

            List<GatewayResourceProfile> allGatewayComputeResources = airavata.getAllGatewayComputeResources();
            for (GatewayResourceProfile gatewayResourceProfile : allGatewayComputeResources) {
                for (String resourceId : loginNamesWithResourceIds.keySet()) {
                    if (resourceId.contains("stampede")) {
                        stampedeResourcePreferences = createComputeResourcePreference(resourceId, "TG-STA110014S", false, null,
                                JobSubmissionProtocol.SSH, DataMovementProtocol.SCP, "/scratch/01437/ogce/gta-work-dirs", loginNamesWithResourceIds.get(resourceId));
                        airavata.addGatewayComputeResourcePreference(gatewayResourceProfile.getGatewayID(), resourceId, stampedeResourcePreferences);
                    }else if (resourceId.contains("trestles")){
                        trestlesResourcePreferences = createComputeResourcePreference(resourceId, "sds128", false, null, JobSubmissionProtocol.SSH,
                                DataMovementProtocol.SCP, "/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs", loginNamesWithResourceIds.get(resourceId));
                        airavata.addGatewayComputeResourcePreference(gatewayResourceProfile.getGatewayID(), resourceId, trestlesResourcePreferences);
                    }else if (resourceId.contains("bigred2")){
                        bigRedResourcePreferences = createComputeResourcePreference(resourceId, "TG-STA110014S", false, null, null, null,
                                "/N/dc2/scratch/cgateway/gta-work-dirs", loginNamesWithResourceIds.get(resourceId));
                        airavata.addGatewayComputeResourcePreference(gatewayResourceProfile.getGatewayID(), resourceId, bigRedResourcePreferences);
                    }
                }
            }
        } catch (TException e) {
            logger.error("Error occured while updating gateway resource profiles", e);
            throw new Exception("Error occured while updating gateway resource profiles", e);
        }
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

    public List<String> getComputeResourceIds() {
        return computeResourceIds;
    }

    public void setComputeResourceIds(List<String> computeResourceIds) {
        this.computeResourceIds = computeResourceIds;
    }
}
