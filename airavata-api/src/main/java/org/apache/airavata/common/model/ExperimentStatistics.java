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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: ExperimentStatistics
 */
public class ExperimentStatistics {
    private int allExperimentCount;
    private int completedExperimentCount;
    private int cancelledExperimentCount;
    private int failedExperimentCount;
    private int createdExperimentCount;
    private int runningExperimentCount;
    private List<ExperimentSummaryModel> allExperiments;
    private List<ExperimentSummaryModel> completedExperiments;
    private List<ExperimentSummaryModel> failedExperiments;
    private List<ExperimentSummaryModel> cancelledExperiments;
    private List<ExperimentSummaryModel> createdExperiments;
    private List<ExperimentSummaryModel> runningExperiments;

    public ExperimentStatistics() {}

    public int getAllExperimentCount() {
        return allExperimentCount;
    }

    public void setAllExperimentCount(int allExperimentCount) {
        this.allExperimentCount = allExperimentCount;
    }

    public int getCompletedExperimentCount() {
        return completedExperimentCount;
    }

    public void setCompletedExperimentCount(int completedExperimentCount) {
        this.completedExperimentCount = completedExperimentCount;
    }

    public int getCancelledExperimentCount() {
        return cancelledExperimentCount;
    }

    public void setCancelledExperimentCount(int cancelledExperimentCount) {
        this.cancelledExperimentCount = cancelledExperimentCount;
    }

    public int getFailedExperimentCount() {
        return failedExperimentCount;
    }

    public void setFailedExperimentCount(int failedExperimentCount) {
        this.failedExperimentCount = failedExperimentCount;
    }

    public int getCreatedExperimentCount() {
        return createdExperimentCount;
    }

    public void setCreatedExperimentCount(int createdExperimentCount) {
        this.createdExperimentCount = createdExperimentCount;
    }

    public int getRunningExperimentCount() {
        return runningExperimentCount;
    }

    public void setRunningExperimentCount(int runningExperimentCount) {
        this.runningExperimentCount = runningExperimentCount;
    }

    public List<ExperimentSummaryModel> getAllExperiments() {
        return allExperiments;
    }

    public void setAllExperiments(List<ExperimentSummaryModel> allExperiments) {
        this.allExperiments = allExperiments;
    }

    public List<ExperimentSummaryModel> getCompletedExperiments() {
        return completedExperiments;
    }

    public void setCompletedExperiments(List<ExperimentSummaryModel> completedExperiments) {
        this.completedExperiments = completedExperiments;
    }

    public List<ExperimentSummaryModel> getFailedExperiments() {
        return failedExperiments;
    }

    public void setFailedExperiments(List<ExperimentSummaryModel> failedExperiments) {
        this.failedExperiments = failedExperiments;
    }

    public List<ExperimentSummaryModel> getCancelledExperiments() {
        return cancelledExperiments;
    }

    public void setCancelledExperiments(List<ExperimentSummaryModel> cancelledExperiments) {
        this.cancelledExperiments = cancelledExperiments;
    }

    public List<ExperimentSummaryModel> getCreatedExperiments() {
        return createdExperiments;
    }

    public void setCreatedExperiments(List<ExperimentSummaryModel> createdExperiments) {
        this.createdExperiments = createdExperiments;
    }

    public List<ExperimentSummaryModel> getRunningExperiments() {
        return runningExperiments;
    }

    public void setRunningExperiments(List<ExperimentSummaryModel> runningExperiments) {
        this.runningExperiments = runningExperiments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentStatistics that = (ExperimentStatistics) o;
        return Objects.equals(allExperimentCount, that.allExperimentCount)
                && Objects.equals(completedExperimentCount, that.completedExperimentCount)
                && Objects.equals(cancelledExperimentCount, that.cancelledExperimentCount)
                && Objects.equals(failedExperimentCount, that.failedExperimentCount)
                && Objects.equals(createdExperimentCount, that.createdExperimentCount)
                && Objects.equals(runningExperimentCount, that.runningExperimentCount)
                && Objects.equals(allExperiments, that.allExperiments)
                && Objects.equals(completedExperiments, that.completedExperiments)
                && Objects.equals(failedExperiments, that.failedExperiments)
                && Objects.equals(cancelledExperiments, that.cancelledExperiments)
                && Objects.equals(createdExperiments, that.createdExperiments)
                && Objects.equals(runningExperiments, that.runningExperiments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                allExperimentCount,
                completedExperimentCount,
                cancelledExperimentCount,
                failedExperimentCount,
                createdExperimentCount,
                runningExperimentCount,
                allExperiments,
                completedExperiments,
                failedExperiments,
                cancelledExperiments,
                createdExperiments,
                runningExperiments);
    }

    @Override
    public String toString() {
        return "ExperimentStatistics{" + "allExperimentCount=" + allExperimentCount + ", completedExperimentCount="
                + completedExperimentCount + ", cancelledExperimentCount=" + cancelledExperimentCount
                + ", failedExperimentCount=" + failedExperimentCount + ", createdExperimentCount="
                + createdExperimentCount + ", runningExperimentCount=" + runningExperimentCount + ", allExperiments="
                + allExperiments + ", completedExperiments=" + completedExperiments + ", failedExperiments="
                + failedExperiments + ", cancelledExperiments=" + cancelledExperiments + ", createdExperiments="
                + createdExperiments + ", runningExperiments=" + runningExperiments + "}";
    }
}
