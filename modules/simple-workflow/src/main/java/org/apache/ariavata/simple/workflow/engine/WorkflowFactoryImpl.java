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

package org.apache.ariavata.simple.workflow.engine;

import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.ariavata.simple.workflow.engine.parser.AiravataDefaultParser;

/**
 * Singleton class, only one instance can exist in runtime.
 */
public class WorkflowFactoryImpl implements WorkflowFactory {

    private static WorkflowFactoryImpl workflowFactoryImpl;

    private WorkflowParser workflowParser;

    private static final String synch = "sync";

    private WorkflowFactoryImpl(){

    }

    public static WorkflowFactoryImpl getInstance() {
        if (workflowFactoryImpl == null) {
            synchronized (synch) {
                if (workflowFactoryImpl == null) {
                    workflowFactoryImpl = new WorkflowFactoryImpl();
                }
            }
        }
        return workflowFactoryImpl;
    }


    @Override
    public WorkflowParser getWorkflowParser(String experimentId, String credentialToken) {
        if (workflowParser == null) {
            try {
                workflowParser = new AiravataDefaultParser(experimentId, credentialToken);
            } catch (RegistryException e) {
                // TODO : handle this scenario
            }
        }
        return workflowParser;
    }

}
