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
package org.apache.airavata.xbaya.ui.dialogs.workflow;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.views.ParameterPropertyPanel;
import org.xmlpull.infoset.XmlElement;

public class ParameterPropertyWindow {

    private XBayaEngine engine;

    protected XBayaDialog dialog;

    private ParameterPropertyPanel inputPanel;

    private ParameterPropertyPanel outputPanel;

    private JButton okButton;

    private WSGraph graph;

    private List<? extends Node> nodes;

    private List<InputNode> inputNodes;

    private List<OutputNode> outputNodes;

    /**
     * @param engine
     */
    public ParameterPropertyWindow(XBayaEngine engine) {
        this.engine = engine;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.graph = this.engine.getGUI().getWorkflow().getGraph();
        this.nodes = this.graph.getNodes();
        this.inputNodes = GraphUtil.getInputNodes(this.graph);
        this.outputNodes = GraphUtil.getOutputNodes(this.graph);

        this.inputPanel.setParameterNodes(this.inputNodes);
//        this.inputPanel.setMetadata(this.graph.getInputMetadata());
        this.outputPanel.setParameterNodes(this.outputNodes);
//        this.outputPanel.setMetadata(this.graph.getOutputMetadata());

        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void ok() {
        String inputMetadataText = this.inputPanel.getMetadata();
        XmlElement inputMetadata;
        if (inputMetadataText.length() == 0) {
            inputMetadata = null;
        } else {
            try {
                inputMetadata = XMLUtil.stringToXmlElement(inputMetadataText);
            } catch (RuntimeException e) {
                String warning = "The input metadata is ill-formed.";
                this.engine.getGUI().getErrorWindow().error(warning, e);
                return;
            }
        }

        String outputMetadataText = this.outputPanel.getMetadata();
        XmlElement outputMetadata;
        if (outputMetadataText.length() == 0) {
            outputMetadata = null;
        } else {
            try {
                outputMetadata = XMLUtil.stringToXmlElement(outputMetadataText);
            } catch (RuntimeException e) {
                String warning = "The output metadata is ill-formed.";
                this.engine.getGUI().getErrorWindow().error(warning, e);
                return;
            }
        }

        // Check is done at this point.

//        this.graph.setInputMetadata(inputMetadata);
//        this.graph.setOutputMetadata(outputMetadata);

        // Sort the nodes in the graph in the order of sorted inputs, sorted
        // outputs, and the rest.
        for (int i = 0; i < this.inputNodes.size(); i++) {
            InputNode inputNode = this.inputNodes.get(i);
            Collections.swap(this.nodes, i, this.nodes.indexOf(inputNode));
        }
        for (int i = 0; i < this.outputNodes.size(); i++) {
            OutputNode outputNode = this.outputNodes.get(i);
            Collections.swap(this.nodes, this.inputNodes.size() + i, this.nodes.indexOf(outputNode));
        }
        hide();
    }

    private void initGui() {
        this.inputPanel = new ParameterPropertyPanel("Inputs");
        this.outputPanel = new ParameterPropertyPanel("Outputs");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2)); // To have the same size.
        mainPanel.add(this.inputPanel.getSwingComponent());
        mainPanel.add(this.outputPanel.getSwingComponent());

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.setDefaultCapable(true);
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(this.okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Workflow Paremeter Properties", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}