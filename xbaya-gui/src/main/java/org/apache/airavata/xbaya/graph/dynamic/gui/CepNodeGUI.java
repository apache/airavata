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

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.dynamic.CepNode;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;

import xsul.MLogger;

public class CepNodeGUI extends NodeGUI {

    private final static MLogger logger = MLogger.getLogger();

    private CepNode node;

    private CepNodeWindow window;

    /**
     * Creates a WsNodeGui
     * 
     * @param node
     */
    public CepNodeGUI(CepNode node) {
        super(node);
        this.node = node;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        logger.finest(event.toString());
        if (event.getClickCount() >= 2) {
            showWindow(engine);
        }
    }

    private void showWindow(XBayaEngine engine) {
        List<DataPort> inputPorts = this.node.getInputPorts();
        // are they all connected
        boolean connected = true;
        for (DataPort dataPort : inputPorts) {
            if (null == dataPort.getFromNode()) {
                connected = false;
                break;
            }
        }
        if (!connected) {

            JOptionPane.showMessageDialog(engine.getGUI().getFrame(),
                    "All inputs need to be connected before configuring");
            return;
        }
        if (this.window == null) {
            this.window = new CepNodeWindow(engine, this.node);
        }
        try {
            this.window.show();
        } catch (Throwable e) {
            engine.getErrorWindow().error(e);
        }
    }
}