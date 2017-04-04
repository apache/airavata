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

import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasListener;
import org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent.GraphCanvasEventType;
import org.apache.airavata.xbaya.ui.widgets.XBayaComponent;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelector;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelectorEvent;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelectorListener;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelectorEvent.ComponentSelectorEventType;

import xsul5.XmlConstants;

public class ComponentViewer implements GraphCanvasListener, ComponentSelectorListener, XBayaComponent {

    /**
     * The title
     */
    public static final String TITLE = "Component Information";

    private static final String DEFAULT_HTML_MESSAGE = "<html> Select a component from the " + ComponentSelector.TITLE
            + ".</html>";

    private Component currentComponent;

    private JEditorPane editorPane;

    /**
     * Creates a ComponentViewer.
     */
    public ComponentViewer() {
        super();

        this.currentComponent = null;

        this.editorPane = new JEditorPane();
        this.editorPane.setEditable(false);
        this.editorPane.setBackground(Color.WHITE);

        this.editorPane.setContentType(XmlConstants.CONTENT_TYPE_HTML);
        this.editorPane.setText(DEFAULT_HTML_MESSAGE);
    }

    /**
     * @return the GUI component
     */
    public JEditorPane getSwingComponent() {
        return this.editorPane;
    }

    /**
     * Sets a component to show.
     * 
     * @param component
     *            the component to show
     */
    public void setComponent(final Component component) {
        // logger.entering(component);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (component == null) {
                    ComponentViewer.this.editorPane.setText(DEFAULT_HTML_MESSAGE);
                } else if (component != ComponentViewer.this.currentComponent) {
                    ComponentViewer.this.editorPane.setText(component.toHTML());
                    // To prevent from scrolling down to the bottom.
                    ComponentViewer.this.editorPane.setCaretPosition(0);
                }
                ComponentViewer.this.currentComponent = component;
            }
        });
        // logger.exiting();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphCanvasListener#graphCanvasChanged(org.apache.airavata.xbaya.ui.graph.GraphCanvasEvent)
     */
    public void graphCanvasChanged(GraphCanvasEvent event) {
        GraphCanvasEventType type = event.getType();
        GraphCanvas graphCanvas = event.getGraphCanvas();
        switch (type) {
        case NODE_SELECTED:
            Node node = graphCanvas.getSelectedNode();
            if (node == null) {
                setComponent(null);
            } else {
                setComponent(node.getComponent());
            }
            break;
        case GRAPH_LOADED:
        case NAME_CHANGED:
        case INPUT_PORT_SELECTED:
        case OUTPUT_PORT_SELECTED:
            // do nothing
        }
    }

    /**
     * @see org.apache.airavata.xbaya.ui.widgets.component.ComponentSelectorListener#componentSelectorChanged(org.apache.airavata.xbaya.ui.widgets.component.ComponentSelectorEvent)
     */
    public void componentSelectorChanged(ComponentSelectorEvent event) {
        ComponentSelectorEventType type = event.getType();
        switch (type) {
        case COMPONENT_SELECTED:
            Component component = event.getComponent();
            setComponent(component);
            break;
        }
    }

}