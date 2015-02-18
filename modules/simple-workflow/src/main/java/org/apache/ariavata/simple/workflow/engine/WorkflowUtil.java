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

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;

public class WorkflowUtil {

    public static InputDataObjectType copyValues(InputDataObjectType fromInputObj, InputDataObjectType toInputObj){
        toInputObj.setValue(fromInputObj.getValue());
        if (fromInputObj.getApplicationArgument() != null
                && !fromInputObj.getApplicationArgument().trim().equals("")) {
            toInputObj.setApplicationArgument(fromInputObj.getApplicationArgument());
        }
        return fromInputObj;
    }

    public static InputDataObjectType copyValues(OutputDataObjectType outputData, InputDataObjectType inputData) {
        inputData.setValue(outputData.getValue());
        return inputData;
    }

}
