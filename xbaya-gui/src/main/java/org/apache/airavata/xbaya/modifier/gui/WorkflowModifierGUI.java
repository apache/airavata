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

package org.apache.airavata.xbaya.modifier.gui;

import java.net.URI;
import java.util.List;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.gui.GPELInvoker;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.modifier.WorkflowModifier;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorEventData;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;

public class WorkflowModifierGUI {

    private XBayaEngine engine;

    /**
     * Constructs a WorkflowModifierGUI.
     * 
     * @param engine
     */
    public WorkflowModifierGUI(XBayaEngine engine) {
        this.engine = engine;
    }

    /**
     * 
     */
    public void createDifference() {
        try {
            Workflow workflow = this.engine.getWorkflow();
            MonitorEventData eventData = this.engine.getMonitor().getEventData();
            WorkflowModifier modifier = new WorkflowModifier(workflow, eventData);
            Workflow diffWorkflow = modifier.createDifference();
            GraphCanvas canvas = this.engine.getGUI().newGraphCanvas(true);
            canvas.setWorkflow(diffWorkflow);
        } catch (MonitorException e) {
            this.engine.getErrorWindow().error(e);
        } catch (GraphException e) {
            // This should not happen.
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (RuntimeException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }
    }

    /**
     * 
     */
    public void invokeModifiedWorkflow() {

        // TODO error handling

        Workflow workflow = this.engine.getWorkflow();
        Workflow diffWorkflow;
        try {
            MonitorEventData eventData = this.engine.getMonitor().getEventData();
            WorkflowModifier modifier = new WorkflowModifier(workflow, eventData);
            diffWorkflow = modifier.createDifference();
            // open the diff in a new tab, but do not focus it.
            GraphCanvas canvas = this.engine.getGUI().newGraphCanvas(false);
            canvas.setWorkflow(diffWorkflow);
        } catch (MonitorException e) {
            this.engine.getErrorWindow().error(e);
            return;
        } catch (GraphException e) {
            // This should not happen.
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            return;
        } catch (RuntimeException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            return;
        }

        // Change the topic so that messages won't be mixed up with ones for the
        // original workflow.
        MonitorConfiguration monitorConfiguration = this.engine.getMonitor().getConfiguration();
        String newTopic = monitorConfiguration.getTopic() + "-diff";
        monitorConfiguration.setTopic(newTopic);

        // Invoke the workflow without showing the dialog.
        GPELInvoker invoker = new GPELInvoker(this.engine);

        try {
            WorkflowClient.createScript(diffWorkflow);
        } catch (GraphException e) {
            this.engine.getErrorWindow().error(e); // TODO
            return;
        }

        // Create a GUI without depending on the graph.

        // Set the default as an input.
        List<WSComponentPort> inputs;
        try {
            inputs = diffWorkflow.getInputs();
        } catch (ComponentException e) {
            // This should not happen when we create WSDL here, but if we use
            // precompiled workflow, it might happen.
            this.engine.getErrorWindow().error(ErrorMessages.WORKFLOW_WSDL_ERROR, e);
            return;
        }
        for (WSComponentPort input : inputs) {
            String defaultValue = input.getDefaultValue();
            input.setValue(defaultValue);
        }

        invoker.invoke(diffWorkflow, inputs, true);

        // Change the ID of current display so that we can keep monitoring.
        URI instanceID = diffWorkflow.getGPELInstanceID();
        workflow.setGPELInstanceID(instanceID);
    }
}