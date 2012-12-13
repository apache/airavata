package org.apache.airavata.registry.api.workflow;/*
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

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public interface WorkflowInstanceData {
    public WorkflowInstance getWorkflowInstance() throws ExperimentLazyLoadedException;
    public List<WorkflowInstanceNodeData> getNodeDataList() throws ExperimentLazyLoadedException;
    public void addNodeData(WorkflowInstanceNodeData...nodeData) throws ExperimentLazyLoadedException;
    public WorkflowInstanceNodeData getNodeData(String nodeId) throws ExperimentLazyLoadedException;
    public String getExperimentId();
    public String getWorkflowInstanceId() throws ExperimentLazyLoadedException;
    public String getTemplateName() throws ExperimentLazyLoadedException;
    public WorkflowInstanceStatus.ExecutionStatus getStatus() throws ExperimentLazyLoadedException;
    public Date getStatusUpdateTime() throws ExperimentLazyLoadedException;
    public ExperimentDataImpl getExperimentData() throws ExperimentLazyLoadedException;
    public void setExperimentData(ExperimentDataImpl experimentData) throws ExperimentLazyLoadedException;
    public List<WorkflowInstanceNodePortData> getWorkflowInput (String worklfowInstanceID);
    public List<WorkflowInstanceNodePortData> getWorkflowOutput (String worklfowInstanceID);
}
