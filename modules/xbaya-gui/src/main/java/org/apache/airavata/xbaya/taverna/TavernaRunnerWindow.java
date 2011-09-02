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

package org.apache.airavata.xbaya.taverna;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.scufl.script.ScuflScript;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.XmlConstants;
import xsul5.MLogger;

public class TavernaRunnerWindow {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Workflow workflow;

    private XBayaDialog dialog;

    private GridPanel parameterPanel;

    private XBayaTextField topicTextField;

    private List<XBayaTextField> parameterTextFields = new ArrayList<XBayaTextField>();

    private ScuflScript script;

    protected final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    /**
     * Constructs a TavernaRunnerWindow.
     * 
     * @param engine
     * 
     */
    public TavernaRunnerWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getWorkflow();

        this.script = new ScuflScript(this.workflow, this.engine.getConfiguration());

        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        if (notifConfig.getBrokerURL() == null) {
            this.engine.getErrorWindow().error(ErrorMessages.BROKER_URL_NOT_SET_ERROR);
            return;
        }

        // Check if the workflow is valid before the user types in input
        // values.
        JythonScript jythonScript = new JythonScript(this.workflow, this.engine.getConfiguration());
        ArrayList<String> warnings = new ArrayList<String>();
        if (!jythonScript.validate(warnings)) {
            StringBuilder buf = new StringBuilder();
            for (String warning : warnings) {
                buf.append("- ");
                buf.append(warning);
                buf.append("\n");
            }
            this.engine.getErrorWindow().warning(buf.toString());
            return;
        }

        // Create a script here. It might throw some exception because the
        // validation is not perfect.
        try {
            this.script.create();
        } catch (GraphException e) {
            this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
            hide();
            return;
        } catch (RuntimeException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        }

        // Create input fields
        Collection<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (InputNode node : inputNodes) {
            String id = node.getID();
            QName parameterType = node.getParameterType();
            JLabel nameLabel = new JLabel(id);
            JLabel typeField = new JLabel(parameterType.getLocalPart());
            XBayaTextField paramField = new XBayaTextField();
            Object value = node.getDefaultValue();

            String valueString;
            if (value == null) {
                valueString = "";
            } else {
                if (value instanceof XmlElement) {
                    XmlElement valueElement = (XmlElement) value;
                    valueString = XMLUtil.xmlElementToString(valueElement);
                } else {
                    // Only string comes here for now.
                    valueString = value.toString();
                }
            }
            paramField.setText(valueString);
            this.parameterPanel.add(nameLabel);
            this.parameterPanel.add(typeField);
            this.parameterPanel.add(paramField);
            this.parameterTextFields.add(paramField);
        }
        this.parameterPanel.layout(inputNodes.size(), 3, GridPanel.WEIGHT_NONE, 2);

        this.topicTextField.setText(notifConfig.getTopic());

        this.engine.getConfiguration();

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        this.dialog.hide();

        this.parameterPanel.getContentPanel().removeAll();
        this.parameterTextFields.clear();
    }

    private void initGUI() {
        this.parameterPanel = new GridPanel(true);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification topic", this.topicTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        infoPanel.layout(1, 1, 0, 0);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(this.parameterPanel);
        mainPanel.add(infoPanel);
        mainPanel.layout(2, 1, 0, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
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
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Invoke  workflow", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void execute() {
        final List<String> arguments = new ArrayList<String>();

        String topic = this.topicTextField.getText();
        if (topic.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.TOPIC_EMPTY_ERROR);
            return;
        }

        // Use topic as a base of workflow instance ID so that the monitor can
        // find it.
        URI workfowInstanceID = URI.create(StringUtil.convertToJavaIdentifier(topic));
        this.workflow.setGPELInstanceID(workfowInstanceID);

        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        notifConfig.setTopic(topic);
        arguments.add("-" + JythonScript.TOPIC_VARIABLE);
        arguments.add(topic);

        // TODO error check for user inputs

        List<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        final org.xmlpull.v1.builder.XmlElement inputs = builder.newFragment("inputs");
        for (int i = 0; i < inputNodes.size(); i++) {
            InputNode inputNode = inputNodes.get(i);
            XBayaTextField parameterTextField = this.parameterTextFields.get(i);
            String id = inputNode.getID();
            String value = parameterTextField.getText();
            org.xmlpull.v1.builder.XmlElement input = inputs.addElement(id);
            input.addChild(value);
        }

        this.script.getScript();
        final String topicString = topic;
        new Thread() {
            /**
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                try {
                    MonitorConfiguration notifConfig = TavernaRunnerWindow.this.engine.getMonitor().getConfiguration();
                    TavernaRunnerWindow.this.engine.getMonitor().start();
                    notifConfig.setTopic(topicString);
                    TavernaRunnerWindow.this.engine.getGUI().addDynamicExecutionToolsToToolbar();
                    new WorkflowInterpreter(TavernaRunnerWindow.this.engine, topicString).scheduleDynamically();
                } catch (XBayaException e) {
                    TavernaRunnerWindow.this.engine.getErrorWindow().error(e);
                }

            }
        }.start();


        hide();
    }
}