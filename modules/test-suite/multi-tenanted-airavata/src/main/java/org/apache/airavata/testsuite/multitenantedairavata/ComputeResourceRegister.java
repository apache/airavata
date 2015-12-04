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
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeResourceRegister {
    private Airavata.Client airavata;
    private List<String> computeResourceIds;
    private Map<String, String> loginNamesWithResourceMap;
    private Map<String, String> loginNamesWithResourceIds;
    private final static Logger logger = LoggerFactory.getLogger(ComputeResourceRegister.class);
    private TestFrameworkProps properties;
    private AuthzToken authzToken;

    public ComputeResourceRegister(Airavata.Client airavata, TestFrameworkProps props) throws Exception {
        this.airavata = airavata;
        this.properties = props;
        computeResourceIds = new ArrayList<String>();
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

    public void addComputeResources () throws Exception {
        String stampedeResourceId = null;
        String trestlesResourceId = null;
        String bigredResourceId = null;
        String gordenResourceId = null;
        String alamoResourceId = null;
        try {
            for (String resourceName : loginNamesWithResourceMap.keySet()) {
                if (resourceName.contains("stampede")) {
                    // adding stampede
                    stampedeResourceId = registerComputeHost(resourceName, "TACC Stampede Cluster",
                            ResourceJobManagerType.SLURM, "push", "/usr/bin", SecurityProtocol.SSH_KEYS, 22, "ibrun");
                    System.out.println("Stampede Resource Id is " + stampedeResourceId);
                } else if (resourceName.contains("trestles")) {
                    //Register Trestles
                    trestlesResourceId = registerComputeHost("trestles.sdsc.xsede.org", "SDSC Trestles Cluster",
                            ResourceJobManagerType.PBS, "push", "/opt/torque/bin/", SecurityProtocol.SSH_KEYS, 22, "mpirun -np");
                    System.out.println("Trestles Resource Id is " + trestlesResourceId);
                } else if (resourceName.contains("bigred2")) {
                    //Register BigRedII
                    bigredResourceId = registerComputeHost("bigred2.uits.iu.edu", "IU BigRed II Cluster",
                            ResourceJobManagerType.PBS, "push", "/opt/torque/torque-5.0.1/bin/", SecurityProtocol.SSH_KEYS, 22, "aprun -n");
                    System.out.println("BigredII Resource Id is " + bigredResourceId);
                } else if (resourceName.contains("gordon")) {
                    //Register BigRedII
                    gordenResourceId = registerComputeHost("gordon.sdsc.edu", "SDSC Gorden Cluster",
                            ResourceJobManagerType.PBS, "push", "/opt/torque/bin/", SecurityProtocol.SSH_KEYS, 22, "mpirun_rsh -hostfile $PBS_NODEFILE -np");
                    System.out.println("BigredII Resource Id is " + bigredResourceId);
                } else if (resourceName.contains("alamo")) {
                    //Register BigRedII
                    alamoResourceId = registerComputeHost("alamo.uthscsa.edu", "TACC alamo Cluster",
                            ResourceJobManagerType.PBS, "push", "/usr/bin/", SecurityProtocol.SSH_KEYS, 22, " /usr/bin/mpiexec -np");
                    System.out.println("BigredII Resource Id is " + bigredResourceId);
                }
            }
            computeResourceIds.add(stampedeResourceId);
            computeResourceIds.add(trestlesResourceId);
            computeResourceIds.add(bigredResourceId);
            computeResourceIds.add(gordenResourceId);
            computeResourceIds.add(alamoResourceId);
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

        String computeResourceId = airavata.registerComputeResource(authzToken, computeResourceDescription);

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
//        sshJobSubmission.setMonitorMode(MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR);
        sshJobSubmission.setSshPort(portNumber);
        airavata.addSSHJobSubmissionDetails(authzToken, computeResourceId, 1, sshJobSubmission);

        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setSecurityProtocol(securityProtocol);
        scpDataMovement.setSshPort(portNumber);
        airavata.addSCPDataMovementDetails(authzToken, computeResourceId, DMType.COMPUTE_RESOURCE, 1, scpDataMovement);

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
            ComputeResourcePreference stampedeOGCEResourcePreferences = null;
            ComputeResourcePreference stampedeUS3ResourcePreferences = null;
            ComputeResourcePreference trestlesOGCEResourcePreferences = null;
            ComputeResourcePreference trestlesUS3ResourcePreferences = null;
            ComputeResourcePreference bigRedCgatewayResourcePreferences = null;
            ComputeResourcePreference gordenUS3ResourcePreference = null;
            ComputeResourcePreference alamoUS3ResourcePreference = null;

            loginNamesWithResourceIds = getLoginNamesWithResourceIDs();

            List<GatewayResourceProfile> allGatewayComputeResources = airavata.getAllGatewayResourceProfiles(authzToken);
            for (GatewayResourceProfile gatewayResourceProfile : allGatewayComputeResources) {
                for (String resourceId : loginNamesWithResourceIds.keySet()) {
                    String loginUserName = loginNamesWithResourceIds.get(resourceId);
                    if (resourceId.contains("stampede") ) {
                        if (loginUserName.equals("ogce")){
                            stampedeOGCEResourcePreferences = createComputeResourcePreference(resourceId, "TG-STA110014S", false, null,
                                    JobSubmissionProtocol.SSH, DataMovementProtocol.SCP, "/scratch/01437/ogce/gta-work-dirs", loginUserName);
                            airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, stampedeOGCEResourcePreferences);
                        }else if (loginUserName.equals("us3")){
                            stampedeUS3ResourcePreferences = createComputeResourcePreference(resourceId, "TG-MCB070039N", false, null,
                                    JobSubmissionProtocol.SSH, DataMovementProtocol.SCP, "/scratch/01623/us3/jobs/", loginUserName);
                            airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, stampedeUS3ResourcePreferences);
                        }
                    }else if (resourceId.contains("trestles")){
                        if (loginUserName.equals("ogce")){
                            trestlesOGCEResourcePreferences = createComputeResourcePreference(resourceId, "sds128", false, null, JobSubmissionProtocol.SSH,
                                    DataMovementProtocol.SCP, "/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs", loginUserName);
                            airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, trestlesOGCEResourcePreferences);
                        }else if (loginUserName.equals("us3")){
                            trestlesUS3ResourcePreferences = createComputeResourcePreference(resourceId, "uot111", false, null, JobSubmissionProtocol.SSH,
                                    DataMovementProtocol.SCP, "/oasis/projects/nsf/uot111/us3/airavata-workdirs/", loginUserName);
                            airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, trestlesUS3ResourcePreferences);
                        }
                    }else if (resourceId.contains("bigred2") && loginUserName.equals("cgateway")){
                        bigRedCgatewayResourcePreferences = createComputeResourcePreference(resourceId, "TG-STA110014S", false, null, null, null,
                                "/N/dc2/scratch/cgateway/gta-work-dirs", loginUserName);
                        airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, bigRedCgatewayResourcePreferences);
                    }else if (resourceId.contains("gordon") && loginUserName.equals("us3")){
                        gordenUS3ResourcePreference = createComputeResourcePreference(resourceId, "uot111", false, null, JobSubmissionProtocol.SSH,
                                DataMovementProtocol.SCP, "/home/us3/gordon/work/airavata", loginUserName);
                        airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, gordenUS3ResourcePreference);
                    }else if (resourceId.contains("alamo") && loginUserName.equals("us3")){
                        alamoUS3ResourcePreference = createComputeResourcePreference(resourceId, null, false, "batch", JobSubmissionProtocol.SSH,
                                DataMovementProtocol.SCP, "/home/us3/work/airavata", loginUserName);
                        airavata.addGatewayComputeResourcePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, alamoUS3ResourcePreference);
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
