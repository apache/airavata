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

package org.apache.airavata.xbaya.ui.dialogs.registry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.jcr.RepositoryException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.airavata.common.registry.api.user.AuthorizableExistsException;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class NewJCRRegistryUserDialog {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private XBayaTextField urlTextField;

    private XBayaTextField usernameTextField;

    private JPasswordField passwordTextField;

    private String username;

    private URL url;

    private String password;

    private JPasswordField confirmPasswordTextField;

    private JButton okButton;

    private boolean userCreated = false;

    public NewJCRRegistryUserDialog(XBayaEngine engine) {
        this(engine, null, null);
    }

    public NewJCRRegistryUserDialog(XBayaEngine engine, URL url) {
        this(engine, url, null);
    }

    public NewJCRRegistryUserDialog(XBayaEngine engine, String username) {
        this(engine, null, username);
    }

    /**
     * @param engine
     */
    public NewJCRRegistryUserDialog(XBayaEngine engine, URL url, String username) {
        this.engine = engine;
        setUrl(url);
        setUsername(username);
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        this.dialog.show();
    }

    private void hide() {
        setUserCreated(false);
        this.dialog.hide();
    }

    private void setData() {
        updateURL();
        updateUsername();
        updatePassword();
    }

    private void ok() {
        setData();
        String status = updateStatus();
        if (status == null) {
            try {
                JCRComponentRegistry registry = new JCRComponentRegistry(getUrl().toURI(), "anonymous", "");
                registry.getUserManager().createUser(getUsername(), getPassword());
                hide();
                setUserCreated(true);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                engine.getErrorWindow().error(e.getMessage());
            } catch (AuthorizableExistsException e) {
                e.printStackTrace();
                engine.getErrorWindow().error(e.getMessage());
            } catch (RepositoryException e) {
                e.printStackTrace();
                engine.getErrorWindow().error(e.getMessage());
            }
        } else {
            engine.getErrorWindow().error(status);
        }
    }

    private String updateStatus() {
        String msg = null;
        if (getUrl() == null) {
            msg = "The url cannot be empty";
        } else if (getUsername() == null || getUsername().equals("")) {
            msg = "Username cannot be empty";
        } else if (getPassword() == null || getPassword().equals("")) {
            msg = "Passwords must match or cannot be empty";
        }
        // okButton.setEnabled(msg==null);
        return msg;
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.urlTextField = new XBayaTextField();
        this.usernameTextField = new XBayaTextField();
        this.passwordTextField = new JPasswordField();
        this.confirmPasswordTextField = new JPasswordField();
        XBayaLabel urlLabel = new XBayaLabel("URL", this.urlTextField);
        XBayaLabel userLabel = new XBayaLabel("Username", this.usernameTextField);
        XBayaLabel passLabel = new XBayaLabel("Password", this.passwordTextField);
        XBayaLabel confirmPassLabel = new XBayaLabel("Confirm Password", this.confirmPasswordTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(urlLabel);
        infoPanel.add(this.urlTextField);
        infoPanel.add(userLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.add(passLabel);
        infoPanel.add(this.passwordTextField);
        infoPanel.add(confirmPassLabel);
        infoPanel.add(this.confirmPasswordTextField);

        infoPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);

        urlTextField.getSwingComponent().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateURL();
                updateStatus();
            }

        });

        usernameTextField.getSwingComponent().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateUsername();
                updateStatus();
            }

        });

        passwordTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updatePassword();
                updateStatus();
            }
        });

        confirmPasswordTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updatePassword();
                updateStatus();
            }
        });

        okButton = new JButton("OK");
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "JCR Registry New User", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
        updateControlData();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void updateControlData() {
        if (usernameTextField != null && getUsername() != null) {
            usernameTextField.setText(getUsername());
        }
        if (urlTextField != null && getUrl() != null) {
            urlTextField.setText(getUrl().toString());
        }
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void updateURL() {
        URL specifiedURL = null;
        try {
            specifiedURL = new URL(urlTextField.getText());
        } catch (MalformedURLException e) {
            // erroneious url, ignore it
        }
        setUrl(specifiedURL);
    }

    private void updateUsername() {
        setUsername(usernameTextField.getText());
    }

    private void updatePassword() {
        String password = null;
        String ptext = new String(passwordTextField.getPassword());
        String ctext = new String(confirmPasswordTextField.getPassword());
        if (ptext.equals(ctext)) {
            password = ptext;
        }
        setPassword(password);
    }

    public boolean isUserCreated() {
        return userCreated;
    }

    public void setUserCreated(boolean userCreated) {
        this.userCreated = userCreated;
    }
}
