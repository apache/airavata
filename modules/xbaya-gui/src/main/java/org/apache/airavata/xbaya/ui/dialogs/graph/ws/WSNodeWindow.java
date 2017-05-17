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
package org.apache.airavata.xbaya.ui.dialogs.graph.ws;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class WSNodeWindow {

    private XBayaEngine engine;

    private WSNode node;

    private XBayaDialog dialog;

    private XBayaTextField nameTextField;

    private XBayaTextField idTextField;

    private XBayaTextField typeTextField;

    private XBayaTextArea wsdlTextArea;

    /**
     * Constructs a WSNodeWindow.
     * 
     * @param engine
     *            The XBayaEngine
     * @param node
     */
    public WSNodeWindow(XBayaEngine engine, WSNode node) {
        this.engine = engine;
        this.node = node;
        initGUI();
    }

    /**
     *
     */
    public void show() {

//        WsdlDefinitions wsdl = this.node.getComponent().getWSDL();
        String type;
//        if (WSDLUtil.isAWSDL(wsdl)) {
            type = "Abstract WSDL";
//        } else {
//            type = "Concrete WSDL";
//        }

        this.nameTextField.setText(this.node.getName());
        this.idTextField.setText(this.node.getID());
        this.typeTextField.setText(type);
        // wsdl.toStringPretty uses tab, which doesn't look good in the editor
        // pane.
//        this.wsdlTextArea.setText(XMLUtil.BUILDER.serializeToStringPretty(wsdl.xml()));

        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void initGUI() {

        this.nameTextField = new XBayaTextField();
        this.nameTextField.setEditable(false);
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.idTextField = new XBayaTextField();
        this.idTextField.setEditable(false);
        XBayaLabel idLabel = new XBayaLabel("ID", this.idTextField);

        this.typeTextField = new XBayaTextField();
        this.typeTextField.setEditable(false);
        XBayaLabel typeLabel = new XBayaLabel("Type", this.typeTextField);

        this.wsdlTextArea = new XBayaTextArea();
        this.wsdlTextArea.setEditable(false);
        XBayaLabel wsdlLabel = new XBayaLabel("WSDL", this.wsdlTextArea);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(nameLabel);
        infoPanel.add(this.nameTextField);
        infoPanel.add(idLabel);
        infoPanel.add(this.idTextField);
        infoPanel.add(typeLabel);
        infoPanel.add(this.typeTextField);
        infoPanel.add(wsdlLabel);
        infoPanel.add(this.wsdlTextArea);
        infoPanel.layout(4, 2, 3, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), this.node.getName(), infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

}