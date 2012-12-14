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
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public interface ExperimentData extends WorkflowExecutionData {
    //Current Id and Topic values are similar

    /**
     * Returns the ExeperimentID of the workflow Run
     * @return
     */
    public String getId();

    /**
     * Returns the Experiment Topic... Currently the ID and the Topic are identical
     * @return
     */
	public String getTopic();

    /**
     * Returns the user of the workflow run
     * @return
     */
	public String getUser();

    /**
     * Returns metadata related to the workflow run
     * @return
     */
	public String getMetadata();

    /**
     * Returns the Experiment Name of the workflow run, This is given in the XBaya-GUI when you user run a workflow
     * @return
     */
	public String getExperimentName();

    /**
     * get data related to a particular experiment, this returns all the workflow runs for the given Experiment
     * @return
     * @throws ExperimentLazyLoadedException
     */
	public List<WorkflowExecutionDataImpl> getWorkflowExecutionDataList() throws ExperimentLazyLoadedException;

    /**
     * Reut
     * @param workflowExecutionID
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public WorkflowExecutionData getWorkflowExecutionData(String workflowExecutionID) throws ExperimentLazyLoadedException;

    /**
     *
     * @param experimentId
     */
    public void setExperimentId(String experimentId);

    /**
     *
     * @param topic
     */
	public void setTopic(String topic);

    /**
     *
     * @param user
     */
	public void setUser(String user);

    /**
     *
     * @param metadata
     */
	public void setMetadata(String metadata);

    /**
     *
     * @param experimentName
     */
	public void setExperimentName(String experimentName);

    /**
     *
     * @return
     */
	public String getExperimentId();
}
