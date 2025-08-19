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
import org.apache.airavata.datatransfer.api.AgentAdaptor;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.helix.adaptor.SSHJAgentAdaptor;
import org.apache.airavata.helix.impl.task.aws.AWSProcessContextManager;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDataManager extends OutputDataStagingTask {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDataManager.class);

    private String processId;
    private AdaptorSupport adaptorSupport;

    private ProcessModel process;
    ExperimentModel experiment;

    public ProcessDataManager(
            RegistryService.Iface registry,
            String processId,
            AdaptorSupport adaptorSupport)
            throws Exception {

        this.adaptorSupport = adaptorSupport;
        try {
            process = registry.getProcess(processId);
            experiment = registry.getExperiment(process.getExperimentId());

            setTaskId(UUID.randomUUID().toString());
            setProcessId(processId);
            setExperimentId(process.getExperimentId());
            setGatewayId(experiment.getGatewayId());
            loadContext();
        } catch (Exception e) {
            logger.error("Failed to initialize the output data mover for process {}", processId, e);
            throw e;
        }
        this.processId = processId;
    }

    public AgentAdaptor getAgentAdaptor() throws Exception {
        if (getTaskContext().getGroupComputeResourcePreference().getResourceType() == ResourceType.AWS) {
            logger.info("Using AWS adaptor for process {}", processId);

            AWSProcessContextManager awsContext = new AWSProcessContextManager(getTaskContext());
            SSHCredential sshCredential = ProcessDataManager.getCredentialStoreClient()
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
    private static CredentialStoreService.Client getCredentialStoreClient()
            throws TTransportException, ApplicationSettingsException {
        TTransport transport = new TSocket(
                ServerSettings.getApiServerHost(),
                Integer.parseInt(ServerSettings.getApiServerPort()));
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        protocol = new TMultiplexedProtocol(protocol, "CredentialStoreService");
        return new CredentialStoreService.Client(protocol);
    }

    public String getBaseDir() throws Exception {
        return getTaskContext().getWorkingDir();
    }
}
