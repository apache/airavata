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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.xbaya.ui.menues.MenuIcons;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLinkButton;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.xmlbeans.XmlException;
import org.ggf.schemas.jsdl.x2006.x07.jsdlHpcpa.HPCProfileApplicationDocument;

public class HostDeploymentDialog extends JDialog implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = -2745085755585610025L;
    private XBayaTextField txtExecPath;
    private XBayaTextField txtTempDir;

    private AiravataAPI registry;
    private ApplicationDescription shellApplicationDescription;
    private JLabel lblError;
    private boolean applcationDescCreated = false;
    private JButton okButton;

    private String hostName;
    private JComboBox cmbHostName;

	private JButton btnHostAdvanceOptions;
	private boolean newDescriptor;
	private ApplicationDescription originalDescription;
	private String originalHost; 
	private JButton btnTmpDirBrowse;
	private JButton btnExecBrowse;
	private List<String> existingHostList;
	
    /**
     * Create the dialog.
     */
    public HostDeploymentDialog(AiravataAPI registry, boolean newDescriptor, ApplicationDescription originalDescription, String originalHost, List<String> existingHostList) {
    	setNewDescriptor(newDescriptor);
    	setOriginalDescription(originalDescription);
    	setOriginalHost(originalHost);
        setRegistry(registry);
        setExistingHostList(existingHostList);
        iniGUI();
    }

    public void open() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected HostDeploymentDialog getDialog() {
        return this;
    }
    
    private void iniGUI() {
        if (isNewDescriptor()) {
			setTitle("New Application Deployment");
		}else{
			setTitle("Update Application Deployment: "+ getOriginalDescription().getType().getApplicationName().getStringValue());
		}
		setBounds(100, 100, 600, 620);
        setModal(true);
        setLocationRelativeTo(null);
        GridPanel buttonPane = new GridPanel();
        {
            lblError = new JLabel("");
            lblError.setForeground(Color.RED);
            buttonPane.add(lblError);
            if (!isNewDescriptor()){
            	JButton resetButton = new JButton("Reset");
                resetButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadData();
                    }
                });
                buttonPane.add(resetButton);
            }
            {
                okButton = new JButton("Add");
                if (!isNewDescriptor()){
                	okButton.setText("Update");
                }
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveApplicationDescription();
                        close();
                    }
                });
                okButton.setEnabled(false);
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setApplicationDescCreated(false);
                        close();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        {
        	JPanel execPath=new JPanel();
            txtExecPath = new XBayaTextField();
            txtExecPath.getTextField().addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    setExecutablePath(txtExecPath.getText());
                }
            });
            txtExecPath.getTextField().addFocusListener(new FocusAdapter() {
            	@Override
            	public void focusLost(FocusEvent e) {
            		super.focusLost(e);
            		updateTempDirWithExecPath(txtExecPath.getText());
            	}
			});
            txtExecPath.setColumns(10);
            btnExecBrowse=new JButton(MenuIcons.OPEN_ICON);
            btnExecBrowse.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser c = new JFileChooser();
					int rVal = c.showOpenDialog(null);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						txtExecPath.setText(c.getSelectedFile().toString());
						setExecutablePath(txtExecPath.getText());
					}
				}
            });
            execPath.add(txtExecPath.getSwingComponent());
            execPath.add(btnExecBrowse);
            
            setupLayoutForBrowse(execPath,txtExecPath.getSwingComponent());

            JLabel lblExecutablePath = new JLabel("Executable path");
        	JPanel tmpDirPath=new JPanel();

            txtTempDir = new XBayaTextField();
            txtTempDir.getTextField().addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    setTempDir(txtTempDir.getText());
                }
            });
            txtTempDir.setColumns(10);
            btnTmpDirBrowse=new JButton(MenuIcons.OPEN_DIR_ICON);
            btnTmpDirBrowse.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser c = new JFileChooser();
					c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int rVal = c.showOpenDialog(null);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						txtTempDir.setText(c.getSelectedFile().toString());
						setTempDir(txtTempDir.getText());
					}
				}
            });
            JTextField component = txtTempDir.getSwingComponent();
			tmpDirPath.add(component);
            tmpDirPath.add(btnTmpDirBrowse);
            
            setupLayoutForBrowse(tmpDirPath, component);

            JLabel lblTemporaryDirectory = new JLabel("Scratch working directory");

            JButton btnAdvance = new JButton("Advanced application configurations...");
            btnAdvance.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        ApplicationDescriptionAdvancedOptionDialog serviceDescriptionDialog = new ApplicationDescriptionAdvancedOptionDialog(
                                getRegistry(), getShellApplicationDescription());
                        serviceDescriptionDialog.open();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
                    }
                }
            });

            cmbHostName = new JComboBox();
            cmbHostName.addActionListener(this);

            XBayaLabel lblHostName = new XBayaLabel("Application host",cmbHostName);
            XBayaLinkButton lnkNewHost = new XBayaLinkButton("New button");
            lnkNewHost.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(getRegistry(), null);
                        hostDescriptionDialog.setLocationRelativeTo(getContentPane());
                        hostDescriptionDialog.open();

                        if (hostDescriptionDialog.isHostCreated()) {
                        	ProgressMonitor progressMonitor = new ProgressMonitor(getContentPane(), "Host Descriptions", "Refreshing host list..", 0, 200);
                        	int progress=1;
                        	progressMonitor.setProgress(progress++);
                        	while(cmbHostName.getSelectedIndex()==-1 || !cmbHostName.getSelectedItem().toString().equals(hostDescriptionDialog.getHostLocation())){
	                            loadHostDescriptions();
	                            cmbHostName.setSelectedItem(hostDescriptionDialog.getHostLocation());
	                            progressMonitor.setProgress(progress++);
	                            Thread.sleep(50);
                        	}
                        	progressMonitor.setProgress(200);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
                    }
                }
            });
            lnkNewHost.setText("Create new host...");
            lnkNewHost.setHorizontalAlignment(SwingConstants.TRAILING);

            btnHostAdvanceOptions=new JButton("HPC Configuration...");
            btnHostAdvanceOptions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						ApplicationDescriptionHostAdvancedOptionDialog hostAdvancedOptionsDialog = new ApplicationDescriptionHostAdvancedOptionDialog(getRegistry(),getShellApplicationDescription());
                        hostAdvancedOptionsDialog.open();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
                    }
				}
			});
            btnHostAdvanceOptions.setVisible(false);
            GridPanel hostPanel=new GridPanel();
            hostPanel.add(cmbHostName);
//            hostPanel.add(btnHostAdvanceOptions);
            hostPanel.add(new JLabel());
            
            SwingUtil.layoutToGrid(hostPanel.getSwingComponent(), 1, 2, 0, 0);
            
            GridPanel infoPanel1 = new GridPanel();

            
            infoPanel1.add(lblExecutablePath);
            infoPanel1.add(execPath);
            infoPanel1.add(lblTemporaryDirectory);
            infoPanel1.add(tmpDirPath);
            
            GridPanel infoPanel3 = new GridPanel();

            infoPanel3.add(lblHostName);
            infoPanel3.add(hostPanel);
            infoPanel3.add(new JLabel());
            infoPanel3.add(lnkNewHost);
            
            GridPanel infoPanel4=new GridPanel();
//            infoPanel4.add(new JLabel());
            infoPanel4.add(btnHostAdvanceOptions);
            infoPanel4.add(btnAdvance);
            infoPanel4.layout(1, 2, 0, 0);
            
            SwingUtil.layoutToGrid(infoPanel1.getSwingComponent(), 4, 1, SwingUtil.WEIGHT_NONE, 0);
            SwingUtil.layoutToGrid(infoPanel3.getSwingComponent(), 2, 2, SwingUtil.WEIGHT_NONE, 1);

            GridPanel infoPanel = new GridPanel();
            infoPanel.add(new JSeparator());
			infoPanel.add(infoPanel3);
            infoPanel.add(new JSeparator());
            infoPanel.add(infoPanel1);

            infoPanel.add(new JSeparator());
            infoPanel.add(infoPanel4);
            
            SwingUtil.layoutToGrid(infoPanel.getSwingComponent(), 6, 1, SwingUtil.WEIGHT_NONE, 0);
            SwingUtil.layoutToGrid(buttonPane.getSwingComponent(), 1, buttonPane.getContentPanel().getComponentCount(),SwingUtil.WEIGHT_NONE,0);
            getContentPane().add(infoPanel.getSwingComponent());
            getContentPane().add(buttonPane.getSwingComponent());
            
            buttonPane.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
            infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

            SwingUtil.layoutToGrid(getContentPane(), 2, 1, -1, 0);
            loadHostDescriptions();
        }
        setResizable(true);
        getRootPane().setDefaultButton(okButton);
        if (!isNewDescriptor()){
        	loadData();
        }
        pack();
        if (getSize().getWidth()<500){
        	setSize(500, getSize().height);
        }
    }

	private void setupLayoutForBrowse(JPanel tmpDirPath, JTextField component) {
		GridBagLayout layout;
		GridBagConstraints constraints;
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		layout.setConstraints(component, constraints);
		tmpDirPath.setLayout(layout);
	}

    private void loadData(){
    	txtExecPath.setText(getOriginalDescription().getType().getExecutableLocation());
    	setExecutablePath(txtExecPath.getText());
    	txtTempDir.setText(getOriginalDescription().getType().getScratchWorkingDirectory());
    	setTempDir(txtTempDir.getText());

    	cmbHostName.setSelectedItem(getOriginalHost());
    	setHostName(cmbHostName.getSelectedItem().toString());
    	cmbHostName.setEnabled(isNewDescriptor());
    }
    
    private void loadHostDescriptions() {
        cmbHostName.removeAllItems();
        setHostName(null);
        try {
            List<HostDescription> hostDescriptions = getRegistry().getApplicationManager().getAllHostDescriptions();
            for (HostDescription hostDescription : hostDescriptions) {
                if (!isNewDescriptor() || !getExistingHostList().contains(hostDescription.getType().getHostName())) {
					cmbHostName.addItem(hostDescription.getType().getHostName());
				}
            }
        } catch (Exception e) {
            setError(e.getLocalizedMessage());
        }
        updateHostName();
    }

    public ApplicationDescription getShellApplicationDescription() {
        if(shellApplicationDescription == null){
            if (isNewDescriptor()) {
				shellApplicationDescription = new ApplicationDescription();
			}else{
				try {
					shellApplicationDescription= ApplicationDescription.fromXML(getOriginalDescription().toXML());
				} catch (XmlException e) {
					//shouldn't happen (hopefully)
				}
			}
        }
        return shellApplicationDescription;
    }

    public ApplicationDeploymentDescriptionType getApplicationDescriptionType() {
    	return getShellApplicationDescription().getType();
    }

    public String getExecutablePath() {
        return getApplicationDescriptionType().getExecutableLocation();
    }

    public void setExecutablePath(String executablePath) {
    	getApplicationDescriptionType().setExecutableLocation(executablePath);
    	updateTempDirWithExecPath(executablePath);
        updateDialogStatus();
    }

	private void updateTempDirWithExecPath(String executablePath) {
//		if (!executablePath.trim().equals("") && (!txtExecPath.getSwingComponent().isFocusOwner()) && 
//				(getApplicationDescriptionType().getScratchWorkingDirectory()==null || getApplicationDescriptionType().getScratchWorkingDirectory().trim().equalsIgnoreCase(""))){
//    		String temp_location = "workflow_runs";
//			String tempDir = new File(new File(executablePath).getParentFile(),temp_location).toString();
//			txtTempDir.setText(tempDir);
//    		txtTempDir.getSwingComponent().setSelectionStart(tempDir.length()-temp_location.length());
//    		txtTempDir.getSwingComponent().setSelectionEnd(tempDir.length());
//    		setTempDir(txtTempDir.getText());
//    		txtTempDir.getSwingComponent().requestFocus();
//    	}
	}

    public String getTempDir() {
        return getApplicationDescriptionType().getScratchWorkingDirectory();
    }

    public void setTempDir(String tempDir) {
    	getApplicationDescriptionType().setScratchWorkingDirectory(tempDir);
        updateDialogStatus();
    }

    public void close() {
        getDialog().setVisible(false);
    }

    public void saveApplicationDescription() {
		setApplicationDescCreated(true);
    }

    public boolean isApplicationDescCreated() {
        return applcationDescCreated;
    }

    public void setApplicationDescCreated(boolean applicationDescCreated) {
        this.applcationDescCreated = applicationDescCreated;
    }

    private void setError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().equals("")) {
            lblError.setText("");
        } else {
            lblError.setText(errorMessage.trim());
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
	
    private void validateDialog() throws Exception {
        if (getExecutablePath() == null || getExecutablePath().trim().equals("")) {
            throw new Exception("Executable path cannot be empty!!!");
        }

        if (getTempDir() == null || getTempDir().trim().equals("")) {
            throw new Exception("Temporary directory location cannot be empty!!!");
        }

        if (getHostName() == null || getHostName().trim().equals("")) {
            throw new Exception("Please select/create host to bind to this deployment description");
        }

    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        if (hostName != null) {
            HostDescription hostDescription;
            try {
				hostDescription = registry.getApplicationManager().getHostDescription(hostName);
				if (hostDescription.getType() instanceof GlobusHostType) {
				    getShellApplicationDescription().getType().changeType(
				            HPCProfileApplicationDocument.type);
				} else {
				    getShellApplicationDescription().getType().changeType(
				            ApplicationDeploymentDescriptionType.type);
				}
				btnHostAdvanceOptions.setVisible(hostDescription.getType() instanceof GlobusHostType);
				String hostAddress = hostDescription.getType().getHostAddress();
				boolean isLocal = isLocalAddress(hostAddress);
				btnExecBrowse.setVisible(isLocal);
				btnTmpDirBrowse.setVisible(isLocal);
			}  catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            }
        }
        updateDialogStatus();
    }

	private boolean isLocalAddress(String hostAddress) {
		return hostAddress.equalsIgnoreCase("localhost") || hostAddress.equalsIgnoreCase("127.0.0.1");
	}

    private void updateHostName() {
        if (cmbHostName.getSelectedItem() != null) {
            setHostName(cmbHostName.getSelectedItem().toString());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cmbHostName) {
            updateHostName();
        }
        if (e.getSource() == txtExecPath) {
            setExecutablePath(txtExecPath.getText());
        }
        if (e.getSource() == txtTempDir) {
            setTempDir(txtTempDir.getText());
        }
    }

    public AiravataAPI getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataAPI registry) {
        this.registry = registry;
    }

	public boolean isNewDescriptor() {
		return newDescriptor;
	}

	public void setNewDescriptor(boolean newDescriptor) {
		this.newDescriptor = newDescriptor;
	}

	public ApplicationDescription getOriginalDescription() {
		return originalDescription;
	}

	public void setOriginalDescription(
            ApplicationDescription originalDescription) {
		this.originalDescription = originalDescription;
	}

	public String getOriginalHost() {
		return originalHost;
	}

	public void setOriginalHost(String originalHost) {
		this.originalHost = originalHost;
	}
	
	public HostDeployment execute() throws AiravataAPIInvocationException{
		open();
		if (isApplicationDescCreated()){
			return new HostDeployment(getRegistry().getApplicationManager().getHostDescription(getHostName()),getShellApplicationDescription());
		}
		return null;
	}
	public List<String> getExistingHostList() {
		return existingHostList;
	}

	public void setExistingHostList(List<String> existingHostList) {
		this.existingHostList = existingHostList;
	}
	
	public static class HostDeployment{
		private HostDescription hostDescription;
		private ApplicationDescription applicationDescription;
		public HostDeployment(HostDescription hostDescription,ApplicationDescription applicationDescription) {
			setHostDescription(hostDescription);
			setApplicationDescription(applicationDescription);
		}
		public ApplicationDescription getApplicationDescription() {
			return applicationDescription;
		}
		public void setApplicationDescription(ApplicationDescription applicationDescription) {
			this.applicationDescription = applicationDescription;
		}
		public HostDescription getHostDescription() {
			return hostDescription;
		}
		public void setHostDescription(HostDescription hostDescription) {
			this.hostDescription = hostDescription;
		}
	}
	
}
