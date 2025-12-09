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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class contains all utility methods across scheduler sub projects
 */
@Component("metaschedulerUtils")
public class Utils {

    private final RegistryService registryService;

    private static Publisher statusPublisher;
    private static ApplicationContext applicationContext;

    public Utils(RegistryService registryService, ApplicationContext applicationContext) {
        this.registryService = registryService;
        Utils.applicationContext = applicationContext;
    }

    public static void saveAndPublishProcessStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId)
            throws RegistryServiceException, AiravataException {
        RegistryService registryService = getRegistryService();
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
        getStatusPublisher().publish(msgCtx);
    }

    public static void updateProcessStatusAndPublishStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId)
            throws RegistryServiceException, AiravataException {
        RegistryService registryService = getRegistryService();
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
        getStatusPublisher().publish(msgCtx);
    }

    public static synchronized Publisher getStatusPublisher() throws AiravataException {
        if (statusPublisher == null) {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
        }
        return statusPublisher;
    }

    // Instance method for Spring DI
    protected RegistryService getRegistryServiceInstance() {
        return registryService;
    }

    // Static method for backward compatibility - delegates to Spring-managed instance
    private static RegistryService getRegistryService() {
        if (applicationContext != null) {
            return applicationContext.getBean(Utils.class).getRegistryServiceInstance();
        }
        throw new RuntimeException("ApplicationContext not available. RegistryService cannot be retrieved.");
    }
}
