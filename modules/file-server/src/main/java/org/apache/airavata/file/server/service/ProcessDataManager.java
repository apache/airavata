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
package org.apache.airavata.file.server.service;

import java.util.UUID;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.helix.adaptor.SSHJAgentAdaptor;
import org.apache.airavata.helix.impl.task.aws.AWSProcessContextManager;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDataManager extends OutputDataStagingTask {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDataManager.class);

    private String processId;
    private AdaptorSupport adaptorSupport;

    private ProcessModel process;
    ExperimentModel experiment;

    public ProcessDataManager(
            ThriftClientPool<RegistryService.Client> registryClientPool,
            String processId,
            AdaptorSupport adaptorSupport)
            throws Exception {

        this.adaptorSupport = adaptorSupport;
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            process = regClient.getProcess(processId);
            experiment = regClient.getExperiment(process.getExperimentId());

            setTaskId(UUID.randomUUID().toString());
            setProcessId(processId);
            setExperimentId(process.getExperimentId());
            setGatewayId(experiment.getGatewayId());
            loadContext();

            registryClientPool.returnResource(regClient);
        } catch (Exception e) {
            logger.error("Failed to initialize the output data mover for process {}", processId, e);
            registryClientPool.returnBrokenResource(regClient);
            throw e;
        }
        this.processId = processId;
    }

    public AgentAdaptor getAgentAdaptor() throws Exception {
        if (getTaskContext().getGroupComputeResourcePreference().getResourceType() == ResourceType.AWS) {
            logger.info("Using AWS adaptor for process {}", processId);

            AWSProcessContextManager awsContext = new AWSProcessContextManager(getTaskContext());
            SSHCredential sshCredential = AgentUtils.getCredentialClient()
                    .getSSHCredential(awsContext.getSSHCredentialToken(), getGatewayId());

            logger.info("Using SSHCredential {} for AWS process {}", sshCredential.getPublicKey(), processId);
            logger.info("AWS public ip is {}", awsContext.getPublicIp());
            SSHJAgentAdaptor adaptor = new SSHJAgentAdaptor();
            adaptor.init(
                    getTaskContext().getComputeResourceLoginUserName(),
                    awsContext.getPublicIp(),
                    22,
                    sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(),
                    sshCredential.getPassphrase());

            return adaptor;
        }

        return getComputeResourceAdaptor(adaptorSupport);
    }

    public String getBaseDir() throws Exception {
        return getTaskContext().getWorkingDir();
    }
}
