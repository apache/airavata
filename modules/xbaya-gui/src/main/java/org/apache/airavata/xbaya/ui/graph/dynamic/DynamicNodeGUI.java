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

package org.apache.airavata.xbaya.ui.graph.dynamic;

import java.awt.event.MouseEvent;

import org.apache.airavata.workflow.model.graph.dynamic.DynamicNode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.graph.dynamic.DynamicNodeWindow;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DynamicNodeGUI extends NodeGUI {

    private static final Log logger = LogFactory.getLog(DynamicNodeGUI.class);

    private DynamicNode node;

    private DynamicNodeWindow window;

    /**
     * Creates a WsNodeGui
     * 
     * @param node
     */
    public DynamicNodeGUI(DynamicNode node) {
        super(node);
        this.node = node;
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        logger.info(event.toString());
        if (event.getClickCount() >= 2) {
            showWindow(engine);
        }
    }

    private void showWindow(XBayaEngine engine) {
        if (this.window == null) {
            this.window = new DynamicNodeWindow(engine, this.node);
        }
        try {
            this.window.show();
        } catch (Throwable e) {
            engine.getGUI().getErrorWindow().error(e);
        }
    }
}