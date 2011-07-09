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

package org.apache.airavata.xbaya.graph.dynamic.gui;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.dynamic.CombineMultipleStreamComponent;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.dynamic.CombineMultipleStreamNode;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.streaming.StreamServiceStub;
import org.apache.airavata.xbaya.streaming.StreamServiceStub.Property;
import org.apache.airavata.xbaya.streaming.StreamServiceStub.StaticInput;

public class CombineMultipleStreamWindow {

    private XBayaEngine engine;

    private CombineMultipleStreamNode node;

    private XBayaDialog dialog;

    private JCheckBox checkBox;

    private LinkedList<String> streams = new LinkedList<String>();

    private XBayaTextField outputStreamName;

    private XBayaTextField rootElementName;

    public CombineMultipleStreamWindow(XBayaEngine engine, CombineMultipleStreamNode node) {
        this.engine = engine;
        this.node = node;

        List<DataPort> inputPorts = node.getInputPorts();
        for (DataPort dataPort : inputPorts) {
            streams.add(dataPort.getFromPort().getName());

        }

        initGUI();

    }

    /**
     * Shows the notification.
     * 
     * @param event
     *            The notification to show
     */
    public void show() {

        if (!this.node.isAllInPortsConnected()) {
            JOptionPane.showMessageDialog(this.engine.getGUI().getFrame(), "Cannot parse unbounded inputs");
        } else {

            this.dialog.show();
        }

    }

    private void hide() {
        this.dialog.hide();
    }

    private void initGUI() {

        GridPanel infoPanel = new GridPanel();
        XBayaTextField streamTxt = new XBayaTextField();
        streamTxt.setText(streams.toString());
        streamTxt.setEditable(false);

        outputStreamName = new XBayaTextField();
        XBayaLabel outputStreamNameLabel = new XBayaLabel("Output Stream Name", this.outputStreamName);

        rootElementName = new XBayaTextField();
        XBayaLabel rootElementNameLabel = new XBayaLabel("Root element", this.rootElementName);

        checkBox = new JCheckBox();

        infoPanel.add(new XBayaLabel("Event Streams", streamTxt));
        infoPanel.add(streamTxt);
        infoPanel.add(outputStreamNameLabel);
        infoPanel.add(this.outputStreamName);
        infoPanel.add(rootElementNameLabel);
        infoPanel.add(this.rootElementName);

        infoPanel.add(new XBayaLabel("Hot deploy during composition", checkBox));
        infoPanel.add(checkBox);

        infoPanel.layout(3, 2, 0, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                ok();
            }

        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        this.dialog = new XBayaDialog(this.engine, this.node.getName(), infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void ok() {

        String newStreamName = outputStreamName.getText();
        if (null != newStreamName && !"".equals(newStreamName)) {
            this.node.setStreamName(newStreamName);
            this.node.setName(CombineMultipleStreamComponent.NAME + "_" + newStreamName);

            try {
                if (this.checkBox.isSelected()) {
                    StreamServiceStub stub = new StreamServiceStub(XBayaConstants.STREAM_SERVER);
                    for (String stream : this.streams) {
                        StaticInput[] staticInput = new StaticInput[0];
                        Property[] properties = new Property[0];
                        stub.registerEPLWithInsert("INSERT INTO " + newStreamName + " SELECT * FROM " + stream,
                                this.rootElementName.getText(), this.rootElementName.getText(), properties, "", "", "",
                                this.rootElementName.getText(), staticInput);
                    }
                }

            } catch (Exception e) {
                this.engine.getErrorWindow().error(e);
            }
            node.getOutputPort(0).setName(newStreamName);
            node.setStreamName(newStreamName);
        }

        hide();
        this.engine.getGUI().getGraphCanvas().repaint();
    }

}