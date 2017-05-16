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
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.MethodNotFoundException;
import java.util.List;

public class ExperimentStatisticsResource extends AbstractExpCatResource {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentStatisticsResource.class);

    private int allExperimentCount;
    private int completedExperimentCount;
    private int createdExperimentCount;
    private int runningExperimentCount;
    private int cancelledExperimentCount;
    private int failedExperimentCount;

    private List<ExperimentSummaryResource> allExperiments;
    private List<ExperimentSummaryResource> createdExperiments;
    private List<ExperimentSummaryResource> runningExperiments;
    private List<ExperimentSummaryResource> completedExperiments;
    private List<ExperimentSummaryResource> cancelledExperiments;
    private List<ExperimentSummaryResource> failedExperiments;

    @Override
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public void remove(ResourceType type, Object name) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public ExperimentResource get(ResourceType type, Object name) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException {
        throw new MethodNotFoundException();
    }

    @Override
    public void save() throws RegistryException {
        throw new MethodNotFoundException();
    }

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

    public List<ExperimentSummaryResource> getAllExperiments() {
        return allExperiments;
    }

    public void setAllExperiments(List<ExperimentSummaryResource> allExperiments) {
        this.allExperiments = allExperiments;
    }

    public List<ExperimentSummaryResource> getCompletedExperiments() {
        return completedExperiments;
    }

    public void setCompletedExperiments(List<ExperimentSummaryResource> completedExperiments) {
        this.completedExperiments = completedExperiments;
    }

    public List<ExperimentSummaryResource> getCancelledExperiments() {
        return cancelledExperiments;
    }

    public void setCancelledExperiments(List<ExperimentSummaryResource> cancelledExperiments) {
        this.cancelledExperiments = cancelledExperiments;
    }

    public List<ExperimentSummaryResource> getFailedExperiments() {
        return failedExperiments;
    }

    public void setFailedExperiments(List<ExperimentSummaryResource> failedExperiments) {
        this.failedExperiments = failedExperiments;
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

    public List<ExperimentSummaryResource> getCreatedExperiments() {
        return createdExperiments;
    }

    public void setCreatedExperiments(List<ExperimentSummaryResource> createdExperiments) {
        this.createdExperiments = createdExperiments;
    }

    public List<ExperimentSummaryResource> getRunningExperiments() {
        return runningExperiments;
    }

    public void setRunningExperiments(List<ExperimentSummaryResource> runningExperiments) {
        this.runningExperiments = runningExperiments;
    }
}