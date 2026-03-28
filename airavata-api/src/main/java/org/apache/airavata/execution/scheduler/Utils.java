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
package org.apache.airavata.execution.scheduler;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.util.AiravataUtils;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Type;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;

/**
 * This class contains all utility methods across scheduler sub projects
 */
public class Utils {

    private static RegistryService.Iface registryHandler;
    private static Publisher statusPublisher;

    /**
     * Sets the registry handler for direct in-JVM calls.
     */
    public static synchronized void setRegistryHandler(RegistryService.Iface handler) {
        registryHandler = handler;
    }

    /**
     * Provides registry handler to access databases
     *
     * @return RegistryService.Iface
     */
    public static synchronized RegistryService.Iface getRegistryHandler() {
        if (registryHandler == null) {
            throw new IllegalStateException("Registry handler has not been initialized");
        }
        return registryHandler;
    }

    public static void saveAndPublishProcessStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId)
            throws RegistryServiceException, TException, AiravataException {
        ProcessStatus processStatus = new ProcessStatus(processState);
        processStatus.setTimeOfStateChange(
                AiravataUtils.getCurrentTimestamp().getTime());

        getRegistryHandler().addProcessStatus(processStatus, processId);
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
            throws RegistryServiceException, TException, AiravataException {
        ProcessStatus processStatus = new ProcessStatus(processState);
        processStatus.setTimeOfStateChange(
                AiravataUtils.getCurrentTimestamp().getTime());

        getRegistryHandler().updateProcessStatus(processStatus, processId);
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
}
