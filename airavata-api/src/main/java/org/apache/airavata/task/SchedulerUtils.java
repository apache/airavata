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
package org.apache.airavata.task;

import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.util.AiravataUtils;

/**
 * This class contains all utility methods across scheduler sub projects
 */
public class SchedulerUtils {

    private static RegistryHandler registryHandler;

    /**
     * Sets the registry handler for direct in-JVM calls.
     */
    public static synchronized void setRegistryHandler(RegistryHandler handler) {
        registryHandler = handler;
    }

    /**
     * Provides registry handler to access databases
     *
     * @return RegistryHandler
     */
    public static synchronized RegistryHandler getRegistryHandler() {
        if (registryHandler == null) {
            throw new IllegalStateException("Registry handler has not been initialized");
        }
        return registryHandler;
    }

    public static void saveAndPublishProcessStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId) throws Exception {
        ProcessStatus processStatus = ProcessStatus.newBuilder()
                .setState(processState)
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();

        getRegistryHandler().addProcessStatus(processStatus, processId);
    }

    public static void updateProcessStatusAndPublishStatus(
            ProcessState processState, String processId, String experimentId, String gatewayId) throws Exception {
        ProcessStatus processStatus = ProcessStatus.newBuilder()
                .setState(processState)
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();

        getRegistryHandler().updateProcessStatus(processStatus, processId);
    }
}
