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
package org.apache.airavata.gfac.bes.utils;

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.experiment.TaskDetails;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public class ResourceProcessor {

	
	public static void generateResourceElements(JobDefinitionType value, JobExecutionContext context) throws Exception{
		
		TaskDetails taskData = context.getTaskData();
		
		
		
		if(taskData != null && taskData.isSetTaskScheduling()){
			try {
				
				ComputationalResourceScheduling crs = taskData.getTaskScheduling();
			
				if (crs.getTotalPhysicalMemory() > 0) {
					RangeValueType rangeType = new RangeValueType();
					rangeType.setLowerBound(Double.NaN);
					rangeType.setUpperBound(Double.NaN);
					rangeType.setExact(crs.getTotalPhysicalMemory());
					JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
				}
				
				if (crs.getNodeCount() > 0) {
					RangeValueType rangeType = new RangeValueType();
					rangeType.setLowerBound(Double.NaN);
					rangeType.setUpperBound(Double.NaN);
					rangeType.setExact(crs.getNodeCount());
					JSDLUtils.setTotalResourceCountRequirements(value, rangeType);
					// set totalcpu count to -1 as we dont need that
					crs.setTotalCPUCount(0);
				}
	
				if(crs.getWallTimeLimit() > 0) {
					RangeValueType cpuTime = new RangeValueType();
					cpuTime.setLowerBound(Double.NaN);
					cpuTime.setUpperBound(Double.NaN);
					long wallTime = crs.getWallTimeLimit() * 60;
					cpuTime.setExact(wallTime);
					JSDLUtils.setIndividualCPUTimeRequirements(value, cpuTime);
				}
				
				if(crs.getTotalCPUCount() > 0) {
					RangeValueType rangeType = new RangeValueType();
					rangeType.setLowerBound(Double.NaN);
					rangeType.setUpperBound(Double.NaN);
					rangeType.setExact(crs.getTotalCPUCount());
					JSDLUtils.setTotalCPUCountRequirements(value, rangeType);
				}
				
			} catch (NullPointerException npe) {
				new GFacProviderException("No value set for resource requirements.",npe);
			}
			
			
	}

		

	}
	
	
	

	
	

	
}
