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

package org.apache.airavata.xbaya.appwrapper;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.util.StringUtil;
import org.ogce.schemas.gfac.beans.utils.ParamObject;

/**
 * @author Ye Fan
 */
public class ArgumentsConfigWindow {
    private XBayaDialog dialog;

    private XBayaEngine engine;

    private GridPanel argsPanel;

    private List<ParamObject> arguments;

    private int rows;

    /**
     * Constructs a ArgumentsConfigWindow.
     * 
     * @param numOfArgs
     * @param args
     * @param engine
     */
    public ArgumentsConfigWindow(int numOfArgs, List<ParamObject> args, XBayaEngine engine) {
        this.engine = engine;
        this.arguments = args;
        this.rows = numOfArgs;
        initArgsPanel();
        initGUI();
    }

    /**
     * Constructs a ArgumentsConfigWindow.
     * 
     * @param args
     * @param engine
     */
    public ArgumentsConfigWindow(List<ParamObject> args, XBayaEngine engine) {
        this(args.size(), args, engine);
    }

    private void initArgsPanel() {
        // init argsPanel
        this.argsPanel = new GridPanel();

        this.argsPanel.add(new JLabel("Input Name", SwingConstants.CENTER));
        this.argsPanel.add(new JLabel("Input Description", SwingConstants.CENTER));
        this.argsPanel.add(new JLabel("Input Type", SwingConstants.CENTER));

        for (int i = 0; i < this.rows; i++) {
            String name;
            String desc;
            String type;
            if (i < this.arguments.size()) {
                name = this.arguments.get(i).getName();
                desc = this.arguments.get(i).getDesc();
                type = this.arguments.get(i).getType();
            } else {
                name = "";
                desc = "";
                type = "String";
            }
            this.argsPanel.add(new JTextField(name));
            this.argsPanel.add(new JTextField(desc));
            JComboBox typeComboBox = new JComboBox(new DefaultComboBoxModel(new String[] { "String", "Integer",
                    "Float", "Double", "Boolean", "URI", "StringArray", "IntegerArray", "FloatArray", "DoubleArray",
                    "BooleanArray", "URIArray", "XmlElement", "HostName" }));
            typeComboBox.setSelectedItem(type);
            this.argsPanel.add(typeComboBox);
        }

        this.argsPanel.layout(this.rows + 1, 3, GridPanel.WEIGHT_NONE, 1);
    }

    private void initGUI() {

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("ok");
        JButton cancelButton = new JButton("cancel");

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Arguments Configuration", this.argsPanel, buttonsPanel);
        this.dialog.setDefaultButton(cancelButton);
        this.dialog.show();
    }

    private void okButtonActionPerformed() {
        this.arguments.clear();
        for (int i = 0; i < this.rows; i++) {
            this.arguments.add(getParamAt(i));
        }

        this.dialog.getDialog().dispose();
    }

    private void cancelButtonActionPerformed() {
        this.dialog.getDialog().dispose();
    }

    private ParamObject getParamAt(int row) {
        String name = StringUtil.trimSpaceInString(((JTextField) this.argsPanel.getSwingComponent().getComponent(
                row * 3 + 3)).getText());
        String desc = ((JTextField) this.argsPanel.getSwingComponent().getComponent(row * 3 + 4)).getText();
        String type = (String) ((JComboBox) this.argsPanel.getSwingComponent().getComponent(row * 3 + 5))
                .getSelectedItem();
        ParamObject paramObject = new ParamObject(name, type, row, desc);
        return paramObject;
    }
}