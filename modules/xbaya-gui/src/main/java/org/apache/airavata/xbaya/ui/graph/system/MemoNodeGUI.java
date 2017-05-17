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
package org.apache.airavata.xbaya.ui.graph.system;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.apache.airavata.workflow.model.graph.system.MemoNode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.MemoConfigurationDialog;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class MemoNodeGUI extends NodeGUI {

    private static final int BORDER_SIZE = 5;

    private MemoNode node;

    private JTextArea textArea;

    private MemoConfigurationDialog window;

    /**
     * @param node
     */
    public MemoNodeGUI(MemoNode node) {
        super(node);
        this.node = node;

        this.textArea = new JTextArea();
        this.textArea.setBorder(new EmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        if (event.getClickCount() >= 2) {
            showWindow(engine);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#isIn(java.awt.Point)
     */
    @Override
    protected boolean isIn(Point point) {
        Point position = this.node.getPosition();
        Dimension preferredSize = this.textArea.getPreferredSize();
        Rectangle area = new Rectangle(position, preferredSize);
        return area.contains(point);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#paint(java.awt.Graphics2D)
     */
    @Override
    protected void paint(Graphics2D g) {
        Point position = this.node.getPosition();
        this.textArea.setText(this.node.getMemo());
        Dimension preferredSize = this.textArea.getPreferredSize();
        Rectangle bounds = new Rectangle(position.x, position.y, preferredSize.width, preferredSize.height);
        this.textArea.setBounds(bounds);
        Graphics graphics = g.create(position.x, position.y, preferredSize.width, preferredSize.height);
        this.textArea.paint(graphics);
    }

    /**
     * @param engine
     */
    private void showWindow(XBayaEngine engine) {
        if (this.window == null) {
            this.window = new MemoConfigurationDialog(this.node, engine);
        }
        this.window.show();
    }

}