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
package org.apache.airavata.models.experiment;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.scheduling.ComputationalResourceSchedulingModel;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.experiment.proto.UserConfigurationDataModel}.
 *
 * @param computationalResourceScheduling {@code null} if unset, matching the generated
 *     {@code hasComputationalResourceScheduling()} presence check.
 */
public record UserConfigurationDataModel(
        boolean airavataAutoSchedule,
        boolean overrideManualScheduledParams,
        boolean shareExperimentPublicly,
        ComputationalResourceSchedulingModel computationalResourceScheduling,
        boolean throttleResources,
        String inputStorageResourceId,
        String outputStorageResourceId,
        String experimentDataDir,
        boolean useUserCrPref,
        String groupResourceProfileId,
        List<ComputationalResourceSchedulingModel> autoScheduledCompResourceSchedulingList) {

    public UserConfigurationDataModel {
        autoScheduledCompResourceSchedulingList = autoScheduledCompResourceSchedulingList == null
                ? List.of()
                : List.copyOf(autoScheduledCompResourceSchedulingList);
    }

    public boolean hasComputationalResourceScheduling() {
        return computationalResourceScheduling != null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private boolean airavataAutoSchedule;
        private boolean overrideManualScheduledParams;
        private boolean shareExperimentPublicly;
        private ComputationalResourceSchedulingModel computationalResourceScheduling;
        private boolean throttleResources;
        private String inputStorageResourceId = "";
        private String outputStorageResourceId = "";
        private String experimentDataDir = "";
        private boolean useUserCrPref;
        private String groupResourceProfileId = "";
        private List<ComputationalResourceSchedulingModel> autoScheduledCompResourceSchedulingList =
                new ArrayList<>();

        private Builder() {}

        private Builder(UserConfigurationDataModel source) {
            this.airavataAutoSchedule = source.airavataAutoSchedule;
            this.overrideManualScheduledParams = source.overrideManualScheduledParams;
            this.shareExperimentPublicly = source.shareExperimentPublicly;
            this.computationalResourceScheduling = source.computationalResourceScheduling;
            this.throttleResources = source.throttleResources;
            this.inputStorageResourceId = source.inputStorageResourceId;
            this.outputStorageResourceId = source.outputStorageResourceId;
            this.experimentDataDir = source.experimentDataDir;
            this.useUserCrPref = source.useUserCrPref;
            this.groupResourceProfileId = source.groupResourceProfileId;
            this.autoScheduledCompResourceSchedulingList =
                    new ArrayList<>(source.autoScheduledCompResourceSchedulingList);
        }

        public Builder setAiravataAutoSchedule(boolean airavataAutoSchedule) {
            this.airavataAutoSchedule = airavataAutoSchedule;
            return this;
        }

        public Builder setOverrideManualScheduledParams(boolean overrideManualScheduledParams) {
            this.overrideManualScheduledParams = overrideManualScheduledParams;
            return this;
        }

        public Builder setShareExperimentPublicly(boolean shareExperimentPublicly) {
            this.shareExperimentPublicly = shareExperimentPublicly;
            return this;
        }

        public Builder setComputationalResourceScheduling(
                ComputationalResourceSchedulingModel computationalResourceScheduling) {
            this.computationalResourceScheduling = computationalResourceScheduling;
            return this;
        }

        public Builder setThrottleResources(boolean throttleResources) {
            this.throttleResources = throttleResources;
            return this;
        }

        public Builder setInputStorageResourceId(String inputStorageResourceId) {
            this.inputStorageResourceId = inputStorageResourceId;
            return this;
        }

        public Builder setOutputStorageResourceId(String outputStorageResourceId) {
            this.outputStorageResourceId = outputStorageResourceId;
            return this;
        }

        public Builder setExperimentDataDir(String experimentDataDir) {
            this.experimentDataDir = experimentDataDir;
            return this;
        }

        public Builder setUseUserCrPref(boolean useUserCrPref) {
            this.useUserCrPref = useUserCrPref;
            return this;
        }

        public Builder setGroupResourceProfileId(String groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId;
            return this;
        }

        public Builder addAutoScheduledCompResourceSchedulingList(ComputationalResourceSchedulingModel value) {
            this.autoScheduledCompResourceSchedulingList.add(value);
            return this;
        }

        public Builder addAllAutoScheduledCompResourceSchedulingList(
                Iterable<ComputationalResourceSchedulingModel> values) {
            values.forEach(this.autoScheduledCompResourceSchedulingList::add);
            return this;
        }

        public Builder clearAutoScheduledCompResourceSchedulingList() {
            this.autoScheduledCompResourceSchedulingList.clear();
            return this;
        }

        public UserConfigurationDataModel build() {
            return new UserConfigurationDataModel(
                    airavataAutoSchedule, overrideManualScheduledParams, shareExperimentPublicly,
                    computationalResourceScheduling, throttleResources, inputStorageResourceId,
                    outputStorageResourceId, experimentDataDir, useUserCrPref, groupResourceProfileId,
                    autoScheduledCompResourceSchedulingList);
        }
    }
}
