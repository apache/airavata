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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.NameValidator;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComboBox;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.xbaya.registry.RegistryAccesser;
import org.ogce.schemas.gfac.beans.MethodBean;
import org.ogce.schemas.gfac.beans.ServiceBean;
import org.ogce.schemas.gfac.beans.utils.ParamObject;

public class ServiceDescriptionRegistrationWindow {
    private XBayaDialog dialog;

    private XBayaEngine engine;

    private XBayaComboBox appNameComboBox;
    private XBayaLabel appNameLabel;
    private XBayaLabel inactivityLimitLabel;
    private XBayaTextField inactivityLimitTextField;
    private XBayaLabel inputNumLabel;
    private JSpinner outputNumSpinner;
    private XBayaLabel methodDescLabel;
    private XBayaTextField methodDescTextField;
    private XBayaLabel methodNameLabel;
    private XBayaTextField methodNameTextField;
    private XBayaLabel outputNumLabel;
    private JSpinner inputNumSpinner;
    private XBayaLabel serviceNameLabel;
    private XBayaTextField serviceNameTextField;

    private JButton configInputsButton;
    private JButton configOutputsButton;
    private JButton addServiceButton;
    private JButton cancelButton;

    private boolean isEditing = false;

    private ServiceBean editingServiceBean;

    private List<ParamObject> inputArguments = new ArrayList<ParamObject>();
    private List<ParamObject> outputArguments = new ArrayList<ParamObject>();

    private static ServiceDescriptionRegistrationWindow window;

    /**
     * Constructs a ServiceDescriptionRegistrationWindow.
     * 
     * @param engine
     */
    private ServiceDescriptionRegistrationWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private ServiceDescriptionRegistrationWindow() {
        // Intend to be blank;
    }

    /**
     * @return ServiceDescriptionRegistrationWindow
     */
    public static ServiceDescriptionRegistrationWindow getInstance() {
        if (window == null) {
            window = new ServiceDescriptionRegistrationWindow();
        }

        return window;
    }

//    private Object[] initApplicationNameList() {
//        XRegistryAccesser xRegAccesser = new XRegistryAccesser(this.engine);
//        AppData[] appDataList = xRegAccesser.searchApplicationByName("");
//        List<String> nameList = new ArrayList<String>();
//        nameList.add("Select Application");
//        for (AppData appData : appDataList) {
//            nameList.add(appData.getName().getLocalPart());
//        }
//        return nameList.toArray();
//    }

//    /**
//     * ReInit Application Name ComboBox
//     */
//    public void reinitApplicationComboBox() {
//        this.appNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(initApplicationNameList()));
//    }

    /**
     * Clear ALl the TextFields
     */
    private void clearAllTextFields() {
        this.inactivityLimitTextField.setText("15");
        this.methodDescTextField.setText("");
        this.methodNameTextField.setText("");
        this.serviceNameTextField.setText("");
        this.inputNumSpinner.setValue(0);
        this.outputNumSpinner.setValue(0);
        this.inputArguments.clear();
        this.outputArguments.clear();
    }

    /**
     * @param serviceBean
     */
    public void initTextFields(ServiceBean serviceBean) {
        this.isEditing = true;
        this.editingServiceBean = serviceBean;
        this.serviceNameTextField.setText(serviceBean.getServiceName());
        this.appNameComboBox.setSelectedItem(serviceBean.getApplicationName());
        this.inactivityLimitTextField.setText(Integer.toString(serviceBean.getNotAfterInactiveMinutes()));
        MethodBean methodBean = serviceBean.getMethodBean();
        this.methodNameTextField.setText(methodBean.getMethodName());
        this.methodDescTextField.setText(methodBean.getMethodDescription());
        this.inputNumSpinner.setValue(methodBean.getInputParms().size());
        this.outputNumSpinner.setValue(methodBean.getOutputParms().size());

        this.inputArguments = methodBean.getInputParms();
        this.outputArguments = methodBean.getOutputParms();

        this.addServiceButton.setText("Update Service");
    }

    private void initGUI() {
        GridPanel infoPanel = new GridPanel();

        this.serviceNameTextField = new XBayaTextField();
        this.serviceNameLabel = new XBayaLabel("Service Name", this.serviceNameTextField);

        this.inactivityLimitTextField = new XBayaTextField("15");
        this.inactivityLimitLabel = new XBayaLabel("Inactivity Limit", this.inactivityLimitTextField);

        this.methodNameTextField = new XBayaTextField();
        this.methodNameLabel = new XBayaLabel("Method Name", this.methodNameTextField);

        this.methodDescTextField = new XBayaTextField();
        this.methodDescLabel = new XBayaLabel("Method Description", this.methodDescTextField);

//        this.appNameComboBox = new XBayaComboBox(new DefaultComboBoxModel(initApplicationNameList()));
        this.appNameLabel = new XBayaLabel("Application Name", this.appNameComboBox);

        this.inputNumSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        this.inputNumLabel = new XBayaLabel("Number of Inputs", this.inputNumSpinner);

        JLabel inputDumpLabel = new JLabel();
        this.configInputsButton = new JButton("Config Inputs");
        this.configInputsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configInputsButtonActionPerformed();
            }
        });

        this.outputNumSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        this.outputNumLabel = new XBayaLabel("Number of Outputs", this.outputNumSpinner);

        JLabel outputDumpLabel = new JLabel();
        this.configOutputsButton = new JButton("Config Outputs");
        this.configOutputsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configOutputsButtonActionPerformed();
            }
        });

        infoPanel.add(this.serviceNameLabel);
        infoPanel.add(this.serviceNameTextField);
        infoPanel.add(this.inactivityLimitLabel);
        infoPanel.add(this.inactivityLimitTextField);
        infoPanel.add(this.methodNameLabel);
        infoPanel.add(this.methodNameTextField);
        infoPanel.add(this.methodDescLabel);
        infoPanel.add(this.methodDescTextField);
        infoPanel.add(this.appNameLabel);
        infoPanel.add(this.appNameComboBox);
        infoPanel.add(this.inputNumLabel);
        infoPanel.add(this.inputNumSpinner);
        infoPanel.add(inputDumpLabel);
        infoPanel.add(this.configInputsButton);
        infoPanel.add(this.outputNumLabel);
        infoPanel.add(this.outputNumSpinner);
        infoPanel.add(outputDumpLabel);
        infoPanel.add(this.configOutputsButton);

        infoPanel.layout(9, 2, GridPanel.WEIGHT_NONE, 1);

        JPanel buttonPanel = new JPanel();
        this.addServiceButton = new JButton("Add Service");
        this.addServiceButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServiceButtonActionPerformed();
            }
        });
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        buttonPanel.add(this.addServiceButton);
        buttonPanel.add(this.cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Register Service Description", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(this.cancelButton);
    }

    private void addServiceButtonActionPerformed() {// GEN-FIRST:event_addServiceButtonActionPerformed

        this.addServiceButton.setText("Add Service");

        /* Input Validity Check */
        if (!NameValidator.validate(this.serviceNameTextField.getText())) {
            JOptionPane.showMessageDialog(this.dialog.getDialog(), "Service Name Invalid", "Error",
                    JOptionPane.ERROR_MESSAGE);
            this.serviceNameTextField.getSwingComponent().requestFocus();
            return;
        }

        if (!NameValidator.validate(this.methodNameTextField.getText())) {
            JOptionPane.showMessageDialog(this.dialog.getDialog(), "Method Name Invalid", "Error",
                    JOptionPane.ERROR_MESSAGE);
            this.methodNameTextField.getSwingComponent().requestFocus();
            return;
        }

        /* Generate Bean Object */
        try {
            ServiceBean serviceBean = new ServiceBean();
            serviceBean.setServiceName(StringUtil.trimSpaceInString(this.serviceNameTextField.getText()));
            serviceBean.setApplicationName(this.appNameComboBox.getText());
            serviceBean.setNotAfterInactiveMinutes(Integer.parseInt(this.inactivityLimitTextField.getText()));

            MethodBean methodBean = new MethodBean();
            methodBean.setMethodName(StringUtil.trimSpaceInString(this.methodNameTextField.getText()));
            methodBean.setMethodDescription(this.methodDescTextField.getText());

            // input arguments setup
            for (ParamObject paraObject : this.inputArguments) {
                methodBean.addInputParms(paraObject);
            }

            // output arguments setup
            for (ParamObject paraObject : this.outputArguments) {
                methodBean.addOutputParms(paraObject);
            }

            serviceBean.setMethodBean(methodBean);

            /* Register to XRegistry */
            RegistryAccesser xRegAccesser = new RegistryAccesser(this.engine);

//            if (!this.isEditing) {
//                xRegAccesser.registerService(serviceBean);
//            } else {
//                /* Delete old Service Bean */
//                QName qName = new QName(this.editingServiceBean.getObjectNamespace(),
//                        this.editingServiceBean.getServiceName());
//                xRegAccesser.deleteServiceDescrption(qName);
//
//                /* Register updated Service Bean */
//                xRegAccesser.registerService(serviceBean);
//
//                this.isEditing = false;
//                this.addServiceButton.setText("Add Service");
//            }

        } catch (Exception e) {
            this.engine.getErrorWindow().error(this.dialog.getDialog(), e.getMessage(), e);
            this.hide();
            return;
        }

        /* Clear All The Fields */
        clearAllTextFields();

        /* "Close" the windows */
        this.hide();

    }// GEN-LAST:event_addServiceButtonActionPerformed

    private void cancelButtonActionPerformed() {// GEN-FIRST:event_cancelButtonActionPerformed
        this.isEditing = false;
        this.addServiceButton.setText("Add Service");
        clearAllTextFields();
        this.hide();
    }// GEN-LAST:event_cancelButtonActionPerformed

    private void configInputsButtonActionPerformed() {// GEN-FIRST:event_configInputsButtonActionPerformed
        try {
            if (((Integer) this.inputNumSpinner.getValue()).intValue() != 0) {
                int number = ((Integer) this.inputNumSpinner.getValue()).intValue();
                new ArgumentsConfigWindow(number, this.inputArguments, this.engine);
            } else {
                JOptionPane.showMessageDialog(this.dialog.getDialog(), "Please input the number of inputs", "Message",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.dialog.getDialog(), "Invalid Input Parameter", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }// GEN-LAST:event_configInputsButtonActionPerformed

    private void configOutputsButtonActionPerformed() {// GEN-FIRST:event_configOutputsButtonActionPerformed
        try {
            if (((Integer) this.outputNumSpinner.getValue()).intValue() != 0) {
                int number = ((Integer) this.outputNumSpinner.getValue()).intValue();
                new ArgumentsConfigWindow(number, this.outputArguments, this.engine);
            } else {
                JOptionPane.showMessageDialog(this.dialog.getDialog(), "Please input the number of outputs", "Message",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.dialog.getDialog(), "Invalid Output Parameter", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }// GEN-LAST:event_configOutputsButtonActionPerformed

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
       // this.reinitApplicationComboBox();
        this.dialog.show();
    }

    /**
     * @param serviceBean
     */
    public void show(ServiceBean serviceBean) {
        initTextFields(serviceBean);
        this.dialog.show();
    }
}