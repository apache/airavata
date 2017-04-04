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

import java.net.URI;

import javax.swing.JTextField;

import org.apache.airavata.common.utils.StringUtil;

public class XBayaTextField implements XBayaTextComponent {

    /**
     * DEFAULT_COLUMNS
     */
    public static final int DEFAULT_COLUMNS = 30;

    private JTextField textArea;

    /**
     * Constructs a XBayaTextArea.
     */
    public XBayaTextField() {
        init();
    }

    /**
     * Constructs a XBayaTextField.
     * 
     * @param initStr
     */
    public XBayaTextField(String initStr) {
        init();
        this.textArea.setText(initStr);
    }

    /**
     * @return The swing component.
     */
    public JTextField getSwingComponent() {
        return getTextField();
    }

    /**
     * @param uri
     */
    public void setText(URI uri) {
        setText(StringUtil.toString(uri));
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
     * @return The text. It never returns null.
     */
    public String getText() {
        return this.textArea.getText().trim();
    }

    /**
     * @return The text field
     */
    public JTextField getTextField() {
        return this.textArea;
    }

    /**
     * @param editable
     */
    public void setEditable(boolean editable) {
        this.textArea.setEditable(editable);
    }

    /**
     * @param columns
     */
    public void setColumns(int columns) {
        this.textArea.setColumns(columns);
    }

    /**
     * Sets whether or not this component is enabled.
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.textArea.setEnabled(enabled);
    }

    private void init() {
        this.textArea = new JTextField(DEFAULT_COLUMNS);
        this.textArea.setEditable(true);
    }
}