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
package org.apache.airavata.research.experiment.model;

import java.util.List;
import java.util.Objects;
import org.apache.airavata.compute.resource.model.ComputationalResourceSchedulingModel;

/**
 * Domain model: UserConfigurationDataModel
 */
public class UserConfigurationDataModel {
    private boolean airavataAutoSchedule;
    private boolean overrideManualScheduledParams;
    private boolean shareExperimentPublicly;
    private ComputationalResourceSchedulingModel computationalResourceScheduling;
    private boolean throttleResources;
    private String userDN;
    private boolean generateCert;
    private String inputStorageResourceId;
    private String outputStorageResourceId;
    private String experimentDataDir;
    private boolean useUserCRPref;
    private String groupResourceProfileId;
    private List<ComputationalResourceSchedulingModel> autoScheduledCompResourceSchedulingList;

    public UserConfigurationDataModel() {}

    public boolean getAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean getOverrideManualScheduledParams() {
        return overrideManualScheduledParams;
    }

    public void setOverrideManualScheduledParams(boolean overrideManualScheduledParams) {
        this.overrideManualScheduledParams = overrideManualScheduledParams;
    }

    public boolean getShareExperimentPublicly() {
        return shareExperimentPublicly;
    }

    public void setShareExperimentPublicly(boolean shareExperimentPublicly) {
        this.shareExperimentPublicly = shareExperimentPublicly;
    }

    public ComputationalResourceSchedulingModel getComputationalResourceScheduling() {
        return computationalResourceScheduling;
    }

    public void setComputationalResourceScheduling(
            ComputationalResourceSchedulingModel computationalResourceScheduling) {
        this.computationalResourceScheduling = computationalResourceScheduling;
    }

    public boolean getThrottleResources() {
        return throttleResources;
    }

    public void setThrottleResources(boolean throttleResources) {
        this.throttleResources = throttleResources;
    }

    public String getUserDN() {
        return userDN;
    }

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    public boolean getGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public String getInputStorageResourceId() {
        return inputStorageResourceId;
    }

    public void setInputStorageResourceId(String inputStorageResourceId) {
        this.inputStorageResourceId = inputStorageResourceId;
    }

    public String getOutputStorageResourceId() {
        return outputStorageResourceId;
    }

    public void setOutputStorageResourceId(String outputStorageResourceId) {
        this.outputStorageResourceId = outputStorageResourceId;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public boolean getUseUserCRPref() {
        return useUserCRPref;
    }

    public void setUseUserCRPref(boolean useUserCRPref) {
        this.useUserCRPref = useUserCRPref;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public List<ComputationalResourceSchedulingModel> getAutoScheduledCompResourceSchedulingList() {
        return autoScheduledCompResourceSchedulingList;
    }

    public void setAutoScheduledCompResourceSchedulingList(
            List<ComputationalResourceSchedulingModel> autoScheduledCompResourceSchedulingList) {
        this.autoScheduledCompResourceSchedulingList = autoScheduledCompResourceSchedulingList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConfigurationDataModel that = (UserConfigurationDataModel) o;
        return Objects.equals(airavataAutoSchedule, that.airavataAutoSchedule)
                && Objects.equals(overrideManualScheduledParams, that.overrideManualScheduledParams)
                && Objects.equals(shareExperimentPublicly, that.shareExperimentPublicly)
                && Objects.equals(computationalResourceScheduling, that.computationalResourceScheduling)
                && Objects.equals(throttleResources, that.throttleResources)
                && Objects.equals(userDN, that.userDN)
                && Objects.equals(generateCert, that.generateCert)
                && Objects.equals(inputStorageResourceId, that.inputStorageResourceId)
                && Objects.equals(outputStorageResourceId, that.outputStorageResourceId)
                && Objects.equals(experimentDataDir, that.experimentDataDir)
                && Objects.equals(useUserCRPref, that.useUserCRPref)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(
                        autoScheduledCompResourceSchedulingList, that.autoScheduledCompResourceSchedulingList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                airavataAutoSchedule,
                overrideManualScheduledParams,
                shareExperimentPublicly,
                computationalResourceScheduling,
                throttleResources,
                userDN,
                generateCert,
                inputStorageResourceId,
                outputStorageResourceId,
                experimentDataDir,
                useUserCRPref,
                groupResourceProfileId,
                autoScheduledCompResourceSchedulingList);
    }

    @Override
    public String toString() {
        return "UserConfigurationDataModel{" + "airavataAutoSchedule=" + airavataAutoSchedule
                + ", overrideManualScheduledParams=" + overrideManualScheduledParams + ", shareExperimentPublicly="
                + shareExperimentPublicly + ", computationalResourceScheduling=" + computationalResourceScheduling
                + ", throttleResources=" + throttleResources + ", userDN=" + userDN + ", generateCert=" + generateCert
                + ", inputStorageResourceId=" + inputStorageResourceId + ", outputStorageResourceId="
                + outputStorageResourceId + ", experimentDataDir=" + experimentDataDir + ", useUserCRPref="
                + useUserCRPref + ", groupResourceProfileId=" + groupResourceProfileId
                + ", autoScheduledCompResourceSchedulingList=" + autoScheduledCompResourceSchedulingList + "}";
    }
}
