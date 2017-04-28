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

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class XBayaTextArea implements XBayaTextComponent {

    /**
     * DEFAULT_WIDTH
     */
    public static final int DEFAULT_WIDTH = 300;

    /**
     * DEFAULT_HEIGHT
     */
    public static final int DEFAULT_HEIGHT = 200;

    private JTextArea textArea;

    private JScrollPane scrollPane;

    /**
     * Constructs a XBayaTextArea.
     */
    public XBayaTextArea() {
        init();
    }

    /**
     * @return The swing component.
     */
    public JScrollPane getSwingComponent() {
        return getScrollPane();
    }

    /**
     * @param text
     */
    public void setText(String text) {
        if (text == null) {
            text = "";
        } else {
            text = text.trim();
        }
        this.textArea.setText(text);
        this.textArea.setCaretPosition(0);
    }

    /**
     * @return The text
     */
    public String getText() {
        return this.textArea.getText().trim();
    }

    /**
     * @return The scroll pane.
     */
    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    /**
     * @return The text area
     */
    public JTextArea getTextArea() {
        return this.textArea;
    }

    /**
     * @param editable
     */
    public void setEditable(boolean editable) {
        this.textArea.setEditable(editable);
    }

    /**
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        Dimension size = new Dimension(width, height);
        this.scrollPane.setMinimumSize(size);
        this.scrollPane.setPreferredSize(size);
    }

    private void init() {
        this.textArea = new JTextArea();
        this.textArea.setEditable(true);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.scrollPane = new JScrollPane(this.textArea);
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}