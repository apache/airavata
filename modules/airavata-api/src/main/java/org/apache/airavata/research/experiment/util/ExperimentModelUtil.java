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
package org.apache.airavata.research.experiment.util;

import java.util.List;
import org.apache.airavata.core.util.EnumUtil;
import org.apache.airavata.research.application.model.ApplicationInput;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.model.ExperimentInput;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentOutput;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.storage.resource.model.DataType;

public class ExperimentModelUtil {

    public static ProcessModel cloneProcessFromExperiment(ExperimentModel experiment) {
        var processModel = new ProcessModel();
        processModel.setCreationTime(experiment.getCreationTime());
        processModel.setExperimentId(experiment.getExperimentId());
        processModel.setApplicationInterfaceId(experiment.getSourceApplicationInterfaceId());
        processModel.setEnableEmailNotification(experiment.getEnableEmailNotification());
        var emailAddresses = experiment.getEmailAddresses();
        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            processModel.setEmailAddresses(emailAddresses);
        }

        // Convert experiment inputs to process inputs (ApplicationInput)
        if (experiment.getInputs() != null) {
            processModel.setProcessInputs(experiment.getInputs().stream()
                    .map(ExperimentModelUtil::toApplicationInput)
                    .toList());
        }

        // Convert experiment outputs to process outputs (ApplicationOutput)
        if (experiment.getOutputs() != null) {
            processModel.setProcessOutputs(experiment.getOutputs().stream()
                    .map(ExperimentModelUtil::toApplicationOutput)
                    .toList());
        }

        var configData = experiment.getUserConfigurationData();
        if (configData != null) {
            processModel.setInputStorageResourceId(configData.getInputStorageResourceId());
            processModel.setOutputStorageResourceId(configData.getOutputStorageResourceId());
            processModel.setExperimentDataDir(configData.getExperimentDataDir());
            var scheduling = configData.getComputationalResourceScheduling();
            if (scheduling != null) {
                processModel.setProcessResourceSchedule(scheduling);
                processModel.setComputeResourceId(scheduling.getResourceHostId());
            }
            processModel.setUseUserCRPref(configData.getUseUserCRPref());
            processModel.setGroupResourceProfileId(configData.getGroupResourceProfileId());
        }
        processModel.setUserName(experiment.getUserName());
        return processModel;
    }

    /** Convert a typed {@link ExperimentInput} to the legacy {@link ApplicationInput} used by the pipeline. */
    public static ApplicationInput toApplicationInput(ExperimentInput input) {
        var appInput = new ApplicationInput();
        appInput.setName(input.getName());
        appInput.setValue(input.getValue());
        appInput.setType(resolveDataType(input.getType()));
        appInput.setApplicationArgument(input.getCommandLineArg());
        appInput.setIsRequired(input.isRequired());
        appInput.setRequiredToAddedToCommandLine(input.isAddToCommandLine());
        appInput.setInputOrder(input.getOrderIndex());
        appInput.setUserFriendlyDescription(input.getDescription());
        return appInput;
    }

    /** Convert a typed {@link ExperimentOutput} to the legacy {@link ApplicationOutput} used by the pipeline. */
    public static ApplicationOutput toApplicationOutput(ExperimentOutput output) {
        var appOutput = new ApplicationOutput();
        appOutput.setName(output.getName());
        appOutput.setValue(output.getValue());
        appOutput.setType(resolveDataType(output.getType()));
        appOutput.setApplicationArgument(output.getCommandLineArg());
        appOutput.setIsRequired(output.isRequired());
        appOutput.setDataMovement(output.isDataMovement());
        appOutput.setLocation(output.getLocation());
        return appOutput;
    }

    /** Convert a list of {@link ExperimentOutput} to legacy {@link ApplicationOutput} list. */
    public static List<ApplicationOutput> toApplicationOutputs(List<ExperimentOutput> outputs) {
        if (outputs == null) return List.of();
        return outputs.stream().map(ExperimentModelUtil::toApplicationOutput).toList();
    }

    private static DataType resolveDataType(String typeToken) {
        if (typeToken == null || typeToken.isBlank()) {
            return DataType.STRING;
        }
        return EnumUtil.safeValueOf(DataType.class, typeToken.toUpperCase(), DataType.STRING);
    }
}
