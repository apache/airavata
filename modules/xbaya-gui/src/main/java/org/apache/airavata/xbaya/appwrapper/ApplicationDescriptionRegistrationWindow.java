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

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.NameValidator;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComboBox;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;
import org.ogce.schemas.gfac.beans.ApplicationBean;

import xregistry.generated.HostDescData;

public class ApplicationDescriptionRegistrationWindow {
    private XBayaDialog dialog;

    private XBayaEngine engine;

    private JButton addApplicationButton;
    private XBayaLabel appParaFileLabel;
    private XBayaTextField appParaFileTextField;
    private XBayaLabel applicationNameLabel;
    private XBayaTextField applicationNameTextField;
    private JButton cancelButton;
    private XBayaLabel executableLabel;
    private XBayaTextField executableTextField;
    private XBayaLabel hostCountLabel;
    private XBayaTextField hostCountTextField;
    private XBayaComboBox hostNameComboBox;
    private XBayaLabel hostNameLabel;
    private JCheckBox isPublicCheckBox;
    private XBayaLabel isPublicLabel;
    private XBayaComboBox jobTypeComboBox;
    private XBayaLabel jobTypeLabel;
    private XBayaLabel maxWallTimeLabel;
    private XBayaTextField maxWallTimeTextField;
    private XBayaLabel minMemoryLabel;
    private XBayaTextField minMemoryTextField;
    private XBayaLabel procsCountLabel;
    private XBayaTextField procsCountTextField;
    private XBayaComboBox projectNameComboBox;
    private XBayaLabel projectNameLabel;
    private XBayaLabel queueLabel;
    private XBayaTextField queueTextField;
    private XBayaLabel rslParaNameLabel;
    private XBayaTextField rslParamNameTextField;
    private XBayaLabel rslParamValueLabel;
    private XBayaTextField rslParamValueTextField;
    private XBayaLabel tempDirLabel;
    private XBayaTextField tempDirTextField;
    private XBayaLabel workDirectoryLabel;
    private XBayaTextField workDirectoryTextField;

    private boolean isEditing = false;

    private ApplicationBean editingAppBean;

    private static ApplicationDescriptionRegistrationWindow window;

    /**
     * Constructs a ApplicationDescriptionRegistrationWindow.
     * 
     * @param engine
     */
    private ApplicationDescriptionRegistrationWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private ApplicationDescriptionRegistrationWindow() {
        // Intend to be blank;
    }

    /**
     * @return ApplicationDescriptionRegistrationWindow
     */
    public static ApplicationDescriptionRegistrationWindow getInstance() {
        if (window == null) {
            window = new ApplicationDescriptionRegistrationWindow();
        }

        return window;
    }

    private Object[] initHostNameList() {
        XRegistryAccesser xRegAccesser = new XRegistryAccesser(this.engine);
        HostDescData[] hostDataList = xRegAccesser.searchHostByName("");
        List<String> nameList = new ArrayList<String>();
        for (HostDescData hostData : hostDataList) {
            nameList.add(hostData.getName().toString());
        }
        return nameList.toArray();
    }

    /**
     * ReInit Host Name ComboBox
     */
    public void reinitHostComboBox() {
        this.hostNameComboBox.setModel(new DefaultComboBoxModel(initHostNameList()));
    }

    /**
     * Clear All the TextFields
     */
    private void clearAllTextFields() {
        this.appParaFileTextField.setText("");
        this.applicationNameTextField.setText("");
        this.executableTextField.setText("");
        this.hostCountTextField.setText("");
        this.maxWallTimeTextField.setText("");
        this.minMemoryTextField.setText("");
        this.procsCountTextField.setText("");
        this.queueTextField.setText("");
        this.rslParamNameTextField.setText("");
        this.rslParamValueTextField.setText("");
        this.tempDirTextField.setText("/tmp");
        this.workDirectoryTextField.setText("/tmp");
    }

    /**
     * @param appBean
     */
    public void initTextField(ApplicationBean appBean) {
        this.isEditing = true;
        this.editingAppBean = appBean;
        this.applicationNameTextField.setText(appBean.getApplicationName());
        this.hostNameComboBox.setSelectedItem(appBean.getHostName());
        this.executableTextField.setText(appBean.getExecutable());
        this.workDirectoryTextField.setText(appBean.getWorkDir());
        this.tempDirTextField.setText(appBean.getTmpDir());
        this.projectNameComboBox.setSelectedItem(appBean.getProjectName());
        if (appBean.getMaxWallTime() != null)
            this.maxWallTimeTextField.setText(appBean.getMaxWallTime().toString());
        this.queueTextField.setText(appBean.getQueue());
        if (appBean.getPcount() != null)
            this.procsCountTextField.setText(appBean.getPcount().toString());

        this.addApplicationButton.setText("Update Application");
    }

    private void initGUI() {
        GridPanel infoPanel = new GridPanel();
        this.applicationNameTextField = new XBayaTextField();
        this.applicationNameLabel = new XBayaLabel("Application Name", this.applicationNameTextField);

        this.hostNameComboBox = new XBayaComboBox(new DefaultComboBoxModel(initHostNameList()));
        this.hostNameComboBox.setEditable(false);
        this.hostNameLabel = new XBayaLabel("Host Name", this.hostNameComboBox);

        this.executableTextField = new XBayaTextField();
        this.executableLabel = new XBayaLabel("Executable", this.executableTextField);

        this.workDirectoryTextField = new XBayaTextField("/tmp");
        this.workDirectoryLabel = new XBayaLabel("Work Directory", this.workDirectoryTextField);

        this.tempDirTextField = new XBayaTextField("/tmp");
        this.tempDirLabel = new XBayaLabel("Temp Dir", this.tempDirTextField);

        this.projectNameComboBox = new XBayaComboBox(new DefaultComboBoxModel());
        this.projectNameComboBox.setEditable(true);
        this.projectNameLabel = new XBayaLabel("TeraGrid Project Name (optional)", this.projectNameComboBox);

        this.jobTypeComboBox = new XBayaComboBox(new DefaultComboBoxModel(new String[] { "single", "mpi", "multiple",
                "condor" }));
        this.jobTypeComboBox.setEditable(false);
        this.jobTypeLabel = new XBayaLabel("Job Type (optional)", this.jobTypeComboBox);

        this.queueTextField = new XBayaTextField();
        this.queueLabel = new XBayaLabel("Queue (optional)", this.queueTextField);

        this.maxWallTimeTextField = new XBayaTextField();
        this.maxWallTimeLabel = new XBayaLabel("Max Wall Time (optional)", this.maxWallTimeTextField);

        this.procsCountTextField = new XBayaTextField();
        this.procsCountLabel = new XBayaLabel("Processors Count (optional)", this.procsCountTextField);

        this.appParaFileTextField = new XBayaTextField();
        this.appParaFileLabel = new XBayaLabel("Application Parameter File (optional)", this.appParaFileTextField);

        this.minMemoryTextField = new XBayaTextField();
        this.minMemoryLabel = new XBayaLabel("Min Memory (optional)", this.minMemoryTextField);

        this.hostCountTextField = new XBayaTextField();
        this.hostCountLabel = new XBayaLabel("Host Count (optional)", this.hostCountTextField);

        this.rslParamNameTextField = new XBayaTextField();
        this.rslParaNameLabel = new XBayaLabel("RSL Param Name (optional)", this.rslParamNameTextField);

        this.rslParamValueTextField = new XBayaTextField();
        this.rslParamValueLabel = new XBayaLabel("RSL Param Value (optional)", this.rslParamValueTextField);

        this.isPublicCheckBox = new JCheckBox();
        this.isPublicLabel = new XBayaLabel("Is Public (optional)", this.isPublicCheckBox);

        infoPanel.add(this.applicationNameLabel);
        infoPanel.add(this.applicationNameTextField);
        infoPanel.add(this.hostNameLabel);
        infoPanel.add(this.hostNameComboBox);
        infoPanel.add(this.executableLabel);
        infoPanel.add(this.executableTextField);
        infoPanel.add(this.workDirectoryLabel);
        infoPanel.add(this.workDirectoryTextField);
        infoPanel.add(this.tempDirLabel);
        infoPanel.add(this.tempDirTextField);
        infoPanel.add(this.projectNameLabel);
        infoPanel.add(this.projectNameComboBox);
        infoPanel.add(this.jobTypeLabel);
        infoPanel.add(this.jobTypeComboBox);
        infoPanel.add(this.queueLabel);
        infoPanel.add(this.queueTextField);
        infoPanel.add(this.maxWallTimeLabel);
        infoPanel.add(this.maxWallTimeTextField);
        infoPanel.add(this.procsCountLabel);
        infoPanel.add(this.procsCountTextField);
        infoPanel.add(this.appParaFileLabel);
        infoPanel.add(this.appParaFileTextField);
        infoPanel.add(this.minMemoryLabel);
        infoPanel.add(this.minMemoryTextField);
        infoPanel.add(this.hostCountLabel);
        infoPanel.add(this.hostCountTextField);
        infoPanel.add(this.rslParaNameLabel);
        infoPanel.add(this.rslParamNameTextField);
        infoPanel.add(this.rslParamValueLabel);
        infoPanel.add(this.rslParamValueTextField);
        infoPanel.add(this.isPublicLabel);
        infoPanel.add(this.isPublicCheckBox);

        infoPanel.layout(16, 2, GridPanel.WEIGHT_NONE, 1);

        JPanel buttonPanel = new JPanel();
        this.addApplicationButton = new JButton();
        this.addApplicationButton.setText("Add Application");
        this.addApplicationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addApplicationButtonActionPerformed();
            }
        });

        this.cancelButton = new JButton();
        this.cancelButton.setText("Cancel");
        this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        buttonPanel.add(this.addApplicationButton);
        buttonPanel.add(this.cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Register Application Description", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(this.cancelButton);
    }

    private void addApplicationButtonActionPerformed() {// GEN-FIRST:event_addApplicationButtonActionPerformed

        this.addApplicationButton.setText("Add Application");

        try {

            /* Input Validity Check */
            if (!NameValidator.validate(this.applicationNameTextField.getText())) {
                JOptionPane.showMessageDialog(this.dialog.getDialog(), "Application Name Invalid", "Error",
                        JOptionPane.ERROR_MESSAGE);
                this.applicationNameTextField.getSwingComponent().requestFocus();
                return;
            }

            if (!this.procsCountTextField.getText().equals("")) {
                String numStr = this.procsCountTextField.getText();
                try {
                    Integer.parseInt(numStr);
                } catch (java.lang.NumberFormatException e) {
                    JOptionPane.showMessageDialog(this.dialog.getDialog(), "Processors Count Invalid", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    this.applicationNameTextField.getSwingComponent().requestFocus();
                    return;
                }
            }

            /* Generate Bean Object */
            ApplicationBean appBean = new ApplicationBean();
            appBean.setApplicationName(StringUtil.trimSpaceInString(this.applicationNameTextField.getText()));
            appBean.setHostName(StringUtil.trimSpaceInString(this.hostNameComboBox.getText()));
            appBean.setExecutable(StringUtil.trimSpaceInString(this.executableTextField.getText()));
            appBean.setWorkDir(StringUtil.trimSpaceInString(this.workDirectoryTextField.getText()));
            appBean.setTmpDir(StringUtil.trimSpaceInString(this.tempDirTextField.getText()));

            appBean.setProjectName(StringUtil.trimSpaceInString(this.projectNameComboBox.getText()));
            appBean.setJobType(StringUtil.trimSpaceInString(this.jobTypeComboBox.getText()));
            appBean.setQueue(StringUtil.trimSpaceInString(this.queueTextField.getText()));
            if (!this.maxWallTimeTextField.getText().equals("")) {
                appBean.setMaxWallTime(new Integer(Integer.parseInt(this.maxWallTimeTextField.getText())));
            }
            if (!this.procsCountTextField.getText().equals("")) {
                appBean.setPcount(new Integer(Integer.parseInt(this.procsCountTextField.getText())));
            }

            /* Register to XRegistry */
            XRegistryAccesser xRegAccesser = new XRegistryAccesser(this.engine);

            if (!this.isEditing) {
                xRegAccesser.registerApplication(appBean);
            } else {
                /* Delete old application bean */
                QName qName = new QName(this.editingAppBean.getObjectNamespace(),
                        this.editingAppBean.getApplicationName());
                xRegAccesser.deleteAppDescription(qName, this.editingAppBean.getHostName());

                /* Register new application bean */
                xRegAccesser.registerApplication(appBean);

                this.isEditing = false;
                this.addApplicationButton.setText("Add Application");
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.hide();
            return;
        }

        /* Clear All The Fields */
        clearAllTextFields();

        /* "Close" the windows */
        this.hide();

    }// GEN-LAST:event_addApplicationButtonActionPerformed

    private void cancelButtonActionPerformed() {// GEN-FIRST:event_cancelButtonActionPerformed
        this.isEditing = false;
        this.addApplicationButton.setText("Add Application");
        clearAllTextFields();
        this.hide();
    }// GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @return Status
     */
    public boolean isEngineSet() {
        if (this.engine == null) {
            return false;
        }
        return true;
    }

    /**
     * @param engine
     */
    public void setXBayaEngine(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * hide the dialog
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * show the dialog
     */
    public void show() {
        this.reinitHostComboBox();
        this.dialog.show();
    }

    /**
     * @param appBean
     */
    public void show(ApplicationBean appBean) {
        initTextField(appBean);
        this.dialog.show();
    }

}