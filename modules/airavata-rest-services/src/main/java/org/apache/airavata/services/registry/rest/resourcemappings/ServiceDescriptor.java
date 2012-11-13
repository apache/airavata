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

package org.apache.airavata.services.registry.rest.resourcemappings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "service")
public class ServiceDescriptor {

    private String serviceName;
    private String description;
    private List<ServiceParameters> inputParams = new ArrayList<ServiceParameters>();
    private List<ServiceParameters> outputParams = new ArrayList<ServiceParameters>();

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServiceParameters> getInputParams() {
        return inputParams;
    }

    public void setInputParams(List<ServiceParameters> inputParams) {
        this.inputParams = inputParams;
    }

    public List<ServiceParameters> getOutputParams() {
        return outputParams;
    }

    public void setOutputParams(List<ServiceParameters> outputParams) {
        this.outputParams = outputParams;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
