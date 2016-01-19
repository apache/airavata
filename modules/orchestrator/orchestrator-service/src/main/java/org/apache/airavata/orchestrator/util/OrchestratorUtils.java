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
package org.apache.airavata.orchestrator.util;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorUtils {
	private static final Logger log = LoggerFactory.getLogger(OrchestratorUtils.class);

	public static void updageAndPublishExperimentStatus(String experimentId, ExperimentStatus status, Publisher publisher, String gatewayId) {
		try {
			RegistryFactory.getDefaultExpCatalog().update(ExperimentCatalogModelType.EXPERIMENT_STATUS, status,
					experimentId);
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(status.getState(),
                    experimentId,
                    gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
		} catch (RegistryException e) {
			log.error("expId : " + experimentId + " Error while updating experiment status to " + status.toString(), e);
		} catch (AiravataException e) {
            log.error("expId : " + experimentId + " Error while publishing experiment status to " + status.toString(), e);
        }
    }

	public static ExperimentStatus getExperimentStatus(String experimentId) throws RegistryException {
		return ((ExperimentStatus) RegistryFactory.getDefaultExpCatalog().get(ExperimentCatalogModelType
						.EXPERIMENT_STATUS,
				experimentId));
	}
}
