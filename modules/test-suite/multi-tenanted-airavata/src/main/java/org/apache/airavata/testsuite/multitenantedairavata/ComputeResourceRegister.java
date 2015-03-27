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
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeResourceRegister {
    private Airavata.Client airavata;
    private List<String> computeResourceIds;

    public ComputeResourceRegister(Airavata.Client airavata) {
        this.airavata = airavata;
        computeResourceIds = new ArrayList<String>();
    }

    public void addComputeResources () throws TException {
        // adding stampede
        String stampedeResourceId = registerComputeHost("stampede.tacc.xsede.org", "TACC Stampede Cluster",
                ResourceJobManagerType.SLURM, "push", "/usr/bin", SecurityProtocol.SSH_KEYS, 22, null);
        System.out.println("Stampede Resource Id is " + stampedeResourceId);

        //Register Trestles
        String trestlesResourceId = registerComputeHost("trestles.sdsc.xsede.org", "SDSC Trestles Cluster",
                ResourceJobManagerType.PBS, "push", "/opt/torque/bin/", SecurityProtocol.SSH_KEYS, 22, null);
        System.out.println("Trestles Resource Id is " + trestlesResourceId);

        //Register BigRedII
        String bigredResourceId = registerComputeHost("bigred2.uits.iu.edu", "IU BigRed II Cluster",
                ResourceJobManagerType.PBS, "push", "/opt/torque/torque-4.2.3.1/bin/", SecurityProtocol.SSH_KEYS, 22, "aprun -n");
        System.out.println("BigredII Resource Id is " + bigredResourceId);

        computeResourceIds.add(stampedeResourceId);
        computeResourceIds.add(trestlesResourceId);
        computeResourceIds.add(bigredResourceId);
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

    public static ComputeResourceDescription createComputeResourceDescription(
            String hostName, String hostDesc, List<String> hostAliases, List<String> ipAddresses) {
        ComputeResourceDescription host = new ComputeResourceDescription();
        host.setHostName(hostName);
        host.setResourceDescription(hostDesc);
        host.setIpAddresses(ipAddresses);
        host.setHostAliases(hostAliases);
        return host;
    }

    public static ResourceJobManager createResourceJobManager(
            ResourceJobManagerType resourceJobManagerType, String pushMonitoringEndpoint, String jobManagerBinPath,
            Map<JobManagerCommand, String> jobManagerCommands) {
        ResourceJobManager resourceJobManager = new ResourceJobManager();
        resourceJobManager.setResourceJobManagerType(resourceJobManagerType);
        resourceJobManager.setPushMonitoringEndpoint(pushMonitoringEndpoint);
        resourceJobManager.setJobManagerBinPath(jobManagerBinPath);
        resourceJobManager.setJobManagerCommands(jobManagerCommands);
        return resourceJobManager;
    }
}
