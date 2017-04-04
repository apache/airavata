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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class XBayaLabel implements XBayaComponent {

    private JLabel label;

    /**
     * Constructs a XBayaLabel.
     * 
     * @param text
     * @param component
     *            The component to be labeled for.
     */
    public XBayaLabel(String text, XBayaComponent component) {
        this(text, component.getSwingComponent());
    }

    /**
     * Constructs a XBayaLabel.
     * 
     * @param text
     * @param component
     *            The component to be labeled for.
     */
    public XBayaLabel(String text, JComponent component) {
        init();
        setText(text);
        setLabelFor(component);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.widgets.XBayaComponent#getSwingComponent()
     */
    public JLabel getSwingComponent() {
        return getJLabel();
    }

    /**
     * @param text
     */
    public void setText(String text) {
        this.label.setText(text + ": ");
    }

    /**
     * @return The text field
     */
    public JLabel getJLabel() {
        return this.label;
    }

    /**
     * @param component
     */
    public void setLabelFor(XBayaComponent component) {
        setLabelFor(component.getSwingComponent());
    }

    /**
     * @param component
     */
    public void setLabelFor(JComponent component) {
        this.label.setLabelFor(component);
    }

    private void init() {
        this.label = new JLabel();
        this.label.setHorizontalAlignment(SwingConstants.TRAILING);
    }
}