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

package org.apache.airavata.xbaya.model.registrybrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.schemas.gfac.Parameter;

public class ServiceParameters {
	private List<ServiceParameter> parameters;
	
	public ServiceParameters(Parameter[] parameters) {
		if (parameters!=null) {
			List<ServiceParameter> serviceParaList = new ArrayList<ServiceParameter>();
			for (Parameter parameter : parameters) {
				serviceParaList.add(new ServiceParameter(parameter));
			}
			setParameters(serviceParaList);
		}
	}
	
	public ServiceParameters(ServiceParameter[] parameters) {
		if (parameters!=null) {
			setParameters(Arrays.asList(parameters));
		}
	}
	public List<ServiceParameter> getParameters() {
		if (parameters==null){
			parameters=new ArrayList<ServiceParameter>();
		}
		return parameters;
	}
	public void setParameters(List<ServiceParameter> parameters) {
		this.parameters = parameters;
	}
}
