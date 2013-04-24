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
package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.ogf.schemas.jsdl.JobDefinitionType;

public class ResourceProcessor {

	
	public static void generateResourceElements(JobDefinitionType value, JobExecutionContext context) throws Exception{
		
		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();
		
		createMemory(value, appDepType);
		
		if (appDepType.getCpuCount() > 0) {
			UCRangeValueType rangeType = new UCRangeValueType();
			rangeType.setLowerBound(Double.NaN);
			rangeType.setUpperBound(Double.NaN);
			rangeType.setExact(appDepType.getCpuCount());
			JSDLUtils.setTotalCPUCountRequirements(value, rangeType);
		}

		if (appDepType.getProcessorsPerNode() > 0) {
			UCRangeValueType rangeType = new UCRangeValueType();
			rangeType.setLowerBound(Double.NaN);
			rangeType.setUpperBound(Double.NaN);
			rangeType.setExact(appDepType.getProcessorsPerNode());
			JSDLUtils.setIndividualCPUCountRequirements(value, rangeType);
		}
		
		if (appDepType.getNodeCount() > 0) {
			UCRangeValueType rangeType = new UCRangeValueType();
			rangeType.setLowerBound(Double.NaN);
			rangeType.setUpperBound(Double.NaN);
			rangeType.setExact(appDepType.getNodeCount());
			JSDLUtils.setTotalResourceCountRequirements(value, rangeType);
		}
		
		if(appDepType.getMaxWallTime() > 0) {
			UCRangeValueType cpuTime = new UCRangeValueType();
			cpuTime.setLowerBound(Double.NaN);
			cpuTime.setUpperBound(Double.NaN);
			long wallTime = appDepType.getMaxWallTime() * 60;
			cpuTime.setExact(wallTime);
			JSDLUtils.setIndividualCPUTimeRequirements(value, cpuTime);
		}
	}
	
	
	private static void createMemory(JobDefinitionType value, HpcApplicationDeploymentType appDepType){
		if (appDepType.getMinMemory() > 0 && appDepType.getMaxMemory() > 0) {
			UCRangeValueType rangeType = new UCRangeValueType();
			rangeType.setLowerBound(appDepType.getMinMemory());
			rangeType.setUpperBound(appDepType.getMaxMemory());
			JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
		}

		else if (appDepType.getMinMemory() > 0 && appDepType.getMaxMemory() <= 0) {
			// TODO set Wall time
			UCRangeValueType rangeType = new UCRangeValueType();
			rangeType.setLowerBound(appDepType.getMinMemory());
			JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
		}
		
		else if (appDepType.getMinMemory() <= 0 && appDepType.getMaxMemory() > 0) {
			// TODO set Wall time
			UCRangeValueType rangeType = new UCRangeValueType();
			rangeType.setUpperBound(appDepType.getMinMemory());
			JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
		}
		
	}

	
	

	
}
