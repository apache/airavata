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

package org.apache.airavata.xbaya.interpretor.thrift;


import org.apache.airavata.client.AiravataAPIUtils;
import org.apache.airavata.experiment.execution.ExperimentAdvanceOptions;
import org.apache.airavata.experiment.execution.InterpreterService;

import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorSkeleton;
import org.apache.thrift.TException;

import java.util.Map;

public class InterpreterServiceHandler implements InterpreterService.Iface{
    private WorkflowInterpretorSkeleton interpreterService;

    public String runExperiment(String workflowTemplateName, Map<String, String> workflowInputs, ExperimentAdvanceOptions experimentAdOptions) throws TException {
        String user =  "admin";
        String gatewayId = "default";
        try {
            return getInterpreterService().setupAndLaunch(workflowTemplateName,
                    experimentAdOptions.getCustomExperimentId(),
                    gatewayId,
                    user,
                    workflowInputs,
                    true,
                    AiravataAPIUtils.createWorkflowContextHeaderBuilder(MappingUtils.getExperimentOptionsObject(experimentAdOptions), experimentAdOptions.getExecutionUser(), user));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cancelExperiment(String experimentID) throws TException {
        try {
            getInterpreterService().haltWorkflow(experimentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void suspendExperiment(String experimentID) throws TException {
        try {
            getInterpreterService().suspendWorkflow(experimentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumeExperiment(String experimentID) throws TException {
        try {
            getInterpreterService().resumeWorkflow(experimentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WorkflowInterpretorSkeleton getInterpreterService() {
        if (interpreterService==null){
            interpreterService=new WorkflowInterpretorSkeleton();
        }
        return interpreterService;
    }
}
