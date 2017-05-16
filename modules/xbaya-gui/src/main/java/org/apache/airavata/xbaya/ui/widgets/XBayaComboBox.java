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

import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class XBayaComboBox implements XBayaComponent {

    /**
     * DEFAULT_COLUMNS
     */
    public static final int DEFAULT_COLUMNS = 30;

    private JComboBox comboBox;

    /**
     * Constructs a XBayaTextArea.
     * 
     * @param model
     */
    public XBayaComboBox(ComboBoxModel model) {
        init(model);
    }

    /**
     * @return The swing component.
     */
    public JComboBox getSwingComponent() {
        return getJComboBox();
    }

    /**
     * @return The text. It never returns null.
     */
    public String getText() {
        return (String) this.comboBox.getSelectedItem();
    }

    /**
     * @param str
     */
    public void setSelectedItem(String str) {
        this.comboBox.setSelectedItem(str);
    }

    /**
     * @param model
     */
    public void setModel(ComboBoxModel model) {
        this.comboBox.setModel(model);
    }

    /**
     * @return The text field
     */
    public JComboBox getJComboBox() {
        return this.comboBox;
    }

    /**
     * @param editable
     */
    public void setEditable(boolean editable) {
        this.comboBox.setEditable(editable);
    }

    private void init(ComboBoxModel model) {
        this.comboBox = new JComboBox(model);
        this.comboBox.setEditable(false);
    }

    /**
     * @param listener
     */
    public void addItemListener(ItemListener listener) {
        this.comboBox.addItemListener(listener);
    }
}