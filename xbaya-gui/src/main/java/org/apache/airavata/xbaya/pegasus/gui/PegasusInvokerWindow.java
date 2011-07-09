/*
 * Copyright (c) 2010 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.pegasus.gui;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.AxisFault;

import edu.indiana.extreme.pegasuswebservice.PegasusWebserviceStub;
import edu.indiana.extreme.pegasuswebservice.PegasusWebserviceStub.SubmitRefRequest;
import edu.indiana.extreme.pegasuswebservice.PegasusWebserviceStub.SubmitRefResponse;

/**
 * @author Ye Fan
 */
public class PegasusInvokerWindow {

    private XBayaEngine engine;
    private MyProxyChecker myProxyChecker;

    private GridPanel parameterPanel;
    private XBayaTextField topicTextField;
    private XBayaTextField xRegistryTextField;
    private XBayaTextField gfacTextField;
    private XBayaTextField inputFilepathField;
    private JButton invokeButton;
    private XBayaDialog dialog;
    private Workflow workflow;

    /**
     * Constructs a PegasusInvokerWindow.
     */
    public PegasusInvokerWindow(XBayaEngine engine) {
        this.engine = engine;
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();
    }

    protected void initGUI() {
        this.parameterPanel = new GridPanel(true);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification Topic", this.topicTextField);

        this.xRegistryTextField = new XBayaTextField();
        XBayaLabel xRegistryLabel = new XBayaLabel("XRegistry URL", this.xRegistryTextField);

        this.gfacTextField = new XBayaTextField();
        XBayaLabel gfacLabel = new XBayaLabel("GFac URL", this.gfacTextField);

        this.inputFilepathField = new XBayaTextField();
        this.inputFilepathField.setText("/Users/fanye/Documents/fileList");
        XBayaLabel filePathLabel = new XBayaLabel("Input File", this.inputFilepathField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        infoPanel.add(xRegistryLabel);
        infoPanel.add(this.xRegistryTextField);
        infoPanel.add(gfacLabel);
        infoPanel.add(this.gfacTextField);
        infoPanel.add(filePathLabel);
        infoPanel.add(this.inputFilepathField);

        infoPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(this.parameterPanel);
        mainPanel.add(infoPanel);
        mainPanel.layout(2, 1, 0, 0);

        this.invokeButton = new JButton("Invoke");
        this.invokeButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                execute();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.invokeButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Execute Workflow (Pegasus)", mainPanel, buttonPanel);
    }

    /**
     * Call web service to execute Pegasus Workflow
     */
    protected void execute() {
        /* ID of workflow (must be unique) */
        String workflowID = this.engine.getWorkflow().getName();

        try {
            PegasusWebserviceStub stub;
            stub = new PegasusWebserviceStub("http://129.79.49.210:8080/axis2/services/PegasusWebservice/");
            SubmitRefRequest request = new SubmitRefRequest();
            request.setWorkflowID(workflowID);
            request.setTopic(this.topicTextField.getText());
            request.setGFacLocation(this.gfacTextField.getText());
            request.setXRegistryLocation(this.xRegistryTextField.getText());
            // String [] contents = fileToStrings(this.intpuFilePath.getText());
            // request.setInputStrings(contents);
            request.setInputStrings(new String[] { "lol", "lol2" });
            SubmitRefResponse response = stub.submitById(request);
            System.out.println(response.getSubmitRefResponse());
        } catch (AxisFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        hide();
    }

    /**
     * Convert file content into an array of string
     */
    private static String[] fileToStrings(String filePath) {
        try {
            List<String> stringList = new ArrayList<String>();
            File file = new File(filePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = null;
            while (true) {
                line = bufferedReader.readLine();
                if (line != null) {
                    stringList.add(line);
                } else {
                    break;
                }
            }
            bufferedReader.close();
            String[] result = stringList.toArray(new String[1]);

            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * hide the dialog (when user clicked on cancel)
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * restore all the field if workflow has been loaded before
     */
    public void show() {
        this.workflow = this.engine.getWorkflow();

        XBayaConfiguration configuration = this.engine.getConfiguration();
        MonitorConfiguration monitorConfiguration = this.engine.getMonitor().getConfiguration();

        // Topic
        String topic = monitorConfiguration.getTopic();
        if (topic != null) {
            this.topicTextField.setText(topic);
        } else {
            this.topicTextField.setText(UUID.randomUUID().toString());
        }

        // XRegistry
        if (null != configuration.getXRegistryURL()) {
            this.xRegistryTextField.setText(configuration.getXRegistryURL());
        } else {
            this.xRegistryTextField.setText(XBayaConstants.DEFAULT_XREGISTRY_URL);
        }

        // GFac URL
        this.gfacTextField.setText(configuration.getGFacURL());

        this.dialog.show();
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2010 The Trustees of Indiana University. All rights reserved.
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
