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
package org.apache.airavata.messaging;

import java.util.UUID;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.MessageType;

/**
 * Utility class for messaging-related test operations.
 * Provides helper methods for creating test messages and message contexts.
 */
public class TestMessagingUtils {

    /**
     * Creates a test MessageContext for an experiment status change event.
     */
    public static MessageContext createExperimentStatusChangeMessage(
            String experimentId, String gatewayId, ExperimentState state) {
        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
        event.setState(state);
        event.setExperimentId(experimentId);

        String messageId = "msg-" + UUID.randomUUID().toString();
        return new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
    }

    /**
     * Creates a test MessageContext with default values.
     */
    public static MessageContext createTestMessageContext(String gatewayId) {
        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
        event.setState(ExperimentState.CREATED);
        event.setExperimentId("exp-" + UUID.randomUUID().toString());

        String messageId = "msg-" + UUID.randomUUID().toString();
        return new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
    }

    /**
     * Creates a test MessageContext for a process status change event.
     */
    public static MessageContext createProcessStatusChangeMessage(
            String processId, String experimentId, String gatewayId, 
            org.apache.airavata.common.model.ProcessState state) {
        org.apache.airavata.common.model.ProcessStatusChangeEvent event =
                new org.apache.airavata.common.model.ProcessStatusChangeEvent();
        event.setState(state);
        
        org.apache.airavata.common.model.ProcessIdentifier processIdentity = 
                new org.apache.airavata.common.model.ProcessIdentifier();
        processIdentity.setProcessId(processId);
        processIdentity.setExperimentId(experimentId);
        processIdentity.setGatewayId(gatewayId);
        event.setProcessIdentity(processIdentity);

        String messageId = "msg-" + UUID.randomUUID().toString();
        return new MessageContext(event, MessageType.PROCESS, messageId, gatewayId);
    }
}

