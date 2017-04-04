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
package org.apache.airavata.xbaya.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.apache.airavata.common.utils.SwingUtil;

public class ScrollPanel implements XBayaComponent {

    private JPanel panel;

    private JComponent component;

    /**
     * Creates a ScrollPanel.
     * 
     * @param component
     * @param title
     */
    public ScrollPanel(XBayaComponent component, String title) {
        this(component.getSwingComponent(), title);
    }

    /**
     * Creates a ScrollPanel.
     * 
     * @param component
     * @param title
     */
    public ScrollPanel(JComponent component, String title) {
        this(component, title, true);
    }

    /**
     * Creates a ScrollPanel.
     * 
     * @param component
     * @param title
     * @param scroll
     */
    public ScrollPanel(XBayaComponent component, String title, boolean scroll) {
        this(component.getSwingComponent(), title, scroll);
    }

    /**
     * Creates a ScrollPanel.
     * 
     * @param component
     * @param title
     * @param scroll
     */
    public ScrollPanel(JComponent component, String title, boolean scroll) {
        this.component = component;
        init(component, title, scroll);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.widgets.XBayaComponent#getSwingComponent()
     */
    public JPanel getSwingComponent() {
        return getPanel();
    }

    /**
     * @return The panel.
     */
    public JPanel getPanel() {
        return this.panel;
    }

    /**
     * @param preferredSize
     */
    public void setPrefferedSize(Dimension preferredSize) {
        this.component.setPreferredSize(preferredSize);
    }

    private void init(JComponent comp, String title, boolean scroll) {
        this.panel = new JPanel();
        this.panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title);
        Border border = BorderFactory.createEtchedBorder();
        titleLabel.setBorder(border);
        this.panel.add(titleLabel, BorderLayout.NORTH);

        if (scroll) {
            JScrollPane scrollPane = new JScrollPane(comp);
            scrollPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
            this.panel.add(scrollPane, BorderLayout.CENTER);
        } else {
            comp.setMinimumSize(SwingUtil.MINIMUM_SIZE);
            this.panel.add(comp, BorderLayout.CENTER);
        }
    }
}