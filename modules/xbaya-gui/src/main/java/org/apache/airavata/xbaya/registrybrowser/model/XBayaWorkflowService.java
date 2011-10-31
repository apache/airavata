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

package org.apache.airavata.xbaya.registrybrowser.model;

public class XBayaWorkflowService {
    private InputParameters inputParameters;
    private OutputParameters outputParameters;
    private String serviceNodeId;

    public XBayaWorkflowService(String serviceNodeId, InputParameters inputParameters, OutputParameters outputParameters) {
        setServiceNodeId(serviceNodeId);
        setInputParameters(inputParameters);
        setOutputParameters(outputParameters);
    }

    public OutputParameters getOutputParameters() {
        if (outputParameters == null) {
            outputParameters = new OutputParameters((ServiceParameter[]) null);
        }
        return outputParameters;
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters = outputParameters;
    }

    public InputParameters getInputParameters() {
        if (inputParameters == null) {
            inputParameters = new InputParameters((ServiceParameter[]) null);
        }
        return inputParameters;
    }

    public void setInputParameters(InputParameters inputParameters) {
        this.inputParameters = inputParameters;
    }

    public String getServiceNodeId() {
        return serviceNodeId;
    }

    public void setServiceNodeId(String serviceNodeId) {
        this.serviceNodeId = serviceNodeId;
    }
}
