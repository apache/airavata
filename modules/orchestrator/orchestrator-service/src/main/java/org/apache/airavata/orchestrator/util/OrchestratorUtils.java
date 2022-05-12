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
package org.apache.airavata.orchestrator.util;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorUtils {
	private static final Logger log = LoggerFactory.getLogger(OrchestratorUtils.class);

	public static void updateAndPublishExperimentStatus(String experimentId, ExperimentStatus status, Publisher publisher, String gatewayId) throws TException {
		RegistryService.Client registryClient = null;
		try {
			registryClient = getRegistryServiceClient();
			registryClient.updateExperimentStatus(status,
					experimentId);
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(status.getState(),
                    experimentId,
                    gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
		} catch (AiravataException e) {
            log.error("expId : " + experimentId + " Error while publishing experiment status to " + status.toString(), e);
        } finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
    }

	public static ExperimentStatus getExperimentStatus(String experimentId) throws TException, ApplicationSettingsException {
		RegistryService.Client registryClient = null;
		try {
			registryClient = getRegistryServiceClient();
			return registryClient.getExperimentStatus(experimentId);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

	public static ProcessModel getProcess(String processId) throws TException, ApplicationSettingsException {
		RegistryService.Client registryClient = null;
		try {
			registryClient = getRegistryServiceClient();
			return registryClient.getProcess(processId);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

	private static RegistryService.Client getRegistryServiceClient() throws ApplicationSettingsException {
		final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
		final String serverHost = ServerSettings.getRegistryServerHost();
		try {
			return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
		} catch (RegistryServiceException e) {
			throw new RuntimeException("Unable to create registry client...", e);
		}
	}

}
