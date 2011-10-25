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

package org.apache.airavata.commons.gfac.type;

import org.apache.airavata.schemas.gfac.ServiceDescriptionType;

public class ServiceDescription implements Type {

    private ServiceDescriptionType serviceDescriptionType;

    public ServiceDescription() {
        this.serviceDescriptionType = ServiceDescriptionType.Factory.newInstance();
    }

    public ServiceDescription(ServiceDescriptionType sdt) {
        this.serviceDescriptionType = sdt;
    }

    public String getId() {
        return serviceDescriptionType.getName();
    }

    public void setId(String id) {
        this.serviceDescriptionType.setName(id);
    }

    public ServiceDescriptionType getServiceDescriptionType() {
        return serviceDescriptionType;
    }

    public org.apache.airavata.schemas.gfac.Parameter[] getInputParameters() {
        return this.serviceDescriptionType.getInputParametersArray();
    }

    public void setInputParameters(org.apache.airavata.schemas.gfac.Parameter[] inputParameters) {
        this.serviceDescriptionType.setInputParametersArray(inputParameters);
    }

    public org.apache.airavata.schemas.gfac.Parameter[] getOutputParameters() {
        return this.serviceDescriptionType.getOutputParametersArray();
    }

    public void setOutputParameters(org.apache.airavata.schemas.gfac.Parameter[] outputParameters) {
        this.serviceDescriptionType.setOutputParametersArray(outputParameters);
    }
}
