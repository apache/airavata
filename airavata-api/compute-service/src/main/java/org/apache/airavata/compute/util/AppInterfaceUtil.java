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
package org.apache.airavata.compute.util;

import java.util.List;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;

public class AppInterfaceUtil {
    public static ApplicationInterfaceDescription createAppInterface(
            String applicationName,
            List<String> appModules,
            List<InputDataObjectType> appInputs,
            List<OutputDataObjectType> appOutputs) {
        return ApplicationInterfaceDescription.newBuilder()
                .setApplicationName(applicationName)
                .addAllApplicationModules(appModules)
                .addAllApplicationInputs(appInputs)
                .addAllApplicationOutputs(appOutputs)
                .build();
    }

    public static InputDataObjectType createApplicationInput(
            String name,
            String value,
            DataType type,
            String applicationArgument,
            int order,
            boolean standardInput,
            String userFriendlyDesc,
            String metadata) {
        return InputDataObjectType.newBuilder()
                .setName(name)
                .setValue(value)
                .setType(type)
                .setMetaData(metadata)
                .setApplicationArgument(applicationArgument)
                .setInputOrder(order)
                .setUserFriendlyDescription(userFriendlyDesc)
                .setStandardInput(standardInput)
                .build();
    }

    public static OutputDataObjectType createApplicationOutput(String name, String value, DataType type) {
        return OutputDataObjectType.newBuilder()
                .setName(name)
                .setValue(value)
                .setType(type)
                .build();
    }
}
