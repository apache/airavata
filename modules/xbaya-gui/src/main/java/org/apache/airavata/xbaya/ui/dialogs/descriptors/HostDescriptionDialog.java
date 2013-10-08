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

package org.apache.airavata.xbaya.ui.dialogs.descriptors;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.api.exception.DescriptorAlreadyExistsException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.schemas.gfac.Ec2HostType;
import org.apache.airavata.schemas.gfac.ExportProperties;
import org.apache.airavata.schemas.gfac.ExportProperties.Name;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class HostDescriptionDialog extends JDialog {

	private static final long serialVersionUID = -2910634296292034085L;

    private XBayaTextField hostIdTextField;

    private XBayaTextField hostAddressTextField;

    private XBayaTextField gateKeeperTextField;

    private XBayaTextField gridFTPTextField;

    private HostDescription hostDescription;

    private boolean hostCreated = false;

    private AiravataAPI registry;

	private XBayaLabel gateKeeperLabel;

	private XBayaLabel gridFTPLabel;

    private JLabel lblError;

    private String hostId;

    private JButton okButton;

    private boolean newHost;

    private HostDescription originalHostDescription;

//    private XBayaEngine engine;

	private JComboBox cmbResourceProtocol;

	private GridPanel infoPanel2;

	private XBayaTextField exportsTextField;

	private XBayaTextField preJobCommandsTextField;

	private XBayaTextField postJobCommandsTextField;

	private XBayaLabel postJobCommandsTextFieldLabel;

	private XBayaLabel preJobCommandsLabel;

	private XBayaLabel exportsLabel;

	private XBayaTextField fileEndPointPrefixTextField;

	private XBayaLabel fileEndPointPrefixLabel;

	private JCheckBox hpcResourceCheckBoxField;

	private static final String REMOTE_PROTOCOL_STR_LOCAL="Local";
	private static final String REMOTE_PROTOCOL_STR_SSH="SSH";
	private static final String REMOTE_PROTOCOL_STR_GLOBUS="Globus";
	private static final String REMOTE_PROTOCOL_STR_UNICORE="Unicore";
	private static final String REMOTE_PROTOCOL_STR_AMAZON_EC2="Amazon EC2";
	private static final String REMOTE_PROTOCOL_STR_HADOOP="Hadoop";
	private static final String REMOTE_PROTOCOL_GSI_SSH="GSI-SSH";


    public HostDescriptionDialog(AiravataAPI registry, JFrame parent) {
    	this(registry,true,null, parent);
    }

    /**
     *
     * @param registry
     * @param newHost
     * @param originalHostDescription
     */
    public HostDescriptionDialog(AiravataAPI registry, boolean newHost, HostDescription originalHostDescription, JFrame parent) {
        super(parent);
        setNewHost(newHost);
        setOriginalHostDescription(originalHostDescription);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
//                if (isNewHost()) {
//					String baseName = "Host";
//					int i = 1;
//					String defaultName = baseName + i;
//					try {
//						while (getRegistry().getServiceDescription(defaultName) != null) {
//							defaultName = baseName + (++i);
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					hostIdTextField.setText(defaultName);
//				}
            }
        });
        setRegistry(registry);
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void open() {
        setModal(true);
        setLocationRelativeTo(getOwner());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
    }

    public void close() {
    	setVisible(false);
    }

    private void ok() {
        hostId = this.hostIdTextField.getText();
        String hostAddress = this.hostAddressTextField.getText();

        setHostId(hostId);
        setHostLocation(hostAddress);
        HostDescriptionType host = getHostDescription().getType();
        if(host instanceof GlobusHostType) {
        	((GlobusHostType)hostDescription.getType()).addGlobusGateKeeperEndPoint(this.gateKeeperTextField.getText());
            ((GlobusHostType)hostDescription.getType()).addGridFTPEndPoint(this.gridFTPTextField.getText());
        }else if (host instanceof UnicoreHostType){
        	((UnicoreHostType)hostDescription.getType()).addUnicoreBESEndPoint(this.gateKeeperTextField.getText());
            ((UnicoreHostType)hostDescription.getType()).addGridFTPEndPoint(this.gridFTPTextField.getText());
        }else if (host instanceof SSHHostType){
        	while(((SSHHostType)hostDescription.getType()).getFileEndPointPrefixArray().length>0){
        		((SSHHostType)hostDescription.getType()).removeFileEndPointPrefix(0);
        	}
        	String[] prefixes = StringUtil.getElementsFromString(this.fileEndPointPrefixTextField.getText());
        	for (String prefix : prefixes) {
        		((SSHHostType)hostDescription.getType()).addNewFileEndPointPrefix().setStringValue(prefix);
			}
            ((SSHHostType)hostDescription.getType()).setHpcResource(hpcResourceCheckBoxField.isSelected());
        }else if (host instanceof GsisshHostType){
        	String[] exports = StringUtil.getElementsFromString(exportsTextField.getText());
        	ExportProperties exportsElement = ((GsisshHostType)hostDescription.getType()).addNewExports();
        	for (String export : exports) {
        		String[] nameVal = StringUtil.getElementsFromString(export,"=",StringUtil.QUOTE);
        		if (nameVal.length>0){
            		Name name = exportsElement.addNewName();
        			name.setStringValue(nameVal[0]);
        			if (nameVal.length>1){
        				name.setValue(nameVal[1]);
        			}
        		}
			}
            ((GsisshHostType)hostDescription.getType()).addNewPreJobCommands().setCommandArray(StringUtil.getElementsFromString(this.preJobCommandsTextField.getText()));
            ((GsisshHostType)hostDescription.getType()).addNewPostJobCommands().setCommandArray(StringUtil.getElementsFromString(this.postJobCommandsTextField.getText()));
        }
        saveHostDescription();
        close();
    }

    private GridPanel createPanelWithMessage(String message){
    	GridPanel gridPanel = new GridPanel();
    	JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
		gridPanel.add(lblMessage);
		lblMessage.setFont(new Font("Tahoma", Font.ITALIC, 11));
    	gridPanel.layout(1,1, 0,0);
    	return gridPanel;
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
    	setBounds(100, 100, 400, 350);
    	setModal(true);
        setLocationRelativeTo(null);
        if (isNewHost()) {
			setTitle("Register Host");
		}else{
			setTitle("Update Host: "+getOriginalHostDescription().getType().getHostName());
		}
		this.hostIdTextField = new XBayaTextField();
        this.hostAddressTextField = new XBayaTextField();

        XBayaLabel hostIdLabel = new XBayaLabel("Host ID", this.hostIdTextField);
        XBayaLabel hostAddressLabel = new XBayaLabel("Host Address", this.hostAddressTextField);
        cmbResourceProtocol = new JComboBox(new String[]{REMOTE_PROTOCOL_STR_LOCAL,REMOTE_PROTOCOL_STR_SSH,REMOTE_PROTOCOL_STR_GLOBUS,REMOTE_PROTOCOL_STR_UNICORE,REMOTE_PROTOCOL_STR_AMAZON_EC2, REMOTE_PROTOCOL_STR_HADOOP, REMOTE_PROTOCOL_GSI_SSH});
        JLabel lblResourceProtocol = new JLabel("Resource Protocol");
        GridPanel pnlResourceProtocolSelection=new GridPanel();
        pnlResourceProtocolSelection.add(lblResourceProtocol);
        pnlResourceProtocolSelection.add(cmbResourceProtocol);
        pnlResourceProtocolSelection.layout(1, 2, 0, 1);
        cmbResourceProtocol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateRemoteProtocolTypeAndControls();
            }
        });
        hostIdTextField.getSwingComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            	updateDialogStatus();
            }
        });
        hostAddressTextField.getSwingComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            	updateDialogStatus();
            }
        });
        GridPanel infoPanel1 = new GridPanel();
        infoPanel1.add(hostIdLabel);
        infoPanel1.add(this.hostIdTextField);
        infoPanel1.add(hostAddressLabel);
        infoPanel1.add(this.hostAddressTextField);
//        infoPanel1.add(chkGobusHost);
        infoPanel2 = new GridPanel();
        infoPanel2.add(createPanelWithMessage("Initializing..."));
        SwingUtil.layoutToGrid(infoPanel1.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);
        SwingUtil.layoutToGrid(infoPanel2.getSwingComponent(), 1, 1, SwingUtil.WEIGHT_NONE, 1);

        GridPanel infoPanel = new GridPanel();

        infoPanel.add(infoPanel1);
        infoPanel.add(pnlResourceProtocolSelection);
        infoPanel.add(infoPanel2);
        infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        SwingUtil.layoutToGrid(infoPanel.getSwingComponent(), 3, 1, SwingUtil.WEIGHT_NONE, 0);

        GridPanel buttonPanel = new GridPanel();
        lblError = new JLabel();
        lblError.setForeground(Color.RED);
        buttonPanel.add(lblError);
        okButton = new JButton("Save");
        if (!isNewHost()) {
			okButton.setText("Update");
		}
		okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.layout(1,3,SwingUtil.WEIGHT_NONE,0);
        buttonPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        getContentPane().add(infoPanel.getSwingComponent());
        getContentPane().add(buttonPanel.getSwingComponent());

        SwingUtil.layoutToGrid(getContentPane(), 2, 1, 0, 0);

        getRootPane().setDefaultButton(okButton);
        cmbResourceProtocol.setSelectedIndex(0);
        updateRemoteProtocolTypeAndControls();
        if (!isNewHost()) {
			loadData();
		}
//        SwingUtil.addPlaceHolder(hostIdTextField.getSwingComponent(), "[unique name for the host]");
//        SwingUtil.addPlaceHolder(hostAddressTextField.getSwingComponent(), "[a valid host address, eg: myhost.com, 127.0.0.1]");
//        SwingUtil.addPlaceHolder(GridFTPTextField.getSwingComponent(), "[List of grid ftp endpoints]");
//        SwingUtil.addPlaceHolder(globusGateKeeperTextField.getSwingComponent(), "[List of globus gate keeper endpoints]");
        updateDialogStatus();
    }

	private GridPanel createGlobusRemoteProtocolPanel() {
		GridPanel globusPanel = new GridPanel();
        if (gridFTPTextField==null) {
			this.gridFTPTextField = new XBayaTextField();
			this.gateKeeperTextField = new XBayaTextField();
			gateKeeperLabel = new XBayaLabel("GRAM Endpoint", this.gateKeeperTextField);
	        gridFTPLabel = new XBayaLabel("Grid FTP Endpoint", this.gridFTPTextField);
		}
        globusPanel.add(gateKeeperLabel);
        globusPanel.add(gateKeeperTextField);
        globusPanel.add(gridFTPLabel);
        globusPanel.add(gridFTPTextField);
        SwingUtil.layoutToGrid(globusPanel.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);
        return globusPanel;
	}
	
	private GridPanel createSSHRemoteProtocolPanel() {
		GridPanel globusPanel = new GridPanel();
        if (exportsTextField==null) {
			this.fileEndPointPrefixTextField = new XBayaTextField();
			this.hpcResourceCheckBoxField = new JCheckBox("HPC Resource");
			fileEndPointPrefixLabel = new XBayaLabel("File Endpoint Prefix", this.exportsTextField);
		}
        globusPanel.add(fileEndPointPrefixLabel);
        globusPanel.add(fileEndPointPrefixTextField);
        globusPanel.add(hpcResourceCheckBoxField);
        SwingUtil.layoutToGrid(globusPanel.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);
        return globusPanel;
	}
	
	private GridPanel createGSISSHRemoteProtocolPanel() {
		GridPanel globusPanel = new GridPanel();
        if (exportsTextField==null) {
			this.exportsTextField = new XBayaTextField();
			this.preJobCommandsTextField = new XBayaTextField();
			this.postJobCommandsTextField = new XBayaTextField();
			exportsLabel = new XBayaLabel("Exports", this.exportsTextField);
			preJobCommandsLabel = new XBayaLabel("Pre-job Commands", this.preJobCommandsTextField);
			postJobCommandsTextFieldLabel = new XBayaLabel("Post-job Commands", this.postJobCommandsTextField);
		}
        globusPanel.add(exportsLabel);
        globusPanel.add(exportsTextField);
        globusPanel.add(preJobCommandsLabel);
        globusPanel.add(preJobCommandsTextField);
        globusPanel.add(postJobCommandsTextFieldLabel);
        globusPanel.add(postJobCommandsTextField);
        SwingUtil.layoutToGrid(globusPanel.getSwingComponent(), 3, 2, SwingUtil.WEIGHT_NONE, 1);
        return globusPanel;
	}

	private GridPanel createUnicoreRemoteProtocolPanel() {
		GridPanel globusPanel = new GridPanel();
        if (gridFTPTextField==null) {
			this.gridFTPTextField = new XBayaTextField();
			this.gateKeeperTextField = new XBayaTextField();
			gateKeeperLabel = new XBayaLabel("Unicore Endpoint", this.gateKeeperTextField);
	        gridFTPLabel = new XBayaLabel("GridFTP Endpoint", this.gridFTPTextField);
		}
        globusPanel.add(gateKeeperLabel);
        globusPanel.add(gateKeeperTextField);
        globusPanel.add(gridFTPLabel);
        globusPanel.add(gridFTPTextField);
        SwingUtil.layoutToGrid(globusPanel.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);
        return globusPanel;
	}
    private void loadData() {
    	HostDescriptionType t = getOriginalHostDescription().getType();
    	hostIdTextField.setText(t.getHostName());
		hostAddressTextField.setText(t.getHostAddress());
		if (t instanceof GlobusHostType){
			cmbResourceProtocol.setSelectedItem(REMOTE_PROTOCOL_STR_GLOBUS);
			gateKeeperTextField.setText(StringUtil.createDelimiteredString(((GlobusHostType) t).getGlobusGateKeeperEndPointArray()));
			gridFTPTextField.setText(StringUtil.createDelimiteredString(((GlobusHostType) t).getGridFTPEndPointArray()));
		}else if (t instanceof SSHHostType){
			cmbResourceProtocol.setSelectedItem(REMOTE_PROTOCOL_STR_SSH);
			fileEndPointPrefixTextField.setText(StringUtil.createDelimiteredString(((SSHHostType)t).getFileEndPointPrefixArray()));
			hpcResourceCheckBoxField.setSelected(((SSHHostType)t).getHpcResource());
		}else if (t instanceof UnicoreHostType){
			cmbResourceProtocol.setSelectedItem(REMOTE_PROTOCOL_STR_UNICORE);
			gateKeeperTextField.setText(StringUtil.createDelimiteredString(((UnicoreHostType) t).getUnicoreBESEndPointArray()));
			gridFTPTextField.setText(StringUtil.createDelimiteredString(((UnicoreHostType) t).getGridFTPEndPointArray()));
		}else if (t instanceof Ec2HostType){
			cmbResourceProtocol.setSelectedItem(REMOTE_PROTOCOL_STR_AMAZON_EC2);
		}else if (t instanceof GsisshHostType){
			cmbResourceProtocol.setSelectedItem(REMOTE_PROTOCOL_GSI_SSH);
			Name[] nameArray = ((GsisshHostType) t).getExports().getNameArray();
			List<String> arr=new ArrayList<String>();
			for (Name name : nameArray) {
				arr.add(name.getStringValue()+"="+StringUtil.quoteString(name.getValue(),"="));
			}
			exportsTextField.setText(StringUtil.createDelimiteredString(arr.toArray(new String[]{})));
			preJobCommandsTextField.setText(StringUtil.createDelimiteredString(((GsisshHostType) t).getPreJobCommands().getCommandArray()));
			postJobCommandsTextField.setText(StringUtil.createDelimiteredString(((GsisshHostType) t).getPostJobCommands().getCommandArray()));
		}
		hostIdTextField.setEditable(isNewHost());
		updateRemoteProtocolTypeAndControls();
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
        if (isNewHost()) {
			String hostName = this.hostIdTextField.getText();
			if (hostName == null
					|| hostName.trim().equals("")) {
				throw new Exception("Id of the host cannot be empty!!!");
			}
			HostDescription hostDescription2 = null;
		    hostDescription2 = getRegistry().getApplicationManager().getHostDescription(hostName);
			if (hostDescription2 != null) {
				throw new Exception(
						"Host descriptor with the given id already exists!!!");
			}
		}
        String hostAddress = this.hostAddressTextField.getText();
		if (hostAddress == null || hostAddress.trim().equals("")) {
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
        okButton.setEnabled(message == null);
        setError(message);
    }

    private void setError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().equals("")) {
            lblError.setText("");
        } else {
            lblError.setText(errorMessage.trim());
        }

    }

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
        try {
        	if (getRegistry().getApplicationManager().isHostDescriptorExists(desc.getType().getHostName())){
        		getRegistry().getApplicationManager().updateHostDescriptor(desc);
        	}else{
        		getRegistry().getApplicationManager().addHostDescription(desc);
        	}
			setHostCreated(true);
		}catch (DescriptorAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (AiravataAPIInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
    }

    public AiravataAPI getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataAPI registry) {
        this.registry = registry;
    }
    String previousProtocol=null;
	private void updateRemoteProtocolTypeAndControls() {
		String selectedProtocol=cmbResourceProtocol.getSelectedItem().toString();
		if (previousProtocol==null || !previousProtocol.equals(selectedProtocol)){
			infoPanel2.getContentPanel().removeAll();
			if (selectedProtocol.equals(REMOTE_PROTOCOL_STR_LOCAL)){
				getHostDescription().getType().changeType(HostDescriptionType.type);
				infoPanel2.add(createPanelWithMessage("No configurations needed."));
			}else if (selectedProtocol.equals(REMOTE_PROTOCOL_STR_SSH)){
				getHostDescription().getType().changeType(SSHHostType.type);
				infoPanel2.add(createSSHRemoteProtocolPanel());
			}else if (selectedProtocol.equals(REMOTE_PROTOCOL_STR_GLOBUS)){
				getHostDescription().getType().changeType(GlobusHostType.type);
				infoPanel2.add(createGlobusRemoteProtocolPanel());
			}else if (selectedProtocol.equals(REMOTE_PROTOCOL_STR_UNICORE)){
				getHostDescription().getType().changeType(UnicoreHostType.type);
				infoPanel2.add(createUnicoreRemoteProtocolPanel());
			}else if (selectedProtocol.equals(REMOTE_PROTOCOL_STR_AMAZON_EC2)){
				getHostDescription().getType().changeType(Ec2HostType.type);
				infoPanel2.add(createPanelWithMessage("No configurations needed."));
			}else if (selectedProtocol.equals(REMOTE_PROTOCOL_GSI_SSH)){
				getHostDescription().getType().changeType(GsisshHostType.type);
				infoPanel2.add(createGSISSHRemoteProtocolPanel());
			}else{
				infoPanel2.add(createPanelWithMessage("Not supported."));
			}
			infoPanel2.getContentPanel().setBorder(BorderFactory.createEtchedBorder());
			infoPanel2.getContentPanel().updateUI();
			infoPanel2.layout(1, 1,0,0);
		}
	}

	public boolean isNewHost() {
		return newHost;
	}

	public void setNewHost(boolean newHost) {
		this.newHost = newHost;
	}

	public HostDescription getOriginalHostDescription() {
		return originalHostDescription;
	}

	public void setOriginalHostDescription(HostDescription originalHostDescription) {
		this.originalHostDescription = originalHostDescription;
	}

//	public XBayaEngine getEngine() {
//		return engine;
//	}
//
//	public void setEngine(XBayaEngine engine) {
//		this.engine = engine;
//	}
}