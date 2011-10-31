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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.XBayaLinkButton;

public class ApplicationDescriptionDialog extends JDialog implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = -2745085755585610025L;
    private JTextField txtExecPath;
    private JTextField txtAppName;
    private JTextField txtTempDir;

    private Registry registry;
    private ApplicationDeploymentDescription shellApplicationDescription;
    private JLabel lblError;
    private boolean applcationDescCreated = false;
    private JButton okButton;

    private String serviceName;
    private String hostName;
    private JComboBox cmbServiceName;
    private JComboBox cmbHostName;

    private XBayaEngine engine;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ApplicationDescriptionDialog dialog = new ApplicationDescriptionDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public ApplicationDescriptionDialog(XBayaEngine engine) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
                String baseName = "Application";
                int i = 1;
                String defaultName = baseName + i;
                try {
                    List<ApplicationDeploymentDescription> applicationDeploymentDescriptions = getRegistry().searchDeploymentDescription(getServiceName(), getHostName());
                    while (true) {
                        boolean notFound = true;
                        for (ApplicationDeploymentDescription deploymentDescription : applicationDeploymentDescriptions) {
                            if (deploymentDescription.getType().addNewApplicationName().getStringValue().equals(defaultName)) {
                                notFound = false;
                                break;
                            }
                        }
                        if (notFound) {
                            break;
                        }
                        defaultName = baseName + (++i);
                    }
                } catch (Exception e) {
                }
                txtAppName.setText(defaultName);
                setApplicationName(txtAppName.getText());
            }
        });
        setRegistry(engine.getConfiguration().getJcrComponentRegistry().getRegistry());
        iniGUI();
    }

    public void open() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected ApplicationDescriptionDialog getDialog() {
        return this;
    }

    private void iniGUI() {
        setTitle("New Deployment Description");
        setBounds(100, 100, 671, 454);
        setModal(true);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            lblError = new JLabel("");
            lblError.setForeground(Color.RED);
            buttonPane.add(lblError);
            {
                okButton = new JButton("Save");
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
            JPanel panel = new JPanel();
            getContentPane().add(panel, BorderLayout.CENTER);
            JLabel lblApplicationName = new JLabel("Application name");
            JLabel lblExecutablePatyh = new JLabel("Executable path");
            txtExecPath = new JTextField();
            txtExecPath.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    setExecutablePath(txtExecPath.getText());
                }
            });
            txtExecPath.setColumns(10);
            txtAppName = new JTextField();
            txtAppName.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent arg0) {
                    setApplicationName(txtAppName.getText());
                }
            });
            txtAppName.setColumns(10);
            JSeparator separator_1 = new JSeparator();
            JLabel lblTemporaryDirectory = new JLabel("Temporary directory");
            txtTempDir = new JTextField();
            txtTempDir.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    setTempDir(txtTempDir.getText());
                }
            });
            txtTempDir.setColumns(10);
            JButton btnAdvance = new JButton("Advanced options...");
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

            JSeparator separator = new JSeparator();

            XBayaLinkButton blnkbtnCreateNewService = new XBayaLinkButton("New button");
            blnkbtnCreateNewService.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        ServiceDescriptionDialog serviceDescriptionDialog = new ServiceDescriptionDialog(getRegistry());
                        serviceDescriptionDialog.open();
                        if (serviceDescriptionDialog.isServiceCreated()) {
                            loadServiceDescriptions();
                            cmbServiceName.setSelectedItem(serviceDescriptionDialog.getServiceName());
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
                    }
                }
            });
            blnkbtnCreateNewService.setText("Create new service...");
            blnkbtnCreateNewService.setHorizontalAlignment(SwingConstants.TRAILING);

            cmbServiceName = new JComboBox();
            cmbServiceName.addActionListener(this);
            cmbServiceName.setRenderer(new DefaultListCellRenderer());
            cmbHostName = new JComboBox();
            cmbHostName.addActionListener(this);

            JLabel label_1 = new JLabel("Host");

            XBayaLinkButton bayaLinkButton_1 = new XBayaLinkButton("New button");
            bayaLinkButton_1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(engine);
                        // TODO : do we need this?
                        //hostDescriptionDialog.open();
                        hostDescriptionDialog.show();

                        if (hostDescriptionDialog.isHostCreated()) {
                            loadHostDescriptions();
                            cmbHostName.setSelectedItem(hostDescriptionDialog.getHostLocation());
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
                    }
                }
            });
            bayaLinkButton_1.setText("Create new host...");
            bayaLinkButton_1.setHorizontalAlignment(SwingConstants.TRAILING);

            JLabel lblService = new JLabel("Service");

            JLabel lblBindThisDeployment = new JLabel("Bind this deployment description to:");
            lblBindThisDeployment.setFont(new Font("Tahoma", Font.BOLD, 11));
            GroupLayout gl_panel = new GroupLayout(panel);
            gl_panel.setHorizontalGroup(gl_panel
                    .createParallelGroup(Alignment.TRAILING)
                    .addGroup(
                            gl_panel.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(
                                            gl_panel.createParallelGroup(Alignment.LEADING)
                                                    .addGroup(
                                                            gl_panel.createSequentialGroup()
                                                                    .addGroup(
                                                                            gl_panel.createParallelGroup(
                                                                                    Alignment.LEADING)
                                                                                    .addGroup(
                                                                                            gl_panel.createSequentialGroup()
                                                                                                    .addGap(12)
                                                                                                    .addGroup(
                                                                                                            gl_panel.createParallelGroup(
                                                                                                                    Alignment.TRAILING)
                                                                                                                    .addComponent(
                                                                                                                            lblApplicationName)
                                                                                                                    .addComponent(
                                                                                                                            lblExecutablePatyh))
                                                                                                    .addGap(18)
                                                                                                    .addGroup(
                                                                                                            gl_panel.createParallelGroup(
                                                                                                                    Alignment.LEADING,
                                                                                                                    false)
                                                                                                                    .addComponent(
                                                                                                                            txtExecPath)
                                                                                                                    .addComponent(
                                                                                                                            txtAppName,
                                                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                                                            309,
                                                                                                                            GroupLayout.PREFERRED_SIZE)))
                                                                                    .addComponent(separator_1,
                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                            425,
                                                                                            GroupLayout.PREFERRED_SIZE)
                                                                                    .addComponent(separator,
                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                            425,
                                                                                            GroupLayout.PREFERRED_SIZE)
                                                                                    .addGroup(
                                                                                            gl_panel.createSequentialGroup()
                                                                                                    .addGap(10)
                                                                                                    .addGroup(
                                                                                                            gl_panel.createParallelGroup(
                                                                                                                    Alignment.TRAILING)
                                                                                                                    .addComponent(
                                                                                                                            btnAdvance)
                                                                                                                    .addGroup(
                                                                                                                            gl_panel.createSequentialGroup()
                                                                                                                                    .addComponent(
                                                                                                                                            lblTemporaryDirectory)
                                                                                                                                    .addPreferredGap(
                                                                                                                                            ComponentPlacement.RELATED)
                                                                                                                                    .addComponent(
                                                                                                                                            txtTempDir,
                                                                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                                                                            305,
                                                                                                                                            GroupLayout.PREFERRED_SIZE))))
                                                                                    .addGroup(
                                                                                            gl_panel.createSequentialGroup()
                                                                                                    .addGap(20)
                                                                                                    .addGroup(
                                                                                                            gl_panel.createParallelGroup(
                                                                                                                    Alignment.LEADING)
                                                                                                                    .addComponent(
                                                                                                                            lblService)
                                                                                                                    .addComponent(
                                                                                                                            label_1,
                                                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                                                            77,
                                                                                                                            GroupLayout.PREFERRED_SIZE))
                                                                                                    .addPreferredGap(
                                                                                                            ComponentPlacement.RELATED,
                                                                                                            51,
                                                                                                            Short.MAX_VALUE)
                                                                                                    .addGroup(
                                                                                                            gl_panel.createParallelGroup(
                                                                                                                    Alignment.LEADING)
                                                                                                                    .addComponent(
                                                                                                                            cmbServiceName,
                                                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                                                            313,
                                                                                                                            GroupLayout.PREFERRED_SIZE)
                                                                                                                    .addComponent(
                                                                                                                            cmbHostName,
                                                                                                                            GroupLayout.PREFERRED_SIZE,
                                                                                                                            313,
                                                                                                                            GroupLayout.PREFERRED_SIZE))
                                                                                                    .addGap(10))
                                                                                    .addComponent(lblBindThisDeployment))
                                                                    .addContainerGap())
                                                    .addGroup(
                                                            Alignment.TRAILING,
                                                            gl_panel.createSequentialGroup()
                                                                    .addComponent(blnkbtnCreateNewService,
                                                                            GroupLayout.PREFERRED_SIZE, 185,
                                                                            GroupLayout.PREFERRED_SIZE)
                                                                    .addContainerGap())
                                                    .addGroup(
                                                            Alignment.TRAILING,
                                                            gl_panel.createSequentialGroup()
                                                                    .addComponent(bayaLinkButton_1,
                                                                            GroupLayout.PREFERRED_SIZE, 176,
                                                                            GroupLayout.PREFERRED_SIZE).addGap(2)))));
            gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
                    gl_panel.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(txtAppName, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblApplicationName))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(txtExecPath, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblExecutablePatyh))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(lblTemporaryDirectory)
                                            .addComponent(txtTempDir, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnAdvance)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(lblBindThisDeployment)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(cmbServiceName, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblService))
                            .addGap(2)
                            .addComponent(blnkbtnCreateNewService, GroupLayout.PREFERRED_SIZE, 23,
                                    GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(cmbHostName, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label_1)).addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(bayaLinkButton_1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
            gl_panel.setAutoCreateGaps(true);
            gl_panel.setAutoCreateContainerGaps(true);
            panel.setLayout(gl_panel);
            loadServiceDescriptions();
            loadHostDescriptions();
        }
        setResizable(false);
        getRootPane().setDefaultButton(okButton);
    }

    private void loadServiceDescriptions() {
        cmbServiceName.removeAllItems();
        setServiceName(null);
        try {
            List<ServiceDescription> serviceDescriptions = getRegistry().searchServiceDescription("");
            for (ServiceDescription serviceDescription : serviceDescriptions) {
                cmbServiceName.addItem(serviceDescription.getType().getName());
            }
        } catch (Exception e) {
            setError(e.getLocalizedMessage());
        }
        updateServiceName();
    }

    private void loadHostDescriptions() {
        cmbHostName.removeAllItems();
        setHostName(null);
        try {
            List<HostDescription> hostDescriptions = getRegistry().searchHostDescription(".*");
            for (HostDescription hostDescription : hostDescriptions) {
                if (hostDescription.getType().getHostName() == null) {
                    cmbHostName.addItem(hostDescription.getType().getHostName());
                } else {
                    cmbHostName.addItem(hostDescription.getType().getHostName());
                }
            }
        } catch (Exception e) {
            setError(e.getLocalizedMessage());
        }
        updateHostName();
    }

    public ApplicationDeploymentDescription getShellApplicationDescription() {
        if(shellApplicationDescription == null){
            shellApplicationDescription = new ApplicationDeploymentDescription();
        }
        return shellApplicationDescription;
    }

    public ApplicationDeploymentDescriptionType getApplicationDescriptionType() {
    	return getShellApplicationDescription().getType();
    }
    
    public String getApplicationName() {
        return getApplicationDescriptionType().getApplicationName().getStringValue();
    }

    public void setApplicationName(String applicationName) {
        ApplicationDeploymentDescriptionType.ApplicationName applicationName1 = getApplicationDescriptionType().addNewApplicationName();
        applicationName1.setStringValue(applicationName);
        updateDialogStatus();
    }

    public String getExecutablePath() {
        return getApplicationDescriptionType().getExecutableLocation();
    }

    public void setExecutablePath(String executablePath) {
    	getApplicationDescriptionType().setExecutableLocation(executablePath);
        updateDialogStatus();
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
        getRegistry().saveDeploymentDescription(getServiceName(), getHostName(), getShellApplicationDescription());
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
        if (getApplicationName() == null || getApplicationName().trim().equals("")) {
            throw new Exception("Name of the application cannot be empty!!!");
        }

        List<ApplicationDeploymentDescription> deploymentDescriptions = null;
        try {
            deploymentDescriptions = getRegistry().searchDeploymentDescription(getServiceName(), getHostName(),
                    Pattern.quote(getApplicationName()));
        } catch (RegistryException e) {
            throw e;
        }
        if (deploymentDescriptions.size() > 0) {
            throw new Exception("Application descriptor with the given name already exists!!!");
        }

        if (getExecutablePath() == null || getExecutablePath().trim().equals("")) {
            throw new Exception("Executable path cannot be empty!!!");
        }

        if (getTempDir() == null || getTempDir().trim().equals("")) {
            throw new Exception("Temporary directory location cannot be empty!!!");
        }

        if (getServiceName() == null || getServiceName().trim().equals("")) {
            throw new Exception("Please select/create service to bind to this deployment description");
        }

        if (getHostName() == null || getHostName().trim().equals("")) {
            throw new Exception("Please select/create host to bind to this deployment description");
        }

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        updateDialogStatus();
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        updateDialogStatus();
    }

    private void updateServiceName() {
        if (cmbServiceName.getSelectedItem() != null) {
            setServiceName(cmbServiceName.getSelectedItem().toString());
        }
    }

    private void updateHostName() {
        if (cmbHostName.getSelectedItem() != null) {
            setHostName(cmbHostName.getSelectedItem().toString());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cmbServiceName) {
            updateServiceName();
        }
        if (e.getSource() == cmbHostName) {
            updateHostName();
        }
        if (e.getSource() == txtAppName) {
            setApplicationName(txtAppName.getText());
        }
        if (e.getSource() == txtExecPath) {
            setExecutablePath(txtExecPath.getText());
        }
        if (e.getSource() == txtTempDir) {
            setTempDir(txtTempDir.getText());
        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

}
