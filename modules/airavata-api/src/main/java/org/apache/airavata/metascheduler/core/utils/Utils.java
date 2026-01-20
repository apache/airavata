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
package org.apache.airavata.metascheduler.core.utils;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.dapr.messaging.DaprMessagingFactory;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.dapr.messaging.Publisher;
import org.apache.airavata.dapr.messaging.Type;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.springframework.stereotype.Component;

/**
 * This class contains all utility methods across scheduler sub projects
 */
@Component("metaschedulerUtils")
public class Utils {

    private final RegistryService registryService;
    private final DaprMessagingFactory messagingFactory;

    private Publisher statusPublisher;
    private static Utils staticInstance;

    public Utils(RegistryService registryService, DaprMessagingFactory messagingFactory) {
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
        Utils.staticInstance = this;
    }

    public static void saveAndPublishProcessStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId)
            throws RegistryException, AiravataException {
        Utils instance = getInstance();
        RegistryService registryService = instance.registryService;
        ProcessStatus processStatus = new ProcessStatus(processState);
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

        registryService.addProcessStatus(processStatus, processId);
        ProcessIdentifier identifier = new ProcessIdentifier(processId, experimentId, gatewayId);
        ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(processState, identifier);
        MessageContext msgCtx = new MessageContext(
                processStatusChangeEvent,
                MessageType.PROCESS,
                AiravataUtils.getId(MessageType.PROCESS.name()),
                gatewayId);
        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        instance.getStatusPublisher().publish(msgCtx);
    }

    public static void updateProcessStatusAndPublishStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId)
            throws RegistryException, AiravataException {
        Utils instance = getInstance();
        RegistryService registryService = instance.registryService;
        ProcessStatus processStatus = new ProcessStatus(processState);
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

        registryService.updateProcessStatus(processStatus, processId);
        ProcessIdentifier identifier = new ProcessIdentifier(processId, experimentId, gatewayId);
        ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(processState, identifier);
        MessageContext msgCtx = new MessageContext(
                processStatusChangeEvent,
                MessageType.PROCESS,
                AiravataUtils.getId(MessageType.PROCESS.name()),
                gatewayId);
        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        instance.getStatusPublisher().publish(msgCtx);
    }

    public synchronized Publisher getStatusPublisher() throws AiravataException {
        if (statusPublisher == null) {
            statusPublisher = messagingFactory.getPublisher(Type.STATUS);
        }
        return statusPublisher;
    }

    // Static method for backward compatibility - delegates to Spring-managed instance
    private static Utils getInstance() {
        if (staticInstance != null) {
            return staticInstance;
        }
        throw new RuntimeException(
                "Utils instance not available. Ensure Utils is properly initialized as a Spring bean.");
    }
}
