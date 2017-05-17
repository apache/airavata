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
package org.apache.airavata.workflow.core;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.workflow.core.parser.JsonWorkflowParser;
import org.apache.airavata.workflow.core.parser.WorkflowParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * All classes implement this WorkflowFactory interface, should be abstract or singleton.
 */
public class WorkflowFactory {

    private static final Logger log = LoggerFactory.getLogger(WorkflowFactory.class);

    public static WorkflowParser getWorkflowParser(String workflowString) throws Exception {
        WorkflowParser workflowParser = null;
        try {
            String wfParserClassName = ServerSettings.getWorkflowParser();
            Class<?> aClass = Class.forName(wfParserClassName);
            Constructor<?> constructor = aClass.getConstructor(String.class);
            workflowParser = (WorkflowParser) constructor.newInstance(workflowString);
        } catch (ApplicationSettingsException e) {
            log.info("A custom workflow parser is not defined, Use default Airavata JSON workflow parser");
        }
        if (workflowParser == null) {
            workflowParser = new JsonWorkflowParser(workflowString);
        }
        return workflowParser;
    }

}
