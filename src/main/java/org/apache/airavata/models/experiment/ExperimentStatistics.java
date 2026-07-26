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

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.experiment.proto.ExperimentStatistics}.
 */
public record ExperimentStatistics(
        int allExperimentCount,
        int completedExperimentCount,
        int cancelledExperimentCount,
        int failedExperimentCount,
        int createdExperimentCount,
        int runningExperimentCount,
        List<ExperimentSummaryModel> allExperiments,
        List<ExperimentSummaryModel> completedExperiments,
        List<ExperimentSummaryModel> failedExperiments,
        List<ExperimentSummaryModel> cancelledExperiments,
        List<ExperimentSummaryModel> createdExperiments,
        List<ExperimentSummaryModel> runningExperiments) {

    public ExperimentStatistics {
        allExperiments = allExperiments == null ? List.of() : List.copyOf(allExperiments);
        completedExperiments = completedExperiments == null ? List.of() : List.copyOf(completedExperiments);
        failedExperiments = failedExperiments == null ? List.of() : List.copyOf(failedExperiments);
        cancelledExperiments = cancelledExperiments == null ? List.of() : List.copyOf(cancelledExperiments);
        createdExperiments = createdExperiments == null ? List.of() : List.copyOf(createdExperiments);
        runningExperiments = runningExperiments == null ? List.of() : List.copyOf(runningExperiments);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private int allExperimentCount;
        private int completedExperimentCount;
        private int cancelledExperimentCount;
        private int failedExperimentCount;
        private int createdExperimentCount;
        private int runningExperimentCount;
        private List<ExperimentSummaryModel> allExperiments = new ArrayList<>();
        private List<ExperimentSummaryModel> completedExperiments = new ArrayList<>();
        private List<ExperimentSummaryModel> failedExperiments = new ArrayList<>();
        private List<ExperimentSummaryModel> cancelledExperiments = new ArrayList<>();
        private List<ExperimentSummaryModel> createdExperiments = new ArrayList<>();
        private List<ExperimentSummaryModel> runningExperiments = new ArrayList<>();

        private Builder() {}

        private Builder(ExperimentStatistics source) {
            this.allExperimentCount = source.allExperimentCount;
            this.completedExperimentCount = source.completedExperimentCount;
            this.cancelledExperimentCount = source.cancelledExperimentCount;
            this.failedExperimentCount = source.failedExperimentCount;
            this.createdExperimentCount = source.createdExperimentCount;
            this.runningExperimentCount = source.runningExperimentCount;
            this.allExperiments = new ArrayList<>(source.allExperiments);
            this.completedExperiments = new ArrayList<>(source.completedExperiments);
            this.failedExperiments = new ArrayList<>(source.failedExperiments);
            this.cancelledExperiments = new ArrayList<>(source.cancelledExperiments);
            this.createdExperiments = new ArrayList<>(source.createdExperiments);
            this.runningExperiments = new ArrayList<>(source.runningExperiments);
        }

        public Builder setAllExperimentCount(int allExperimentCount) {
            this.allExperimentCount = allExperimentCount;
            return this;
        }

        public Builder setCompletedExperimentCount(int completedExperimentCount) {
            this.completedExperimentCount = completedExperimentCount;
            return this;
        }

        public Builder setCancelledExperimentCount(int cancelledExperimentCount) {
            this.cancelledExperimentCount = cancelledExperimentCount;
            return this;
        }

        public Builder setFailedExperimentCount(int failedExperimentCount) {
            this.failedExperimentCount = failedExperimentCount;
            return this;
        }

        public Builder setCreatedExperimentCount(int createdExperimentCount) {
            this.createdExperimentCount = createdExperimentCount;
            return this;
        }

        public Builder setRunningExperimentCount(int runningExperimentCount) {
            this.runningExperimentCount = runningExperimentCount;
            return this;
        }

        public Builder addAllExperiments(ExperimentSummaryModel value) {
            this.allExperiments.add(value);
            return this;
        }

        public Builder addAllAllExperiments(Iterable<ExperimentSummaryModel> values) {
            values.forEach(this.allExperiments::add);
            return this;
        }

        public Builder clearAllExperiments() {
            this.allExperiments.clear();
            return this;
        }

        public Builder addCompletedExperiments(ExperimentSummaryModel value) {
            this.completedExperiments.add(value);
            return this;
        }

        public Builder addAllCompletedExperiments(Iterable<ExperimentSummaryModel> values) {
            values.forEach(this.completedExperiments::add);
            return this;
        }

        public Builder clearCompletedExperiments() {
            this.completedExperiments.clear();
            return this;
        }

        public Builder addFailedExperiments(ExperimentSummaryModel value) {
            this.failedExperiments.add(value);
            return this;
        }

        public Builder addAllFailedExperiments(Iterable<ExperimentSummaryModel> values) {
            values.forEach(this.failedExperiments::add);
            return this;
        }

        public Builder clearFailedExperiments() {
            this.failedExperiments.clear();
            return this;
        }

        public Builder addCancelledExperiments(ExperimentSummaryModel value) {
            this.cancelledExperiments.add(value);
            return this;
        }

        public Builder addAllCancelledExperiments(Iterable<ExperimentSummaryModel> values) {
            values.forEach(this.cancelledExperiments::add);
            return this;
        }

        public Builder clearCancelledExperiments() {
            this.cancelledExperiments.clear();
            return this;
        }

        public Builder addCreatedExperiments(ExperimentSummaryModel value) {
            this.createdExperiments.add(value);
            return this;
        }

        public Builder addAllCreatedExperiments(Iterable<ExperimentSummaryModel> values) {
            values.forEach(this.createdExperiments::add);
            return this;
        }

        public Builder clearCreatedExperiments() {
            this.createdExperiments.clear();
            return this;
        }

        public Builder addRunningExperiments(ExperimentSummaryModel value) {
            this.runningExperiments.add(value);
            return this;
        }

        public Builder addAllRunningExperiments(Iterable<ExperimentSummaryModel> values) {
            values.forEach(this.runningExperiments::add);
            return this;
        }

        public Builder clearRunningExperiments() {
            this.runningExperiments.clear();
            return this;
        }

        public ExperimentStatistics build() {
            return new ExperimentStatistics(
                    allExperimentCount, completedExperimentCount, cancelledExperimentCount,
                    failedExperimentCount, createdExperimentCount, runningExperimentCount, allExperiments,
                    completedExperiments, failedExperiments, cancelledExperiments, createdExperiments,
                    runningExperiments);
        }
    }
}
