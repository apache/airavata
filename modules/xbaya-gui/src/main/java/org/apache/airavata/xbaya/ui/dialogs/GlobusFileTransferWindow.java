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
///*
//*
//* Licensed to the Apache Software Foundation (ASF) under one
//* or more contributor license agreements.  See the NOTICE file
//* distributed with this work for additional information
//* regarding copyright ownership.  The ASF licenses this file
//* to you under the Apache License, Version 2.0 (the
//* "License"); you may not use this file except in compliance
//* with the License.  You may obtain a copy of the License at
//*
//*   http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*
//*/
//
//package org.apache.airavata.xbaya.ui.dialogs;
//
//import org.apache.airavata.xbaya.XBayaEngine;
//import org.apache.airavata.xbaya.ui.widgets.GridPanel;
//import org.apache.airavata.xbaya.ui.widgets.XBayaComboBox;
//import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
//import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
//import org.apache.airavata.xbaya.util.GlobusOnlineUtils;
//import org.apache.airavata.xbaya.util.TransferFile;
//import org.globusonline.transfer.APIError;
//import org.json.JSONException;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class GlobusFileTransferWindow {
//    private XBayaEngine engine;
//
//    private XBayaDialog dialog;
//
//    private XBayaTextField usernameTextField;
//
//    private JPasswordField pwdTextField;
//
//    private XBayaComboBox sourceEndpointTextField;
//
//    private XBayaTextField sourceFilePathTextField;
//
//    private XBayaComboBox destEndpointTextField;
//
//    private XBayaTextField destFilePathTextField;
//
//    private XBayaTextField transferLabelTextField;
//
//    private GlobusOnlineUtils globusOnlineUtils;
//
//
//    private String goUserName;
//    private String goPWD;
//
//    /**
//     * @param engine XBaya workflow engine
//     */
//    public GlobusFileTransferWindow(XBayaEngine engine) {
//        this.engine = engine;
//        initGUI();
//    }
//
//    /**
//     * Displays the dialog.
//     */
//    public void show() {
//        this.dialog.show();
//    }
//
//    private void hide() {
//        this.dialog.hide();
//    }
//
//    private void ok() {
//
//        goUserName = this.usernameTextField.getText();
//        goPWD  = new String(this.pwdTextField.getPassword());
//
//        String sourceEndpoint = this.sourceEndpointTextField.getText();
//        String sourceFilePath = this.sourceFilePathTextField.getText();
//        String destEndpoint = this.destEndpointTextField.getText();
//        String destFilePath = this.destFilePathTextField.getText();
//        String transferLabel = this.transferLabelTextField.getText();
//
//        if(globusOnlineUtils == null){
//            globusOnlineUtils = new GlobusOnlineUtils(goUserName, goPWD);
//        }
//        TransferFile transferFile = globusOnlineUtils.getTransferFile(sourceEndpoint, destEndpoint, sourceFilePath, destFilePath, transferLabel);
//        globusOnlineUtils.transferFiles(transferFile);
//    }
//
//    private String[] getGOEndpointList(){
//        if (getGoUserName() != null && getGoPWD() != null){
//            globusOnlineUtils = new GlobusOnlineUtils(goUserName, goPWD);
//        }
//        List<String> epList = new ArrayList<String>();
//        try {
//            if (globusOnlineUtils != null){
//                epList = globusOnlineUtils.getEPList();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (APIError apiError) {
//            apiError.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return epList.toArray(new String[]{});
//    }
//
//    /**
//     * Initializes the GUI.
//     */
//    private void initGUI() {
//        this.usernameTextField = new XBayaTextField();
//        this.pwdTextField = new JPasswordField();
//        JButton authenticateButton = new JButton("Authenticate");
//
//        XBayaLabel nameLabel = new XBayaLabel("GO Username", this.usernameTextField);
//        XBayaLabel pwdLabel = new XBayaLabel("GO Password", this.pwdTextField);
//        JLabel authLabel = new JLabel("");
//
//        GridPanel infoPanel = new GridPanel();
//        GridPanel authButtonPanel = new GridPanel();
//        GridPanel otherPanel = new GridPanel();
//
//        authButtonPanel.add(nameLabel.getSwingComponent());
//        authButtonPanel.add(this.usernameTextField.getSwingComponent());
//        authButtonPanel.add(pwdLabel.getSwingComponent());
//        authButtonPanel.add(this.pwdTextField);
//        authButtonPanel.add(authLabel);
//        authButtonPanel.add(authenticateButton);
//
//        authButtonPanel.layout(3,2,GridPanel.WEIGHT_NONE, 1);
//
//        String[] goEndpointList = getGOEndpointList();
//        DefaultComboBoxModel cmbModelJobType1 = new DefaultComboBoxModel(goEndpointList);
//        sourceEndpointTextField = new XBayaComboBox(cmbModelJobType1);
//        sourceEndpointTextField.setEditable(true);
//
//        sourceFilePathTextField = new XBayaTextField();
//        DefaultComboBoxModel cmbModelJobType2 = new DefaultComboBoxModel(goEndpointList);
//        destEndpointTextField = new XBayaComboBox(cmbModelJobType2);
//        destEndpointTextField.setEditable(true);
//        destFilePathTextField = new XBayaTextField();
//        transferLabelTextField = new XBayaTextField();
//
//        XBayaLabel sourceEprLabel = new XBayaLabel("Source Endpoint", sourceEndpointTextField);
//        XBayaLabel sourceFilePathLabel = new XBayaLabel("Source File Path", sourceFilePathTextField);
//        XBayaLabel destEprLabel = new XBayaLabel("Destination Endpoint", destEndpointTextField);
//        XBayaLabel destFilePathLabel = new XBayaLabel("Destination FIle path", destFilePathTextField);
//        XBayaLabel labelTransferLabel = new XBayaLabel("Label This Transfer", destFilePathTextField);
//
//
//        otherPanel.add(sourceEprLabel.getSwingComponent());
//        otherPanel.add(sourceEndpointTextField.getSwingComponent());
//        otherPanel.add(sourceFilePathLabel.getSwingComponent());
//        otherPanel.add(sourceFilePathTextField.getSwingComponent());
//        otherPanel.add(destEprLabel.getSwingComponent());
//        otherPanel.add(destEndpointTextField.getSwingComponent());
//        otherPanel.add(destFilePathLabel.getSwingComponent());
//        otherPanel.add(destFilePathTextField.getSwingComponent());
//        otherPanel.add(labelTransferLabel.getSwingComponent());
//        otherPanel.add(transferLabelTextField.getSwingComponent());
//
//        otherPanel.layout(5, 2, GridPanel.WEIGHT_NONE, 1);
//        infoPanel.add(authButtonPanel);
//
//        infoPanel.add(otherPanel);
//        JPanel buttonPanel = new JPanel();
//        infoPanel.add(buttonPanel);
//
//        infoPanel.layout(3, 1, GridPanel.WEIGHT_NONE, GridPanel.WEIGHT_NONE);
//
//        JButton okButton = new JButton("OK");
//        okButton.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                ok();
//            }
//        });
//
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                hide();
//            }
//        });
//
//        buttonPanel.add(okButton);
//        buttonPanel.add(cancelButton);
//
//        usernameTextField.getSwingComponent().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                setGoUserName(usernameTextField.getText());
//            }
//        }
//        );
//
//        pwdTextField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                setGoPWD(new String(pwdTextField.getPassword()));
//            }
//        }
//        );
//
//        authenticateButton.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                goUserName = usernameTextField.getText();
//                goPWD = new String(pwdTextField.getPassword());
//
//                if (goUserName != null && goPWD != null){
//                    globusOnlineUtils = new GlobusOnlineUtils(usernameTextField.getText(), new String(pwdTextField.getPassword()));
//                    String[] goEndpointList = getGOEndpointList();
//                    DefaultComboBoxModel comboBoxModel1 = new DefaultComboBoxModel(goEndpointList);
//                    DefaultComboBoxModel comboBoxModel2 = new DefaultComboBoxModel(goEndpointList);
//                    sourceEndpointTextField.setModel(comboBoxModel1);
//                    destEndpointTextField.setModel(comboBoxModel2);
//                }
//            }
//        });
//
//        this.dialog = new XBayaDialog(this.engine.getGUI(), "Globus file transfer", infoPanel, buttonPanel);
//        this.dialog.setDefaultButton(okButton);
//    }
//
//    public String getGoUserName() {
//        return goUserName;
//    }
//
//    public void setGoUserName(String goUserName) {
//        this.goUserName = goUserName;
//    }
//
//    public String getGoPWD() {
//        return goPWD;
//    }
//
//    public void setGoPWD(String goPWD) {
//        this.goPWD = goPWD;
//    }
//}
