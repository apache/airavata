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

package org.apache.airavata.registry.api.workflow;

import java.util.List;

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.impl.WorkflowInstanceDataImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public interface ExperimentData extends WorkflowInstanceData {
    public String getId();
	public String getTopic();
	public String getUser();
	public String getMetadata();
	public String getExperimentName();
	public List<WorkflowInstanceDataImpl> getWorkflowInstanceData() throws ExperimentLazyLoadedException;
    public WorkflowInstanceData getWorkflowInstance(String workflowInstanceID) throws ExperimentLazyLoadedException;

    public void setExperimentId(String experimentId);
	public void setTopic(String topic);
	public void setUser(String user);
	public void setMetadata(String metadata);
	public void setExperimentName(String experimentName);
    
	public String getExperimentId();
}
