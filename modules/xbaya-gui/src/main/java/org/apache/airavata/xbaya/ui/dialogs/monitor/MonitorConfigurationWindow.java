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

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class MonitorConfigurationWindow {

    private XBayaEngine engine;

//    private MonitorConfiguration configuration;

    private XBayaDialog dialog;

    private XBayaTextField brokerTextField;

    private XBayaTextField topicTextField;

    private JCheckBox pullCheckBox;

    private XBayaTextField messageBoxTextField;

    /**
     * @param engine
     * 
     */
    public MonitorConfigurationWindow(XBayaEngine engine) {
        this.engine = engine;
//        this.configuration = engine.getMonitor().getConfiguration();
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
//        this.brokerTextField.setText(this.configuration.getBrokerURL());
//        this.topicTextField.setText(this.configuration.getTopic());
//        this.pullCheckBox.setSelected(this.configuration.isPullMode());
//        this.messageBoxTextField.setText(this.configuration.getMessageBoxURL());

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    private void setConfiguration() {
        String broker = this.brokerTextField.getText();
        String topic = this.topicTextField.getText();
        String messageBox = this.messageBoxTextField.getText();
        boolean pull = this.pullCheckBox.isSelected();

        if (broker.length() == 0) {
            this.engine.getGUI().getErrorWindow().error("Broker URL cannot be empty");
            return;
        }
        URI brokerURL;
        try {
            brokerURL = new URI(broker).parseServerAuthority();
        } catch (URISyntaxException e) {
            String message = "Broker URL is in a wrong format";
            this.engine.getGUI().getErrorWindow().error(message, e);
            return;
        }

        if (topic.length() == 0) {
            String message = "Topic cannot be empty";
            this.engine.getGUI().getErrorWindow().error(message);
            return;
        }

        URI messageBoxURL = null;
        if (pull) {
            if (messageBox.length() == 0) {
                this.engine.getGUI().getErrorWindow().error("Message box URL cannot be empty");
                return;
            }
            try {
                messageBoxURL = new URI(messageBox).parseServerAuthority();
            } catch (URISyntaxException e) {
                String message = "Message box URL is in a wrong format";
                this.engine.getGUI().getErrorWindow().error(message, e);
                return;
            }
        } else {
//            messageBoxURL = this.configuration.getMessageBoxURL();
        }

//        this.configuration.set(brokerURL, topic, pull, messageBoxURL);
        this.engine.getConfiguration().setMessageBoxURL(messageBoxURL);
        this.engine.getConfiguration().setBrokerURL(brokerURL);
        this.engine.getConfiguration().setTopic(topic);
        hide();
    }

    private void initGui() {

        this.brokerTextField = new XBayaTextField();
        XBayaLabel brokerLabel = new XBayaLabel("Broker URL", this.brokerTextField);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Topic", this.topicTextField);

        this.pullCheckBox = new JCheckBox("Pull Mode");
        JLabel dummyLabel = new JLabel();

        this.messageBoxTextField = new XBayaTextField();
        XBayaLabel msgBoxLabel = new XBayaLabel("Message Box URL", this.messageBoxTextField);

        this.messageBoxTextField.setEnabled(false);
        this.pullCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                int stateChange = event.getStateChange();
                if (stateChange == ItemEvent.SELECTED) {
                    MonitorConfigurationWindow.this.messageBoxTextField.setEnabled(true);
                } else if (stateChange == ItemEvent.DESELECTED) {
                    MonitorConfigurationWindow.this.messageBoxTextField.setEnabled(false);
                }
            }
        });

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(brokerLabel);
        infoPanel.add(this.brokerTextField);
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        infoPanel.add(dummyLabel);
        infoPanel.add(this.pullCheckBox);
        infoPanel.add(msgBoxLabel);
        infoPanel.add(this.messageBoxTextField);
        infoPanel.layout(4, 2, SwingUtil.WEIGHT_NONE, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setConfiguration();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Notification Configuration", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}