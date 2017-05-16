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
package org.apache.airavata.xbaya.interpretor;

import java.net.URI;
import java.util.List;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.jython.lib.ServiceNotifiable;
import org.apache.airavata.xbaya.jython.lib.StandaloneServiceNotificationSender;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.apache.axis2.addressing.EndpointReference;
import org.python.core.PyObject;

public class StandaloneNotificationSender implements WorkflowNotifiable {

    private Workflow workflow;
    private URI workflowID;

    public StandaloneNotificationSender(String topic, Workflow workflow) {
        this.workflow = workflow;
        this.workflowID = URI.create(StringUtil.convertToJavaIdentifier(topic));
    }

    @Override
    public EndpointReference getEventSink() {
        return new EndpointReference(XBayaConstants.DEFAULT_BROKER_URL.toString());
    }

    @Override
    public void workflowStarted(PyObject[] args, String[] keywords) {
        List<InputNode> inputs = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (InputNode inputNode : inputs) {
            inputNode.setState(NodeExecutionState.FINISHED);
        }

    }

    @Override
    public void workflowStarted(Object[] args, String[] keywords) {
        List<InputNode> inputs = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (InputNode inputNode : inputs) {
            inputNode.setState(NodeExecutionState.FINISHED);
        }
    }

    @Override
    public void workflowFinished(Object[] args, String[] keywords) {
        List<OutputNode> outputs = GraphUtil.getOutputNodes(this.workflow.getGraph());
        for (OutputNode outputNode : outputs) {
        	outputNode.setState(NodeExecutionState.EXECUTING);
        }

    }

    @Override
    public void sendingPartialResults(Object[] args, String[] keywords) {
        // noop

    }

    @Override
    public void workflowFinished(PyObject[] args, String[] keywords) {
        List<OutputNode> outputs = GraphUtil.getOutputNodes(this.workflow.getGraph());
        for (OutputNode outputNode : outputs) {
        	outputNode.setState(NodeExecutionState.EXECUTING);
        }

    }

    @Override
    public void workflowTerminated() {
        // noop

    }

    @Override
    public void workflowFailed(String message) {
        // noop

    }

    @Override
    public void workflowFailed(Throwable e) {
        // noop

    }

    @Override
    public void workflowFailed(String message, Throwable e) {
        // noop

    }

    @Override
    public ServiceNotifiable createServiceNotificationSender(String nodeID) {
        return new StandaloneServiceNotificationSender(this.workflow, this.workflowID);
    }

    @Override
    public void cleanup(){

    }

    public String getTopic() {
        return this.workflowID.toASCIIString();
    }
}
