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
import org.apache.airavata.execution.process.ProcessModel;
import org.apache.airavata.research.application.model.ApplicationInput;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.model.Experiment;
import org.apache.airavata.research.experiment.model.ExperimentInput;
import org.apache.airavata.research.experiment.model.ExperimentOutput;

public class ExperimentUtil {

    public static ProcessModel cloneProcessFromExperiment(Experiment experiment) {
        var processModel = new ProcessModel();
        processModel.setCreatedAt(experiment.getCreatedAt());
        processModel.setExperimentId(experiment.getExperimentId());
        processModel.setApplicationInterfaceId(experiment.getApplicationId());

        var configData = experiment.getUserConfigurationData();
        if (configData != null) {
            processModel.setExperimentDataDir(configData.getExperimentDataDir());
            var scheduling = configData.getComputationalResourceScheduling();
            if (scheduling != null) {
                processModel.setResourceSchedule(scheduling);
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
        appInput.setType(input.getType());
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
        appOutput.setType(output.getType());
        appOutput.setApplicationArgument(output.getCommandLineArg());
        appOutput.setIsRequired(output.isRequired());
        appOutput.setDataMovement(output.isDataMovement());
        appOutput.setLocation(output.getLocation());
        return appOutput;
    }

    /** Convert a list of {@link ExperimentOutput} to legacy {@link ApplicationOutput} list. */
    public static List<ApplicationOutput> toApplicationOutputs(List<ExperimentOutput> outputs) {
        if (outputs == null) return List.of();
        return outputs.stream().map(ExperimentUtil::toApplicationOutput).toList();
    }
}
