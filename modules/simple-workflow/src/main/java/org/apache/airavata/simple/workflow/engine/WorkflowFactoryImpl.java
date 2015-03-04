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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.simple.workflow.engine.parser.AiravataWorkflowParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Singleton class, only one instance can exist in runtime.
 */
public class WorkflowFactoryImpl implements WorkflowFactory {

    private static final Logger log = LoggerFactory.getLogger(WorkflowFactoryImpl.class);

    private static WorkflowFactoryImpl workflowFactoryImpl;

    private WorkflowFactoryImpl(){

    }

    public static WorkflowFactoryImpl getInstance() {
        if (workflowFactoryImpl == null) {
            synchronized (WorkflowFactory.class) {
                if (workflowFactoryImpl == null) {
                    workflowFactoryImpl = new WorkflowFactoryImpl();
                }
            }
        }
        return workflowFactoryImpl;
    }


    @Override
    public WorkflowParser getWorkflowParser(String experimentId, String credentialToken) throws Exception {
        WorkflowParser workflowParser = null;
        try {
            String wfParserClassName = ServerSettings.getWorkflowParser();
            Class<?> aClass = Class.forName(wfParserClassName);
            Constructor<?> constructor = aClass.getConstructor(String.class, String.class);
            workflowParser = (WorkflowParser) constructor.newInstance(experimentId, credentialToken);
        } catch (ApplicationSettingsException e) {
            log.info("A custom workflow parser is not defined, Use default Airavata workflow parser");
        }
        if (workflowParser == null) {
            workflowParser = new AiravataWorkflowParser(experimentId, credentialToken);
        }
        return workflowParser;
    }

}
