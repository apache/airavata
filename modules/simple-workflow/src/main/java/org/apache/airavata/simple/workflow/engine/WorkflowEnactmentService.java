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

package org.apache.airavata.simple.workflow.engine;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.messaging.core.impl.RabbitMQProcessPublisher;
import org.apache.airavata.registry.cpi.RegistryException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkflowEnactmentService {

    private static WorkflowEnactmentService workflowEnactmentService;
    private ExecutorService executor;

    private WorkflowEnactmentService () {
        executor = Executors.newFixedThreadPool(getThreadPoolSize());
    }

    public static WorkflowEnactmentService getInstance(){
        if (workflowEnactmentService == null) {
            synchronized (WorkflowEnactmentService.class) {
                if (workflowEnactmentService == null) {
                    workflowEnactmentService = new WorkflowEnactmentService();
                }
            }
        }
        return workflowEnactmentService;
    }

    public void submitWorkflow(String experimentId,
                                  String credentialToken,
                                  String gatewayName,
                                  RabbitMQProcessPublisher publisher) throws RegistryException {

        SimpleWorkflowInterpreter simpleWorkflowInterpreter = new SimpleWorkflowInterpreter(
                experimentId, credentialToken,gatewayName, publisher);
        executor.execute(simpleWorkflowInterpreter);
    }

    private int getThreadPoolSize() {
        return ServerSettings.getEnactmentThreadPoolSize();
    }
}
