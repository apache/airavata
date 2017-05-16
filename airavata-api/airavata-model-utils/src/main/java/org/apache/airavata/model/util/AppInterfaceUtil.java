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
package org.apache.airavata.model.util;

import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;

import java.util.List;

public class AppInterfaceUtil {
    public static ApplicationInterfaceDescription createAppInterface (String applicationName,
                                                                      List<String> appModules,
                                                                      List<InputDataObjectType> appInputs,
                                                                      List<OutputDataObjectType> appOutputs){
        ApplicationInterfaceDescription interfaceDescription = new ApplicationInterfaceDescription();
        interfaceDescription.setApplicationName(applicationName);
        interfaceDescription.setApplicationModules(appModules);
        interfaceDescription.setApplicationInputs(appInputs);
        interfaceDescription.setApplicationOutputs(appOutputs);
        return interfaceDescription;
    }

    public static InputDataObjectType createApplicationInput (String name,
                                                              String value,
                                                              DataType type,
                                                              String applicationArgument,
                                                              int order,
                                                              boolean standardInput,
                                                              String userFriendlyDesc,
                                                              String metadata){
        InputDataObjectType appInput = new InputDataObjectType();
        appInput.setName(name);
        appInput.setValue(value);
        appInput.setType(type);
        appInput.setMetaData(metadata);
        appInput.setApplicationArgument(applicationArgument);
        appInput.setInputOrder(order);
        appInput.setUserFriendlyDescription(userFriendlyDesc);
        appInput.setStandardInput(standardInput);
        return appInput;
    }

    public static OutputDataObjectType createApplicationOutput (String name,
                                                                String value,
                                                                DataType type){
        OutputDataObjectType appOutput = new OutputDataObjectType();
        appOutput.setName(name);
        appOutput.setValue(value);
        appOutput.setType(type);
        return appOutput;
    }
}
