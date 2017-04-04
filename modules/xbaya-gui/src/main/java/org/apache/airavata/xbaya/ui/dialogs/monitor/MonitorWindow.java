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
package org.apache.airavata.xbaya.ui.dialogs.monitor;

import org.apache.airavata.common.utils.BrowserLauncher;
import org.apache.airavata.xbaya.messaging.EventData;
import org.apache.airavata.xbaya.messaging.EventDataRepository;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import xsul5.XmlConstants;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Date;

public class MonitorWindow {

    private XBayaGUI xbayaGUI;

    private XBayaDialog dialog;

    private XBayaTextField timeTextField;

    private XBayaTextField idTextField;

    private XBayaTextField statusTextField;

    private JEditorPane messageTextArea;
    private String messageText;
    

    /**
     * Constructs a MonitorWindow.
     * 
     * @param xbayaGUI The XBayaEngine
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
    public void show(EventData event) {
        Date timestamp = event.getUpdateTime();
        if (timestamp != null) {
            this.timeTextField.setText(timestamp.toString());
        } else {
            this.timeTextField.setText("");
        }
        this.idTextField.setText(event.getMessageId());
        this.statusTextField.setText(event.getStatus());
        
        // Show the raw XML for now.
        messageText = event.getMessage();
		this.messageTextArea.setText(messageText);

        this.dialog.show();
        this.dialog.getDialog().setSize(600, 800);
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

        this.messageTextArea = new JEditorPane(XmlConstants.CONTENT_TYPE_HTML, "");
        this.messageTextArea.setSize(500, 500);
        this.messageTextArea.setEditable(false);
        messageTextArea.setBackground(Color.WHITE);
        messageTextArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == EventType.ACTIVATED) {
                    URL url = event.getURL();
                    try {
                        BrowserLauncher.openURL(url.toString());
                    } catch (Exception e) {
                        MonitorWindow.this.xbayaGUI.getErrorWindow().error(MonitorWindow.this.dialog.getDialog(),
                                e.getMessage(), e);
                    }
                }
            }
        });
        JScrollPane pane = new JScrollPane(messageTextArea);
        pane.setSize(500, 500);
        XBayaLabel messageLabel = new XBayaLabel(EventDataRepository.Column.MESSAGE.getName(), pane);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(timeLabel);
        infoPanel.add(this.timeTextField);
        infoPanel.add(idLabel);
        infoPanel.add(this.idTextField);
        infoPanel.add(statusLabel);
        infoPanel.add(this.statusTextField);
        infoPanel.add(messageLabel);
        infoPanel.add(pane);
        infoPanel.layout(4, 2, 3, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(messageText), null);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(copyButton);

        this.dialog = new XBayaDialog(this.xbayaGUI, "Notification", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}