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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.dynamic.CepComponent;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.dynamic.CepNode;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.streaming.StreamServiceStub;
import org.apache.airavata.xbaya.streaming.StreamServiceStub.Property;

public class CepNodeWindow {

    private XBayaEngine engine;

    private CepNode node;

    private XBayaDialog dialog;

    private XBayaTextField eplStatement;

    private JCheckBox checkBox;

    private String streams;

    private XBayaTextField outputStreamName;

    private XBayaTextField rootElementName;

    private XBayaTextField xpathsName;

    private XBayaTextField xpaths;

    private XBayaTextField properties;

    private XBayaTextField types;

    /**
     * Constructs a WSNodeWindow.
     * 
     * @param engine
     *            The XBayaEngine
     * @param node
     */
    public CepNodeWindow(XBayaEngine engine, CepNode node) {
        this.engine = engine;
        this.node = node;

        List<DataPort> inputPorts = node.getInputPorts();
        boolean first = true;
        streams = "";
        for (DataPort dataPort : inputPorts) {
            if (first) {
                streams += dataPort.getFromPort().getName();
                first = false;
            } else {
                streams += "," + dataPort.getFromPort().getName();
            }

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

            if (this.node.getQuery() == null) {
                this.node.setQuery("SELECT * FROM " + streams);
            }
            this.eplStatement.setText(this.node.getQuery());

            this.dialog.show();
        }

    }

    private void hide() {
        this.dialog.hide();
    }

    private void initGUI() {

        GridPanel infoPanel = new GridPanel();
        this.eplStatement = new XBayaTextField();
        XBayaLabel operationLabel = new XBayaLabel("EPL Statement", this.eplStatement);
        XBayaTextField streamTxt = new XBayaTextField();
        streamTxt.setText(streams);
        streamTxt.setEditable(false);

        outputStreamName = new XBayaTextField();
        XBayaLabel outputStreamNameLabel = new XBayaLabel("Output Stream Name", this.outputStreamName);

        rootElementName = new XBayaTextField();
        XBayaLabel rootElementNameLabel = new XBayaLabel("Root element", this.rootElementName);

        xpaths = new XBayaTextField();
        XBayaLabel xpathsLabel = new XBayaLabel("Comma seperated xpath expressions", this.xpaths);

        properties = new XBayaTextField();
        XBayaLabel propertiesLabel = new XBayaLabel("Comma seperated xpath property name", this.properties);

        types = new XBayaTextField();
        XBayaLabel typesLabel = new XBayaLabel("Comma seperated property Data Type", this.types);

        checkBox = new JCheckBox();

        infoPanel.add(new XBayaLabel("Event Streams", streamTxt));
        infoPanel.add(streamTxt);
        infoPanel.add(operationLabel);
        infoPanel.add(this.eplStatement);
        infoPanel.add(outputStreamNameLabel);
        infoPanel.add(this.outputStreamName);
        infoPanel.add(rootElementNameLabel);
        infoPanel.add(this.rootElementName);
        infoPanel.add(xpathsLabel);
        infoPanel.add(this.xpaths);
        infoPanel.add(propertiesLabel);
        infoPanel.add(this.properties);
        infoPanel.add(typesLabel);
        infoPanel.add(this.types);

        infoPanel.add(new XBayaLabel("Hot deploy during composition", checkBox));
        infoPanel.add(checkBox);

        infoPanel.layout(8, 2, 0, 0);

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

        String eplWIthoutInsert = eplStatement.getText();
        String newStreamName = outputStreamName.getText();
        this.node.setName(CepComponent.NAME + "_" + newStreamName);
        String query = "INSERT INTO " + newStreamName + " " + eplWIthoutInsert;
        this.node.setQuery(query);

        String[] xpathSplits = xpaths.getText().trim().split(",");
        String[] propertySplits = properties.getText().trim().split(",");
        String[] typeSplit = types.getText().trim().split(",");

        try {
            if (xpathSplits.length == propertySplits.length && propertySplits.length == typeSplit.length) {
                Property[] xpathProperties = new Property[xpathSplits.length];
                for (int i = 0; i < xpathSplits.length; ++i) {
                    Property prop = new Property();
                    prop.setXpath(xpathSplits[i]);
                    prop.setProprtyName(propertySplits[i]);
                    prop.setType(typeSplit[i]);

                    xpathProperties[i] = prop;
                }
                if (this.checkBox.isSelected()) {
                    StreamServiceStub stub = new StreamServiceStub(XBayaConstants.STREAM_SERVER);

                    stub.registerEPLWithInsert(query, this.rootElementName.getText(), this.rootElementName.getText(),
                            xpathProperties, "epr", "workflow", "topic", this.rootElementName.getText(), null);
                }
                node.getOutputPort(0).setName(newStreamName);
                node.setStreamName(newStreamName);
                node.setQuery(this.eplStatement.getText());

            } else {
                throw new XBayaException("length mismatch in xpaths, property names and types");
            }
        } catch (Exception e) {

        }

        hide();
        this.engine.getGUI().getGraphCanvas().repaint();
    }
}