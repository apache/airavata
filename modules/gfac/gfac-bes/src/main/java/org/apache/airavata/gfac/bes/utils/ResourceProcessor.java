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

package org.apache.airavata.gfac.bes.utils;

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.cxf.helpers.XMLUtils;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.ResourceRequestDocument;
import eu.unicore.jsdl.extensions.ResourceRequestType;

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
				}
	
				if(crs.getWallTimeLimit() > 0) {
					RangeValueType cpuTime = new RangeValueType();
					cpuTime.setLowerBound(Double.NaN);
					cpuTime.setUpperBound(Double.NaN);
					long wallTime = crs.getWallTimeLimit() * 60;
					cpuTime.setExact(wallTime);
					JSDLUtils.setIndividualCPUTimeRequirements(value, cpuTime);
				}
				// the total cpu count is total cpus per node 
				if(crs.getTotalCPUCount() > 0) {
					RangeValueType rangeType = new RangeValueType();
					rangeType.setLowerBound(Double.NaN);
					rangeType.setUpperBound(Double.NaN);
					rangeType.setExact(crs.getTotalCPUCount()/crs.getNodeCount());
					JSDLUtils.setIndividualCPUCountRequirements(value, rangeType);
				}

				String qName = crs.getQueueName(); 
				if(!( qName == null || "".equals(qName) ) ) {
					// ignore "default" queue names
					if(! (crs.getQueueName().trim().equalsIgnoreCase("default")) ) {
						ResourceRequestDocument rqDoc = ResourceRequestDocument.Factory.newInstance();
						ResourceRequestType rq = rqDoc.addNewResourceRequest();
						rq.setName("Queue");
						rq.setValue(qName);
						ResourcesType res = JSDLUtils.getOrCreateResources(value);
						WSUtilities.insertAny(rqDoc, res);
					}
				}
				
			} catch (NullPointerException npe) {
				new GFacProviderException("No value set for resource requirements.",npe);
			}
			
			
	}

		

	}
	
	
	

	
	

	
}
