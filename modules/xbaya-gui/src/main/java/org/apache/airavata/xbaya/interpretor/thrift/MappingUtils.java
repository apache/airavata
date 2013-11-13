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

package org.apache.airavata.xbaya.interpretor.thrift;

import org.apache.airavata.client.api.*;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.impl.*;
import org.apache.airavata.client.tools.NameValuePairType;

import java.util.ArrayList;
import java.util.List;

public class MappingUtils {
    public static ExperimentAdvanceOptions getExperimentOptionsObject(org.apache.airavata.experiment.execution.ExperimentAdvanceOptions advanceOptions){
        try {
            ExperimentAdvanceOptions experimentAdvanceOptions = new ExperimentAdvanceOptions();
            experimentAdvanceOptions.setCustomExperimentId(advanceOptions.getCustomExperimentId());
            experimentAdvanceOptions.setExperimentExecutionUser(advanceOptions.getExecutionUser());
            experimentAdvanceOptions.setExperimentCustomMetadata(advanceOptions.getMetadata());
            experimentAdvanceOptions.setExperimentName(advanceOptions.getExperimentName());

            if (advanceOptions.getWorkflowSchedulingSettings() != null){
                List<org.apache.airavata.experiment.execution.NodeSettings> nodeSettingsList = advanceOptions.getWorkflowSchedulingSettings().getNodeSettingsList();
                for (org.apache.airavata.experiment.execution.NodeSettings nodeSettings : nodeSettingsList){
                    NodeSettings n = getNodeSettingObject(nodeSettings);
                    org.apache.airavata.experiment.execution.HPCSettings hpcSettings = nodeSettings.getHpcSettings();
                    HPCSettings hpcSettingsObject = getHPCSettingsObject(hpcSettings);
                    n.setHPCSettings(hpcSettingsObject);
                    org.apache.airavata.experiment.execution.HostSchedulingSettings hostSchedulingSettings = nodeSettings.getHostSchedulingSettings();
                    HostSchedulingSettings hostSchedulingSettingsObj = getHostSchedulingSettingsObj(hostSchedulingSettings);
                    n.setHostSettings(hostSchedulingSettingsObj);
                    experimentAdvanceOptions.getCustomWorkflowSchedulingSettings().addNewNodeSettings(n);
                }
            }

            org.apache.airavata.experiment.execution.WorkflowOutputDataSettings workflowOutputDataSettings = advanceOptions.getWorkflowOutputDataSettings();
            if (workflowOutputDataSettings != null){
                WorkflowOutputDataSettings dataSettings = getWorkflowOutputDataSettingsObject(workflowOutputDataSettings);
                experimentAdvanceOptions.getCustomWorkflowOutputDataSettings().addNewOutputDataSettings(dataSettings.getOutputDataSettingsList());
            }

            SecuritySettings securitySettings = experimentAdvanceOptions.getCustomSecuritySettings();
            org.apache.airavata.experiment.execution.SecuritySettings securitySettings1 = advanceOptions.getSecuritySettings();
            if (securitySettings != null && securitySettings1 != null){
                AmazonWebServicesSettings amazonWSSettings = securitySettings.getAmazonWSSettings();
                if (amazonWSSettings != null){
                    amazonWSSettings.setAccessKeyId(securitySettings1.getAmazonWSSettings().getAccessKey());
                    amazonWSSettings.setAMIId(securitySettings1.getAmazonWSSettings().getAmiID());
                    amazonWSSettings.setInstanceId(securitySettings1.getAmazonWSSettings().getInstanceID());
                    amazonWSSettings.setInstanceType(securitySettings1.getAmazonWSSettings().getInstanceType());
                    amazonWSSettings.setSecretAccessKey(securitySettings1.getAmazonWSSettings().getSecretAccessKey());
                    amazonWSSettings.setUsername(securitySettings1.getAmazonWSSettings().getUsername());
                }
                CredentialStoreSecuritySettings storeSecuritySettings = securitySettings.getCredentialStoreSecuritySettings();
                if (storeSecuritySettings != null){
                    storeSecuritySettings.setGatewayId(securitySettings1.getCredentialStoreSettings().getGatewayID());
                    storeSecuritySettings.setPortalUser(securitySettings1.getCredentialStoreSettings().getPortalUser());
                    storeSecuritySettings.setTokenId(securitySettings1.getCredentialStoreSettings().getTokenId());
                }

//                GridMyProxyRepositorySettings myProxyRepositorySettings = securitySettings.getGridMyProxyRepositorySettings();
//                if (myProxyRepositorySettings != null){
//                    myProxyRepositorySettings.setUsername(securitySettings1.getMyproxySettings().getUserName());
//                    myProxyRepositorySettings.setPassword(securitySettings1.getMyproxySettings().getPassword());
//                    myProxyRepositorySettings.setLifeTime(securitySettings1.getMyproxySettings().getLifetime());
//                    myProxyRepositorySettings.setMyProxyServer(securitySettings1.getMyproxySettings().getMyproxyServer());
//                }
//
//                SSHAuthenticationSettings authenticationSettings = securitySettings.getSSHAuthenticationSettings();
//                if (authenticationSettings != null){
//                    authenticationSettings.setAccessKeyId(securitySettings1.getSshAuthSettings().getAccessKeyID());
//                    authenticationSettings.setSecretAccessKey(securitySettings1.getSshAuthSettings().getSecretAccessKey());
//                }
            }
            return experimentAdvanceOptions;
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static NodeSettingsImpl getNodeSettingObject (org.apache.airavata.experiment.execution.NodeSettings nodeSettings){
        NodeSettings n = new NodeSettingsImpl(nodeSettings.getNodeId());
        n.setServiceId(nodeSettings.getServiceId());
        List<NameValuePairType> nameValuePairTypes = new ArrayList<NameValuePairType>();
        List<org.apache.airavata.experiment.execution.NameValuePairType> nameValuePairList = nodeSettings.getNameValuePairList();
        for (org.apache.airavata.experiment.execution.NameValuePairType np : nameValuePairList) {
            NameValuePairType nameValuePairObject = getNameValuePairObject(np);
            nameValuePairTypes.add(nameValuePairObject);
        }
        n.setNameValuePair(nameValuePairTypes);
        return (NodeSettingsImpl)n;

    }

    public static NameValuePairType getNameValuePairObject(org.apache.airavata.experiment.execution.NameValuePairType nameValuePairType){
        NameValuePairType nameValuePair = new NameValuePairType();
        nameValuePair.setName(nameValuePairType.getName());
        nameValuePair.setValue(nameValuePairType.getValue());
        nameValuePair.setDescription(nameValuePairType.getDescription());
        return nameValuePair;
    }

    public static HPCSettings getHPCSettingsObject (org.apache.airavata.experiment.execution.HPCSettings hsettings){
        HPCSettings hpcSettings = new HPCSettingsImpl();
        hpcSettings.setCPUCount(hsettings.getCpuCount());
        hpcSettings.setJobManager(hsettings.getJobManager());
        hpcSettings.setMaxWallTime(hsettings.getMaxWalltime());
        hpcSettings.setNodeCount(hsettings.getNodeCount());
        hpcSettings.setQueueName(hsettings.getQueueName());
        return hpcSettings;
    }

    public static HostSchedulingSettings getHostSchedulingSettingsObj (org.apache.airavata.experiment.execution.HostSchedulingSettings schedulingSettings){
        HostSchedulingSettings hostSchedulingSettings = new HostSchedulingSettingsImpl();
        hostSchedulingSettings.setHostId(schedulingSettings.getHostID());
        hostSchedulingSettings.setGatekeeperEPR(schedulingSettings.getGatekeeperEPR());
        hostSchedulingSettings.setWSGramPreffered(schedulingSettings.isIsWSGramPreferred());
        return hostSchedulingSettings;
    }

    public static WorkflowOutputDataSettings getWorkflowOutputDataSettingsObject(org.apache.airavata.experiment.execution.WorkflowOutputDataSettings wfods) {
        WorkflowOutputDataSettings wfOutDataSettings = new WorkflowOutputDataSettingsImpl();
        List<org.apache.airavata.experiment.execution.OutputDataSettings> dataSettingsList = wfods.getOutputDataSettingsList();
        for (org.apache.airavata.experiment.execution.OutputDataSettings outPutDSettng : dataSettingsList) {
            OutputDataSettings outputDataSettings = new ApplicationOutputDataSettingsImpl(outPutDSettng.getNodeID());
            outputDataSettings.setDataPersistent(outPutDSettng.isIsdataPersisted());
            outputDataSettings.setDataRegistryUrl(outPutDSettng.getDataRegURL());
            outputDataSettings.setOutputDataDirectory(outPutDSettng.getOutputdataDir());
            wfOutDataSettings.addNewOutputDataSettings(outputDataSettings);
        }
        return wfOutDataSettings;
    }



}
