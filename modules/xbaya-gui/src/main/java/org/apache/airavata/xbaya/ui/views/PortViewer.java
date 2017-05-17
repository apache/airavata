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
package org.apache.airavata.xbaya.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasListener;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent.GraphCanvasEventType;

import xsul5.XmlConstants;

public class PortViewer implements GraphCanvasListener {

    /**
     * The title.
     */
    public static final String TITLE = "Parameters";

    private JPanel panel;

    private JEditorPane outputEditor;

    private JEditorPane inputEditor;

    /**
     * Creates a PortInformation
     */
    public PortViewer() {
        this.panel = new JPanel();
        this.panel.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        this.panel.setPreferredSize(new Dimension(0, 150));

        JPanel inBox = new JPanel(new BorderLayout());
        inBox.setBorder(new TitledBorder(new EtchedBorder(), "Input Parameter"));
        this.inputEditor = createEditorPane();
        JScrollPane inputScrollPane = new JScrollPane(this.inputEditor);
        inputScrollPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        inBox.add(inputScrollPane, BorderLayout.CENTER);
        inBox.setMinimumSize(SwingUtil.MINIMUM_SIZE);

        JPanel outBox = new JPanel(new BorderLayout());
        outBox.setBorder(new TitledBorder(new EtchedBorder(), "Output Parameter"));
        this.outputEditor = createEditorPane();
        JScrollPane outScrollPane = new JScrollPane(this.outputEditor);
        outScrollPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        outBox.add(outScrollPane, BorderLayout.CENTER);
        outBox.setMinimumSize(SwingUtil.MINIMUM_SIZE);

        this.panel.setLayout(new GridLayout(1, 2));
        this.panel.add(outBox);
        this.panel.add(inBox);
    }

    /**
     * @return The panel
     */
    public JComponent getSwingComponent() {
        return this.panel;
    }

    /**
     * @param port
     */
    public void setOutputPort(Port port) {
        showPortInfo(this.outputEditor, port);
    }

    /**
     * @param port
     */
    public void setInputPort(Port port) {
        showPortInfo(this.inputEditor, port);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphCanvasListener#graphCanvasChanged(org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent)
     */
    public void graphCanvasChanged(GraphCanvasEvent event) {
        GraphCanvasEventType type = event.getType();
        GraphCanvas graphCanvas = event.getGraphCanvas();
        switch (type) {
        case INPUT_PORT_SELECTED:
            Port inputPort = graphCanvas.getSelectedInputPort();
            setInputPort(inputPort);
            break;
        case OUTPUT_PORT_SELECTED:
            Port outputPort = graphCanvas.getSelectedOutputPort();
            setOutputPort(outputPort);
            break;
        case GRAPH_LOADED:
        case NAME_CHANGED:
        case NODE_SELECTED:
            // do nothing
        }
    }

    /**
     * Shows the information of a selected port on the list specified.
     * 
     * @param editor
     * @param port
     */
    private void showPortInfo(JEditorPane editor, Port port) {
        if (port == null) {
            editor.setText("");
        } else {
            // TODO dispatch to each port?.
            StringBuilder buf = new StringBuilder();
            buf.append("<strong>Component: " + port.getNode().getName() + "</strong><br>");
            buf.append("<strong>Port: " + port.getName() + "<br></strong>");
            if (port instanceof DataPort) {
                buf.append("<strong>Type</strong>: " + ((DataPort) port).getType() + "<br>");
            } else {
                // TODO
            }
            buf.append("<strong>Description</strong>: " + port.getComponentPort().getDescription() + "<br>");
            editor.setText(buf.toString());

            // To prevent from scrolling down to the bottom.
            editor.setCaretPosition(0);
        }
    }

    private JEditorPane createEditorPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        editorPane.setEditable(false);
        editorPane.setBackground(Color.WHITE);
        editorPane.setContentType(XmlConstants.CONTENT_TYPE_HTML);
        return editorPane;
    }

}