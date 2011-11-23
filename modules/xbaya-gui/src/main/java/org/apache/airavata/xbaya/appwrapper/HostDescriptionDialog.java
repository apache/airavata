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

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

public class HostDescriptionDialog extends JDialog {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private XBayaTextField hostIdTextField;

    private XBayaTextField hostAddressTextField;

    private XBayaTextField globusGateKeeperTextField;

    private XBayaTextField GridFTPTextField;

    private HostDescription hostDescription;

    private GlobusHostType globusHostType;

    private boolean hostCreated = false;

    private boolean isGlobusHostCreated = false;

    private Registry registry;

    /**
     * @param engine XBaya workflow engine
     */
    public HostDescriptionDialog(XBayaEngine engine) {
        this.engine = engine;
        setRegistry(engine.getConfiguration().getJcrComponentRegistry().getRegistry());
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        this.dialog.show();
    }

    public void hide() {
        this.dialog.hide();
    }

    private void ok() {
        String hostId = this.hostIdTextField.getText();
        String hostAddress = this.hostAddressTextField.getText();
        String globusGateKeeperEPR = this.globusGateKeeperTextField.getText();
        String gridFTP = this.GridFTPTextField.getText();

        if((globusGateKeeperEPR != null) || (gridFTP != null)){
            isGlobusHostCreated = true;
        }

        // TODO the logic here

        setHostId(hostId);
        setHostLocation(hostAddress);
        if(globusGateKeeperEPR != null) {
          setGlobusGateKeeperEPR(globusGateKeeperEPR);
        }
        if(gridFTP != null) {
          setGridFTPEPR(globusGateKeeperEPR);
        }

        saveHostDescription();
        hide();
    }

    private void setGlobusGateKeeperEPR(String epr) {
        hostDescription.getType().changeType(GlobusHostType.type);
            ((GlobusHostType)hostDescription.getType()).addGlobusGateKeeperEndPoint(epr);
    }

    private String[] getGlobusGateKeeperEPR(String epr) {
        if (hostDescription.getType() instanceof GlobusHostType) {
            return ((GlobusHostType)hostDescription.getType()).getGlobusGateKeeperEndPointArray();
        } else {
            return null;
        }
    }

    private void setGridFTPEPR(String epr) {
        hostDescription.getType().changeType(GlobusHostType.type);
        ((GlobusHostType)hostDescription.getType()).addGridFTPEndPoint(epr);
    }

    private String[] getGridFTPEPR() {
        if (hostDescription.getType() instanceof GlobusHostType) {
            return ((GlobusHostType)hostDescription.getType()).getGridFTPEndPointArray();
        } else {
            return null;
        }
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.hostIdTextField = new XBayaTextField();
        this.hostAddressTextField = new XBayaTextField();
        this.globusGateKeeperTextField = new XBayaTextField();
        this.GridFTPTextField = new XBayaTextField();

        XBayaLabel hostIdLabel = new XBayaLabel("Host ID", this.hostIdTextField);
        XBayaLabel hostAddressLabel = new XBayaLabel("Host Address", this.hostAddressTextField);
        XBayaLabel globusGateKeeperLabel = new XBayaLabel("Gloubus Gate Keeper Endpoint", this.globusGateKeeperTextField);
        XBayaLabel gridFTPLabel = new XBayaLabel("Grid FTP Endpoint", this.GridFTPTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(hostIdLabel);
        infoPanel.add(this.hostIdTextField);
        infoPanel.add(hostAddressLabel);
        infoPanel.add(this.hostAddressTextField);
        infoPanel.add(globusGateKeeperLabel);
        infoPanel.add(globusGateKeeperTextField);
        infoPanel.add(gridFTPLabel);
        infoPanel.add(this.GridFTPTextField);
        infoPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);

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

        this.dialog = new XBayaDialog(this.engine, "New Host Description", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    public String getHostId() {
        return getHostDescription().getType().getHostName();
    }

    public void setHostId(String hostId) {
        getHostDescription().getType().setHostName(hostId);
        updateDialogStatus();
    }

    public String getHostLocation() {
        return getHostDescription().getType().getHostAddress();
    }

    public void setHostLocation(String hostLocation) {
        getHostDescription().getType().setHostAddress(hostLocation);
        updateDialogStatus();
    }

    private void validateDialog() throws Exception {
        if (getHostId() == null || getHostId().trim().equals("")) {
            throw new Exception("Id of the host cannot be empty!!!");
        }

        HostDescription hostDescription2 = null;
        try {
            hostDescription2 = getRegistry().getHostDescription(Pattern.quote(getHostId()));
        } catch (RegistryException e) {
            throw e;
        }
        if (hostDescription2 != null) {
            throw new Exception("Host descriptor with the given id already exists!!!");
        }

        if (getHostLocation() == null || getHostLocation().trim().equals("")) {
            throw new Exception("Host location/ip cannot be empty!!!");
        }
    }

    private void updateDialogStatus() {
        String message = null;
        try {
            validateDialog();
        } catch (Exception e) {
            message = e.getLocalizedMessage();
        }
        //okButton.setEnabled(message == null);
        //setError(message);
    }

/*    public void close() {
        getDialog().setVisible(false);
    }*/

    public boolean isHostCreated() {
        return hostCreated;
    }

    public void setHostCreated(boolean hostCreated) {
        this.hostCreated = hostCreated;
    }

    public HostDescription getHostDescription() {
        if (hostDescription == null) {
            if (isGlobusHostCreated) {
                hostDescription = new HostDescription(GlobusHostType.type);
            } else {
                hostDescription = new HostDescription();
            }
        }
        return hostDescription;
    }

    public void saveHostDescription() {
        getRegistry().saveHostDescription(getHostDescription());
        setHostCreated(true);
    }

/*    private void setError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().equals("")) {
            lblError.setText("");
        } else {
            lblError.setText(errorMessage.trim());
        }
    }*/

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}