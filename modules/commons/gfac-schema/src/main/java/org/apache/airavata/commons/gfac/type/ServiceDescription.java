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

    public Parameter[] getInputParameters() {    	
    	int length = this.serviceDescriptionType.getInputParametersArray().length;
    	Parameter[] result = new Parameter[length];
    	for (int i = 0; i < length; i++) {
    		result[i] = new Parameter(this.serviceDescriptionType.getInputParametersArray(i));			
		}
        return result; 
    }

    public void setInputParameters(Parameter[] inputParameters) {
    	int length = inputParameters.length;
    	org.apache.airavata.schemas.gfac.Parameter[] result = new org.apache.airavata.schemas.gfac.Parameter[length];
    	for (int i = 0; i < length; i++) {
    		result[i] = inputParameters[i].getParameterType();			
		}
    	
        this.serviceDescriptionType.setInputParametersArray(result);
    }

    public Parameter[] getOutputParameters() {
    	int length = this.serviceDescriptionType.getOutputParametersArray().length;
    	Parameter[] result = new Parameter[length];
    	for (int i = 0; i < length; i++) {
    		result[i] = new Parameter(this.serviceDescriptionType.getOutputParametersArray(i));			
		}    	
        return result;
    }

    public void setOutputParameters(Parameter[] outputParameters) {
    	int length = outputParameters.length;
    	org.apache.airavata.schemas.gfac.Parameter[] result = new org.apache.airavata.schemas.gfac.Parameter[length];
    	for (int i = 0; i < length; i++) {
    		result[i] = outputParameters[i].getParameterType();			
		}
    	
        this.serviceDescriptionType.setInputParametersArray(result);
    }
}
