/*
 * Copyright (c) 2004-2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: WorkflowPropertyWindow.java,v 1.9 2009/01/10 06:48:19 cherath Exp $
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

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.airavata.xbaya.util.WSConstants;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlElement;

/**
 * @author Satoshi Shirasuna
 */
public class WorkflowPropertyWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private Workflow workflow;

    private XBayaTextField nameTextField;

    private XBayaTextField templateIDField;

    private XBayaTextField instanceIDField;

    private XBayaTextArea descriptionTextArea;

    private XBayaTextArea metadataTextArea;

    /**
     * @param engine
     */
    public WorkflowPropertyWindow(XBayaEngine engine) {
        this.engine = engine;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getWorkflow();

        String name = this.workflow.getName();
        this.nameTextField.setText(name);

        String description = this.workflow.getDescription();
        this.descriptionTextArea.setText(description);

        URI templateID = this.workflow.getUniqueWorkflowName();
        if (templateID == null) {
            this.templateIDField.setText("");
        } else {
            this.templateIDField.setText(templateID.toString());
        }

        URI instanceID = this.workflow.getGPELInstanceID();
        if (instanceID == null) {
            this.instanceIDField.setText("");
        } else {
            this.instanceIDField.setText(instanceID.toString());
        }

        XmlElement metadata = this.workflow.getMetadata();
        String metadataText;
        if (metadata == null) {
            metadataText = WSConstants.EMPTY_APPINFO;
        } else {
            metadataText = XMLUtil.xmlElementToString(metadata);
        }
        this.metadataTextArea.setText(metadataText);

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    private void setToWorkflow() {
        String name = this.nameTextField.getText();
        if (name != null && name.equals(StringUtil.convertToJavaIdentifier(name))) {
            String description = this.descriptionTextArea.getText();
            String metadataText = this.metadataTextArea.getText();

            XmlElement metadata;
            if (metadataText.length() == 0) {
                metadata = null;
            } else {
                try {
                    metadata = XMLUtil.stringToXmlElement(metadataText);
                } catch (RuntimeException e) {
                    String warning = "The metadata is ill-formed.";
                    this.engine.getErrorWindow().error(warning, e);
                    return;
                }
            }

            GraphCanvas graphCanvas = this.engine.getGUI().getGraphCanvas();
            graphCanvas.setNameAndDescription(name, description);
            graphCanvas.getWorkflow().setMetadata(metadata);
            hide();
        } else {
            this.nameTextField.setText(StringUtil.convertToJavaIdentifier(name));
            JOptionPane.showMessageDialog(this.engine.getGUI().getFrame(),
                    "Invalid Name. Please consider the Name suggsted", "Invalid Name", JOptionPane.OK_OPTION);
        }
    }

    private void initGui() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.templateIDField = new XBayaTextField();
        this.templateIDField.setEditable(false);
        XBayaLabel templateIDLabel = new XBayaLabel("Template ID", this.templateIDField);

        this.instanceIDField = new XBayaTextField();
        this.instanceIDField.setEditable(false);
        XBayaLabel instanceIDLabel = new XBayaLabel("Instance ID", this.instanceIDField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        this.metadataTextArea = new XBayaTextArea();
        XBayaLabel metadataLabel = new XBayaLabel("Metadata", this.metadataTextArea);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(nameLabel);
        mainPanel.add(this.nameTextField);
        mainPanel.add(templateIDLabel);
        mainPanel.add(this.templateIDField);
        mainPanel.add(instanceIDLabel);
        mainPanel.add(this.instanceIDField);
        mainPanel.add(descriptionLabel);
        mainPanel.add(this.descriptionTextArea);
        mainPanel.add(metadataLabel);
        mainPanel.add(this.metadataTextArea);
        mainPanel.layout(new double[] { 0, 0, 0, 0.5, 0.5 }, new double[] { 0, 1 });

        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setToWorkflow();

            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Workflow Properties", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2004-2006 The Trustees of Indiana University. All rights reserved.
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
