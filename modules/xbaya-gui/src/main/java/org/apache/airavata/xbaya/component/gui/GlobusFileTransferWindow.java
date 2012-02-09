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

package org.apache.airavata.xbaya.component.gui;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.globus.GridFTPFileTransferClient;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.globusonline.transfer.APIError;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.json.JSONException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class GlobusFileTransferWindow {
    public static final String GLOBUSONLINE_BASE_URL_V0_10 = "https://transfer.api.globusonline.org/v0.10";

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private XBayaTextField usernameTextField;

    private XBayaTextField caFileTextField;

    private XBayaTextField certFileTextField;

    private XBayaTextField keyFileTextField;

    private XBayaTextField sourceEndpointTextField;

    private XBayaTextField sourceFilePathTextField;

    private XBayaTextField destEndpointTextField;

    private XBayaTextField destFilePathTextField;

    /**
     * @param engine XBaya workflow engine
     */
    public GlobusFileTransferWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void ok() {
        String username = this.usernameTextField.getText();
        String caFile = this.caFileTextField.getText();
        String certFile = this.certFileTextField.getText();
        String keyFile = this.keyFileTextField.getText();

        String sourceEndpoint = this.sourceEndpointTextField.getText();
        String sourceFilePath = this.sourceFilePathTextField.getText();
        String destEndpoint = this.destEndpointTextField.getText();
        String destFilePath = this.destFilePathTextField.getText();

        JSONTransferAPIClient c;
        try {
            c = new JSONTransferAPIClient(username, caFile, certFile, keyFile, GLOBUSONLINE_BASE_URL_V0_10);
        } catch (KeyManagementException e) {
            this.engine.getErrorWindow().error("Key Management Error.", e);
            return;
        } catch (NoSuchAlgorithmException e) {
            this.engine.getErrorWindow().error("No Such Algorithm Error.", e);
            return;
        }
        System.out.println("base url: " + c.getBaseUrl());
        GridFTPFileTransferClient e = new GridFTPFileTransferClient(c);
        try {
            e.transfer(sourceEndpoint, sourceFilePath, destEndpoint, destFilePath);
        } catch (IOException e1) {
            this.engine.getErrorWindow().error("IO Error.", e1);
            return;
        } catch (JSONException e1) {
            this.engine.getErrorWindow().error("JSON Error.", e1);
            return;
        } catch (GeneralSecurityException e1) {
            this.engine.getErrorWindow().error("Key Management Error.", e1);
            return;
        } catch (APIError apiError) {
            this.engine.getErrorWindow().error("Globus Transfer API Calling Error.", apiError);
            return;
        }

        // TODO: should display a message whether the transfer was successful/unsuccessful
        hide();

    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.usernameTextField = new XBayaTextField();
        this.caFileTextField = new XBayaTextField();
        this.certFileTextField = new XBayaTextField();
        this.keyFileTextField = new XBayaTextField();
        this.sourceEndpointTextField = new XBayaTextField();
        this.sourceFilePathTextField = new XBayaTextField();
        this.destEndpointTextField = new XBayaTextField();
        this.destFilePathTextField = new XBayaTextField();

        // Setting some sample values when the Window is loaded
        /*this.usernameTextField.setText("heshan");
        this.caFileTextField
                .setText("/home/heshan/Dev/globusonline/transfer-api-client-java.git/trunk/ca/gd-bundle_ca.cert");
        this.certFileTextField.setText("/tmp/x509up_u780936");
        this.keyFileTextField.setText("/tmp/x509up_u780936");
        this.sourceEndpointTextField.setText("xsede#ranger");
        this.sourceFilePathTextField.setText("~/tmp.log");
        this.destEndpointTextField.setText("xsede#trestles");
        this.destFilePathTextField.setText("~/tmp.log.copy");*/

        XBayaLabel nameLabel = new XBayaLabel("Username", this.usernameTextField);
        XBayaLabel caFileLabel = new XBayaLabel("CA File", this.caFileTextField);
        XBayaLabel certFileLabel = new XBayaLabel("Certificate File", this.certFileTextField);
        XBayaLabel keyFileLabel = new XBayaLabel("Key File", this.keyFileTextField);
        XBayaLabel sourceEprLabel = new XBayaLabel("Source Endpoint", this.sourceEndpointTextField);
        XBayaLabel sourceFilePathLabel = new XBayaLabel("Source File Path", this.sourceFilePathTextField);
        XBayaLabel destEprLabel = new XBayaLabel("Destination Endpoint", this.destEndpointTextField);
        XBayaLabel destFilePathLabel = new XBayaLabel("Destination FIle path", this.destFilePathTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(nameLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.add(caFileLabel);
        infoPanel.add(this.caFileTextField);
        infoPanel.add(certFileLabel);
        infoPanel.add(certFileTextField);
        infoPanel.add(keyFileLabel);
        infoPanel.add(this.keyFileTextField);
        infoPanel.add(sourceEprLabel);
        infoPanel.add(this.sourceEndpointTextField);
        infoPanel.add(sourceFilePathLabel);
        infoPanel.add(this.sourceFilePathTextField);
        infoPanel.add(destEprLabel);
        infoPanel.add(this.destEndpointTextField);
        infoPanel.add(destFilePathLabel);
        infoPanel.add(this.destFilePathTextField);
        infoPanel.layout(8, 2, GridPanel.WEIGHT_NONE, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Globus file transfer", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}
