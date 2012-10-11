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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class HostDescriptionDialog extends JDialog {

	private static final long serialVersionUID = -2910634296292034085L;

    private XBayaTextField hostIdTextField;

    private XBayaTextField hostAddressTextField;

    private XBayaTextField globusGateKeeperTextField;

    private XBayaTextField GridFTPTextField;

    private HostDescription hostDescription;

    private boolean hostCreated = false;

    private AiravataRegistry2 registry;

	private XBayaLabel globusGateKeeperLabel;

	private XBayaLabel gridFTPLabel;

    private JLabel lblError;

    private String hostId;

    private JButton okButton;
    
    private boolean newHost;
    
    private HostDescription originalHostDescription;
    
//    private XBayaEngine engine;

	private JComboBox cmbResourceProtocol;

	private GridPanel infoPanel2;
	
	private static final String REMOTE_PROTOCOL_STR_LOCAL="Local";
	private static final String REMOTE_PROTOCOL_STR_GLOBUS="Globus";
	private static final String REMOTE_PROTOCOL_STR_UNICORE="Unicore";
	private static final String REMOTE_PROTOCOL_STR_AMAZON_EC2="Amazon EC2";
	private static final String REMOTE_PROTOCOL_STR_HADOOP="Hadoop";
	
    
    public HostDescriptionDialog(AiravataRegistry2 registry) {
    	this(registry,true,null);
    }

    /**
     *
     * @param registry
     * @param newHost
     * @param originalHostDescription
     */
    public HostDescriptionDialog(AiravataRegistry2 registry, boolean newHost, HostDescription originalHostDescription) {
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
        if(isGlobusHostType()) {
        	String globusGateKeeperEPR = this.globusGateKeeperTextField.getText();
            String gridFTP = this.GridFTPTextField.getText();
        	setGlobusGateKeeperEPR(globusGateKeeperEPR);
        	setGridFTPEPR(gridFTP);
        }

        saveHostDescription();
        close();
    }

	private boolean isGlobusHostType() {
		return getHostDescription().getType() instanceof GlobusHostType;
	}

    private void setGlobusGateKeeperEPR(String epr) {
        ((GlobusHostType)hostDescription.getType()).addGlobusGateKeeperEndPoint(epr);
    }

    private void setGridFTPEPR(String epr) {
        ((GlobusHostType)hostDescription.getType()).addGridFTPEndPoint(epr);
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
        cmbResourceProtocol = new JComboBox(new String[]{REMOTE_PROTOCOL_STR_LOCAL,REMOTE_PROTOCOL_STR_GLOBUS,REMOTE_PROTOCOL_STR_UNICORE,REMOTE_PROTOCOL_STR_AMAZON_EC2, REMOTE_PROTOCOL_STR_HADOOP});
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
        if (GridFTPTextField==null) {
			this.GridFTPTextField = new XBayaTextField();
			this.globusGateKeeperTextField = new XBayaTextField();
			globusGateKeeperLabel = new XBayaLabel("GRAM Endpoint", this.globusGateKeeperTextField);        
	        gridFTPLabel = new XBayaLabel("Grid FTP Endpoint", this.GridFTPTextField);
		}
        globusPanel.add(globusGateKeeperLabel);
        globusPanel.add(globusGateKeeperTextField);
        globusPanel.add(gridFTPLabel);
        globusPanel.add(GridFTPTextField);
        SwingUtil.layoutToGrid(globusPanel.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);
        return globusPanel;
	}

    private String arrayToString(String[] list) {
    	String result="";
		for (String s : list) {
			if (result.equals("")){
				result=s;
			}else{
				result+=","+s;
			}
		}
		return result;
	}
    private void loadData() {
    	HostDescriptionType t = getOriginalHostDescription().getType();
    	hostIdTextField.setText(t.getHostName());
		hostAddressTextField.setText(t.getHostAddress());
		boolean isGlobus = t instanceof GlobusHostType;
		if (isGlobus){
			cmbResourceProtocol.setSelectedItem(REMOTE_PROTOCOL_STR_GLOBUS);
			globusGateKeeperTextField.setText(arrayToString(((GlobusHostType) t).getGlobusGateKeeperEndPointArray()));
			GridFTPTextField.setText(arrayToString(((GlobusHostType) t).getGridFTPEndPointArray()));
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
		    hostDescription2 = getRegistry().getHostDescriptor(hostName);
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
        	if (getRegistry().isHostDescriptorExists(desc.getType().getHostName())){
        		getRegistry().updateHostDescriptor(desc);	
        	}else{
        		getRegistry().addHostDescriptor(desc);
        	}
			setHostCreated(true);
		} catch (DescriptorAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		setHostCreated(false);
    }

    public AiravataRegistry2 getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry2 registry) {
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
			}else if (selectedProtocol.equals(REMOTE_PROTOCOL_STR_GLOBUS)){
				getHostDescription().getType().changeType(GlobusHostType.type);
				infoPanel2.add(createGlobusRemoteProtocolPanel());
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