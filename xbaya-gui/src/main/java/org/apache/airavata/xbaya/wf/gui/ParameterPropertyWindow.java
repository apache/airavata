/*
 * Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ParameterPropertyWindow.java,v 1.6 2008/04/01 21:44:32 echintha Exp $
 */
package org.apache.airavata.xbaya.wf.gui;

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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.graph.ws.WSGraph;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.infoset.XmlElement;

/**
 * @author Satoshi Shirasuna
 */
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
        this.graph = this.engine.getWorkflow().getGraph();
        this.nodes = this.graph.getNodes();
        this.inputNodes = GraphUtil.getInputNodes(this.graph);
        this.outputNodes = GraphUtil.getOutputNodes(this.graph);

        this.inputPanel.setParameterNodes(this.inputNodes);
        this.inputPanel.setMetadata(this.graph.getInputMetadata());
        this.outputPanel.setParameterNodes(this.outputNodes);
        this.outputPanel.setMetadata(this.graph.getOutputMetadata());

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
                this.engine.getErrorWindow().error(warning, e);
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
                this.engine.getErrorWindow().error(warning, e);
                return;
            }
        }

        // Check is done at this point.

        this.graph.setInputMetadata(inputMetadata);
        this.graph.setOutputMetadata(outputMetadata);

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

        this.dialog = new XBayaDialog(this.engine, "Workflow Paremeter Properties", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
