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
package org.apache.airavata.gfac.impl.task.utils.bes;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.ResourceRequestDocument;
import eu.unicore.jsdl.extensions.ResourceRequestType;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;

public class ResourceProcessor {

	
	public static void generateResourceElements(JobDefinitionType value, ProcessContext context) throws Exception {
        ProcessModel processModel = context.getProcessModel();
        if (processModel != null) {
            try {
                ComputationalResourceSchedulingModel crs = processModel.getProcessResourceSchedule();

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

                if (crs.getWallTimeLimit() > 0) {
                    RangeValueType cpuTime = new RangeValueType();
                    cpuTime.setLowerBound(Double.NaN);
                    cpuTime.setUpperBound(Double.NaN);
                    long wallTime = crs.getWallTimeLimit() * 60;
                    cpuTime.setExact(wallTime);
                    JSDLUtils.setIndividualCPUTimeRequirements(value, cpuTime);
                }
                // the total cpu count is total cpus per node
                if (crs.getTotalCPUCount() > 0) {
                    RangeValueType rangeType = new RangeValueType();
                    rangeType.setLowerBound(Double.NaN);
                    rangeType.setUpperBound(Double.NaN);
                    int nodeCount = crs.getNodeCount();
                    if (nodeCount <= 0) {
                        nodeCount = 1;
                    }
                    rangeType.setExact(crs.getTotalCPUCount() / nodeCount);
                    JSDLUtils.setIndividualCPUCountRequirements(value, rangeType);
                }

                String qName = crs.getQueueName();
                if (!(qName == null || "".equals(qName))) {
                    // ignore "default" queue names
                    if (!(crs.getQueueName().trim().equalsIgnoreCase("default"))) {
                        ResourceRequestDocument rqDoc = ResourceRequestDocument.Factory.newInstance();
                        ResourceRequestType rq = rqDoc.addNewResourceRequest();
                        rq.setName("Queue");
                        rq.setValue(qName);
                        ResourcesType res = JSDLUtils.getOrCreateResources(value);
                        WSUtilities.insertAny(rqDoc, res);
                    }
                }

            } catch (NullPointerException npe) {
                throw new Exception("No value set for resource requirements.", npe);
            }
        }
    }
}
