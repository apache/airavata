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

package org.apache.airavata.xbaya.ui.dialogs.monitor;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.ws.monitor.EventDataRepository;
import org.apache.airavata.ws.monitor.MonitorUtil;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.xmlpull.infoset.XmlElement;

public class MonitorWindow {

    private XBayaGUI xbayaGUI;

    private XBayaDialog dialog;

    private XBayaTextField timeTextField;

    private XBayaTextField idTextField;

    private XBayaTextField statusTextField;

    private XBayaTextArea messageTextArea;

    /**
     * Constructs a MonitorWindow.
     * 
     * @param engine
     *            The XBayaEngine
     */
    public MonitorWindow(XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        init();
    }

    /**
     * Shows the notification.
     * 
     * @param event
     *            The notification to show
     */
    public void show(XmlElement event) {
        Date timestamp = MonitorUtil.getTimestamp(event);
        if (timestamp != null) {
            this.timeTextField.setText(timestamp.toString());
        } else {
            this.timeTextField.setText("");
        }
        this.idTextField.setText(MonitorUtil.getNodeID(event));
        this.statusTextField.setText(MonitorUtil.getStatus(event));
        // Show the raw XML for now.
        this.messageTextArea.setText(XMLUtil.BUILDER.serializeToStringPretty(event));

        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void init() {
        this.timeTextField = new XBayaTextField();
        this.timeTextField.setEditable(false);
        XBayaLabel timeLabel = new XBayaLabel(EventDataRepository.Column.TIME.getName(), this.timeTextField);

        this.idTextField = new XBayaTextField();
        this.idTextField.setEditable(false);
        XBayaLabel idLabel = new XBayaLabel(EventDataRepository.Column.ID.getName(), this.idTextField);

        this.statusTextField = new XBayaTextField();
        this.statusTextField.setEditable(false);
        XBayaLabel statusLabel = new XBayaLabel(EventDataRepository.Column.STATUS.getName(), this.statusTextField);

        this.messageTextArea = new XBayaTextArea();
        this.messageTextArea.setSize(500, 500);
        this.messageTextArea.setEditable(false);
        XBayaLabel messageLabel = new XBayaLabel(EventDataRepository.Column.MESSAGE.getName(), this.messageTextArea);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(timeLabel);
        infoPanel.add(this.timeTextField);
        infoPanel.add(idLabel);
        infoPanel.add(this.idTextField);
        infoPanel.add(statusLabel);
        infoPanel.add(this.statusTextField);
        infoPanel.add(messageLabel);
        infoPanel.add(this.messageTextArea);
        infoPanel.layout(4, 2, 3, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        this.dialog = new XBayaDialog(this.xbayaGUI, "Notification", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}