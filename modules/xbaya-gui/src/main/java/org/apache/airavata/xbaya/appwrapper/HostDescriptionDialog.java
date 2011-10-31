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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostDescriptionDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(HostDescriptionDialog.class);
    
    private static final long serialVersionUID = 1423293834766468324L;
    private JTextField txtHostLocation;
    private JTextField txtHostName;
    private HostDescription hostDescription;
    private Registry registry;
    private JButton okButton;
    private boolean hostCreated = false;
    private JLabel lblError;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            HostDescriptionDialog dialog = new HostDescriptionDialog(null);
            dialog.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected HostDescriptionDialog getDialog() {
        return this;
    }

    /**
     * Create the dialog.
     */
    public HostDescriptionDialog(Registry registry) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
                String baseName = "Host";
                int i = 1;
                String defaultName = baseName + i;
                try {
                    while (getRegistry().getHostDescription(defaultName) != null) {
                        defaultName = baseName + (++i);
                    }
                } catch (RegistryException e) {
                    log.error("error", e);
                }
                txtHostName.setText(defaultName);
                setHostId(txtHostName.getText());
            }
        });
        setRegistry(registry);
        initGUI();
    }

    private void initGUI() {
        setTitle("New Host Description");
        setBounds(100, 100, 455, 182);
        setModal(true);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                okButton = new JButton("Save");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveHostDescription();
                        close();
                    }
                });

                lblError = new JLabel("");
                lblError.setForeground(Color.RED);
                buttonPane.add(lblError);
                okButton.setEnabled(false);
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setHostCreated(false);
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
            JLabel lblHostName = new JLabel("Host id");
            JLabel lblHostLocationip = new JLabel("Host address");
            txtHostLocation = new JTextField();
            txtHostLocation.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    setHostLocation(txtHostLocation.getText());
                }
            });
            txtHostLocation.setColumns(10);
            txtHostName = new JTextField();
            txtHostName.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    setHostId(txtHostName.getText());
                }
            });
            txtHostName.setColumns(10);
            GroupLayout gl_panel = new GroupLayout(panel);
            gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
                    gl_panel.createSequentialGroup()
                            .addGap(22)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.TRAILING).addComponent(lblHostName)
                                            .addComponent(lblHostLocationip))
                            .addGap(18)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.LEADING, false)
                                            .addComponent(txtHostLocation)
                                            .addComponent(txtHostName, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
                            .addGap(37)));
            gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
                    gl_panel.createSequentialGroup()
                            .addGap(31)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(txtHostName, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblHostName))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(
                                    gl_panel.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(txtHostLocation, GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblHostLocationip)).addGap(176)));
            gl_panel.setAutoCreateGaps(true);
            gl_panel.setAutoCreateContainerGaps(true);
            panel.setLayout(gl_panel);
        }
        setResizable(false);
        getRootPane().setDefaultButton(okButton);
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
        okButton.setEnabled(message == null);
        setError(message);
    }

    public void close() {
        getDialog().setVisible(false);
    }

    public boolean isHostCreated() {
        return hostCreated;
    }

    public void setHostCreated(boolean hostCreated) {
        this.hostCreated = hostCreated;
    }

    public HostDescription getHostDescription() {
        if (hostDescription == null) {
            hostDescription = new HostDescription();
        }
        return hostDescription;
    }

    public void saveHostDescription() {
        getRegistry().saveHostDescription(getHostDescription());
        setHostCreated(true);
    }

    private void setError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().equals("")) {
            lblError.setText("");
        } else {
            lblError.setText(errorMessage.trim());
        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
