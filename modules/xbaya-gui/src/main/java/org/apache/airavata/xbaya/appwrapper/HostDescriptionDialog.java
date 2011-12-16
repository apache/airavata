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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;

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

    private Registry registry;

	private JCheckBox chkGobusHost;

	private XBayaLabel globusGateKeeperLabel;

	private XBayaLabel gridFTPLabel;

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

        setHostId(hostId);
        setHostLocation(hostAddress);
        if(isGlobusHostType()) {
        	setGlobusGateKeeperEPR(globusGateKeeperEPR);
        	setGridFTPEPR(gridFTP);
        }

        saveHostDescription();
        hide();
    }

	private boolean isGlobusHostType() {
		return getHostDescription().getType() instanceof GlobusHostType;
	}

    private void setGlobusGateKeeperEPR(String epr) {
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
        globusGateKeeperLabel = new XBayaLabel("Globus Gate Keeper Endpoint", this.globusGateKeeperTextField);
        gridFTPLabel = new XBayaLabel("Grid FTP Endpoint", this.GridFTPTextField);
        chkGobusHost=new JCheckBox("Define this host as a Globus host");
        chkGobusHost.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateGlobusHostTypeAndControls();
			}
        });
        GridPanel infoPanel1 = new GridPanel();
        infoPanel1.add(hostIdLabel);
        infoPanel1.add(this.hostIdTextField);
        infoPanel1.add(hostAddressLabel);
        infoPanel1.add(this.hostAddressTextField);
        infoPanel1.add(chkGobusHost);
        GridPanel infoPanel2 = new GridPanel();
        infoPanel2.add(globusGateKeeperLabel);
        infoPanel2.add(globusGateKeeperTextField);
        infoPanel2.add(gridFTPLabel);
        infoPanel2.add(GridFTPTextField);
        SwingUtil.layoutToGrid(infoPanel1.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);
        SwingUtil.layoutToGrid(infoPanel2.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);

        GridPanel infoPanel = new GridPanel();

        infoPanel.add(infoPanel1);
        infoPanel.add(chkGobusHost);
        infoPanel.add(infoPanel2);
        infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        SwingUtil.layoutToGrid(infoPanel.getSwingComponent(), 3, 1, SwingUtil.WEIGHT_NONE, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        GridPanel buttonPanel = new GridPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        this.dialog = new XBayaDialog(this.engine, "New Host Description", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
        chkGobusHost.setSelected(false);
        updateGlobusHostTypeAndControls();
    }

    public String getHostId() {
        return getHostDescription().getType().getHostName();
    }

    public void setHostId(String hostId) {
        getHostDescription().getType().setHostName(hostId);
        updateDialogStatus();
    }

    public String getHostLocation() {
        return getHostDescription().getType().getHostName();
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
            hostDescription = new HostDescription(GlobusHostType.type);
        }
        return hostDescription;
    }

    public void saveHostDescription() {
        HostDescription desc = getHostDescription();
		getRegistry().saveHostDescription(desc);
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

	private void updateGlobusHostTypeAndControls() {
		if(chkGobusHost.isSelected()) {
			getHostDescription().getType().changeType(GlobusHostType.type);
		}else{
			getHostDescription().getType().changeType(HostDescriptionType.type);
		}
		globusGateKeeperLabel.getSwingComponent().setEnabled(isGlobusHostType());
		globusGateKeeperTextField.setEnabled(isGlobusHostType());
		gridFTPLabel.getSwingComponent().setEnabled(isGlobusHostType());
		GridFTPTextField.setEnabled(isGlobusHostType());
	}
}