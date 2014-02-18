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

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.schemas.gfac.BooleanParameterType;

import java.util.List;

public class ConfigDataResource extends AbstractResource {
    private ExperimentResource experimentResource;
    private boolean airavataAutoSchedule;
    private boolean overrideManualParams;
    private boolean shareExp;

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public boolean isAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean isOverrideManualParams() {
        return overrideManualParams;
    }

    public void setOverrideManualParams(boolean overrideManualParams) {
        this.overrideManualParams = overrideManualParams;
    }

    public boolean isShareExp() {
        return shareExp;
    }

    public void setShareExp(boolean shareExp) {
        this.shareExp = shareExp;
    }

    @Override
    public Resource create(ResourceType type) {
        return null;
    }

    @Override
    public void remove(ResourceType type, Object name) {

    }

    @Override
    public Resource get(ResourceType type, Object name) {
        return null;
    }

    @Override
    public List<Resource> get(ResourceType type) {
        return null;
    }

    @Override
    public void save() {

    }
}
