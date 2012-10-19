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

package org.apache.airavata.wsmg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.airavata.wsmg.client.ConsumerServer;
import org.apache.airavata.wsmg.client.WsntMsgBrokerClient;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NotificationViewerFrame extends JFrame {

    private static final long serialVersionUID = -4924241323165353343L;

    private static final Log logger = LogFactory.getLog(NotificationViewerFrame.class);

    JPanel contentPane;

    BorderLayout borderLayout1 = new BorderLayout();

    JPanel jPanel1 = new JPanel();

    Border border1 = BorderFactory.createLineBorder(Color.gray, 2);

    JLabel jLabel1 = new JLabel();

    JButton jButtonStart = new JButton();

    JLabel jLabel2 = new JLabel();

    JTextField jTextBrokerUrl = new JTextField();

    JLabel jLabel3 = new JLabel();

    JTextField jTextPort = new JTextField();

    JLabel jLabel4 = new JLabel();

    JTextField jTextTopic = new JTextField();

    JTabbedPane jTabbedPane1 = new JTabbedPane();

    JScrollPane jScrollPane1 = new JScrollPane();

    JTextArea jTextAreaBrief = new JTextArea();

    JScrollPane jScrollPane2 = new JScrollPane();

    JTextArea jTextAreaWhole = new JTextArea();

    JButton jButtonClear = new JButton();

    ConsumerServer consumerServer = null;

    WsntMsgBrokerClient client;

    String subId = null;

    JButton jButtonStop = new JButton();

    GridBagLayout gridBagLayout1 = new GridBagLayout();

    public NotificationViewerFrame() {
        try {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        client = new WsntMsgBrokerClient();

    }

    /**
     * Component initialization.
     * 
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(borderLayout1);
        setSize(new Dimension(547, 429));
        setTitle("WS-Notification Viewer");
        jPanel1.setBackground(Color.white);
        jPanel1.setBorder(border1);
        jPanel1.setMaximumSize(new Dimension(180000, 180000));
        jPanel1.setLayout(gridBagLayout1);
        jLabel1.setFont(new java.awt.Font("Serif", Font.BOLD | Font.ITALIC, 28));
        jLabel1.setForeground(Color.blue);
        jLabel1.setText("WS-Notification Listener");
        jButtonStart.setToolTipText("Subscribe to the topic and start listening");
        jButtonStart.setText("Start");
        jButtonStart.addActionListener(new NotificationViewerFrame_jButton1_actionAdapter(this));
        jLabel2.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        jLabel2.setText("BrokerURL");
        jTextBrokerUrl.setToolTipText("Enter Broker URL here.");
        jTextBrokerUrl.setText("http://localhost:8080/axis2/services/NotificationService");
        jLabel3.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        jLabel3.setText("Listening Port");
        jTextPort.setToolTipText("Enter the port this listener will be listening to.");
        jTextPort.setText("19999");
        jLabel4.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        jLabel4.setText("Topic");
        jTextTopic.setToolTipText("Enter the topic to subscribe.");
        jTextTopic.setText("topic");
        jTextAreaBrief.setToolTipText("Summary of the messages received.");
        jTextAreaBrief.setLineWrap(true);
        // jTextAreaWhole.setMinimumSize(new Dimension(490, 17));
        // jTextAreaWhole.setPreferredSize(new Dimension(490, 17));
        jTextAreaWhole.setToolTipText("Full content of the SOAP messages.");
        // jTextAreaWhole.setText("");
        jButtonClear.setToolTipText("Clear messages in the two message panes.");
        jButtonClear.setText("Clear Messages");
        jButtonClear.addActionListener(new NotificationViewerFrame_jButtonClear_actionAdapter(this));
        jButtonStop.setEnabled(false);
        jButtonStop.setToolTipText("Unsubscribe to the topic and stop listening.");
        jButtonStop.setText("Stop");
        jButtonStop.addActionListener(new NotificationViewerFrame_jButtonStop_actionAdapter(this));
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setToolTipText("Summary of the messages received.");
        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setAutoscrolls(true);
        jScrollPane2.setToolTipText("Full content of the SOAP messages.");
        jTabbedPane1.add(jScrollPane1, "Brief Messages");
        jTabbedPane1.add(jScrollPane2, "Whole Messages");
        jPanel1.add(jLabel1, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(7, 10, 0, 107), 20, -5));
        jPanel1.add(jButtonStop, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 48, 0, 80), 55, -4));
        jPanel1.add(jLabel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 36, 0, 8), 43, 4));
        jPanel1.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 36, 0, 0), 1, 4));
        jPanel1.add(jTextPort, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(7, 0, 0, 0), 144, -1));
        jPanel1.add(jButtonClear, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 46, 0, 78), 3, -4));
        jPanel1.add(jTextTopic, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.SOUTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 149, -1));
        jPanel1.add(jTextBrokerUrl, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(6, -1, 5, 1), 10, -1));
        jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(6, 37, 5, 0), 21, 4));
        jPanel1.add(jButtonStart, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(8, 47, 0, 79), 57, -4));
        jScrollPane2.getViewport().add(jTextAreaWhole);
        jScrollPane1.getViewport().add(jTextAreaBrief);

        jPanel1.add(jTabbedPane1, new GridBagConstraints(0, 4, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 18, 57, 30), 0, 206));
        contentPane.add(jPanel1, java.awt.BorderLayout.CENTER);
    }

    public void jButtonStart_actionPerformed(ActionEvent event) {
        // jLabel1.setForeground(new Color(255,0,0));
        String brokerUrl = jTextBrokerUrl.getText().trim();
        String topic = jTextTopic.getText();

        String listeningPort = jTextPort.getText();

        int port = -1;

        try {
            port = Integer.parseInt(listeningPort);
        } catch (NumberFormatException nfe) {

            JOptionPane.showMessageDialog(this, nfe, "invalid port specified", JOptionPane.ERROR_MESSAGE);
            return;
        }

        client.init(brokerUrl);// important
        // Create a handler to handle the notifications arrived
        WsntViewerConsumerNotificationHandler handler = new WsntViewerConsumerNotificationHandler(this);

        String consumerUrl = null;

        try {
            String[] eprs = client.startConsumerService(port, handler);

            if (eprs.length > 0) {
                consumerUrl = eprs[0];
            } else {
                throw new AxisFault("no consumer url given by wse client api");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e, "Unable to start consumer service", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            subId = client.subscribe(consumerUrl, topic, null);
        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, e, "Unable to subscribe to topic", JOptionPane.ERROR_MESSAGE);

            client.shutdownConsumerService();
            return;
        }

        jButtonStart.setEnabled(false);
        jTextBrokerUrl.setEnabled(false);
        jTextTopic.setEnabled(false);
        jTextPort.setEnabled(false);
        jButtonStop.setEnabled(true);
        jTextAreaBrief.append("Listener started...\n");
        jTextAreaWhole.append("Listener started...\n");

    }

    public void jButtonStop_actionPerformed(ActionEvent e) {
        if (subId != null) {
            try {
                client.init(jTextBrokerUrl.getText().trim());
                client.unSubscribe(subId);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, e, "Unable to unsubscribe from topic", JOptionPane.ERROR_MESSAGE);
            }// TODO: add with replyTo URL
            subId = null;
        }

        client.shutdownConsumerService();

        jButtonStop.setEnabled(false);
        jButtonStart.setEnabled(true);
        jTextBrokerUrl.setEnabled(true);
        jTextTopic.setEnabled(true);
        jTextPort.setEnabled(true);
        jTextAreaBrief.append("Listener stoped.\n");
        jTextAreaWhole.append("Listener stoped.\n");

    }

    public void addBriefMessage(String message) {
        jTextAreaBrief.append(message + "\n");
        jTextAreaBrief.selectAll();
    }

    public void addWholeMessage(String message) {
        jTextAreaWhole.append(message + "\n");
        jTextAreaWhole.selectAll();
    }

    public void jButtonClear_actionPerformed(ActionEvent e) {
        jTextAreaBrief.setText("");
        jTextAreaWhole.setText("");
    }
}

class NotificationViewerFrame_jButtonStop_actionAdapter implements ActionListener {
    private NotificationViewerFrame adaptee;

    NotificationViewerFrame_jButtonStop_actionAdapter(NotificationViewerFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonStop_actionPerformed(e);
    }
}

class NotificationViewerFrame_jButtonClear_actionAdapter implements ActionListener {
    private NotificationViewerFrame adaptee;

    NotificationViewerFrame_jButtonClear_actionAdapter(NotificationViewerFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonClear_actionPerformed(e);
    }
}

class NotificationViewerFrame_jButton1_actionAdapter implements ActionListener {
    private NotificationViewerFrame adaptee;

    NotificationViewerFrame_jButton1_actionAdapter(NotificationViewerFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonStart_actionPerformed(e);

    }
}
