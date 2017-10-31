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
package org.apache.airavata.k8s.api.resources.application;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationIfaceResource {
    private long id;
    private String name;
    private String description;
    private long applicationModuleId;
    private List<ApplicationInputResource> inputs = new ArrayList<>();
    private List<ApplicationOutputResource> outputs = new ArrayList<>();

    public long getId() {
        return id;
    }

    public ApplicationIfaceResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationIfaceResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationIfaceResource setDescription(String description) {
        this.description = description;
        return this;
    }

    public long getApplicationModuleId() {
        return applicationModuleId;
    }

    public ApplicationIfaceResource setApplicationModuleId(long applicationModuleId) {
        this.applicationModuleId = applicationModuleId;
        return this;
    }

    public List<ApplicationInputResource> getInputs() {
        return inputs;
    }

    public ApplicationIfaceResource setInputs(List<ApplicationInputResource> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<ApplicationOutputResource> getOutputs() {
        return outputs;
    }

    public ApplicationIfaceResource setOutputs(List<ApplicationOutputResource> outputs) {
        this.outputs = outputs;
        return this;
    }
}
