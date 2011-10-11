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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.gui.JCRRegistryWindow;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;

public class RegisterApplicationsMenu {

    private JMenu registerApplicationsMenu;

    private JMenuItem registerServiceDesc;

    private JMenuItem registerApplicationDesc;

    private JMenuItem registerHostDesc;

    private JMenuItem registerThroughFile;

    private JMenuItem searchAndEdit;

    private XBayaEngine engine;

    private MyProxyChecker myProxyChecker;

    /**
     * Constructs a MyProxyMenu.
     * 
     * @param engine
     */
    public RegisterApplicationsMenu(XBayaEngine engine) {
        this.engine = engine;
        createRegsiterApplicationsMenu();
    }

    /**
     * @return The JMenu.
     */
    public JMenu getMenu() {
        return this.registerApplicationsMenu;
    }

    private void createRegsiterApplicationsMenu() {

        createRegisterHostDesc();
        createRegisterServiceDesc();
        createRegisterApplicationDesc();
        createRegisterThroughFile();
        createSearchAndEdit();

        this.registerApplicationsMenu = new JMenu("Register Applications");
        this.registerApplicationsMenu.setMnemonic(KeyEvent.VK_P);

        this.registerApplicationsMenu.add(this.registerHostDesc);
        this.registerApplicationsMenu.add(this.registerServiceDesc);
        this.registerApplicationsMenu.add(this.registerApplicationDesc);
        this.registerApplicationsMenu.add(this.registerThroughFile);
        this.registerApplicationsMenu.addSeparator();
        this.registerApplicationsMenu.add(this.searchAndEdit);
    }

    private boolean acquireJCRRegistry(){
    	if (engine.getConfiguration().getJcrComponentRegistry()==null){
	    	JCRRegistryWindow window = new JCRRegistryWindow(this.engine);
			window.show();
    	}
    	return engine.getConfiguration().getJcrComponentRegistry()!=null;
    }
    
    private void createRegisterThroughFile() {
        this.registerThroughFile = new JMenuItem("Register Description Through File");

        this.registerThroughFile.addActionListener(new AbstractAction() {
            private RegisterThroughFileWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    RegisterApplicationsMenu.this.myProxyChecker = new MyProxyChecker(
                            RegisterApplicationsMenu.this.engine);
                    boolean loaded = RegisterApplicationsMenu.this.myProxyChecker.loadIfNecessary();
                    if (loaded) {
                        // intended to be blank
                    } else {
                        return; // error
                    }

                    this.window = RegisterThroughFileWindow.getInstance();
                }
                if (!this.window.isEngineSet()) {
                    this.window.setXBayaEngine(RegisterApplicationsMenu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    RegisterApplicationsMenu.this.engine.getErrorWindow().error(e1);
                }
            }
        });
    }

    private void createRegisterServiceDesc() {
        this.registerServiceDesc = new JMenuItem("Register Application Service Description");

        this.registerServiceDesc.addActionListener(new AbstractAction() {
            private ServiceDescriptionRegistrationWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (acquireJCRRegistry()) {
					try {
						ServiceDescriptionDialog serviceDescriptionDialog = new ServiceDescriptionDialog(
								RegisterApplicationsMenu.this.engine);
						serviceDescriptionDialog.open();
					} catch (Exception e1) {
						RegisterApplicationsMenu.this.engine.getErrorWindow()
								.error(e1);
					}
				}
            }
        });

    }

    private void createRegisterApplicationDesc() {
        this.registerApplicationDesc = new JMenuItem("Register Application Deployment Description");

        this.registerApplicationDesc.addActionListener(new AbstractAction() {
            private ApplicationDescriptionRegistrationWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
            	 if (acquireJCRRegistry()) {
					try {
						ApplicationDescriptionDialog applicationDescriptionDialog = new ApplicationDescriptionDialog(
								RegisterApplicationsMenu.this.engine);
						applicationDescriptionDialog.open();
					} catch (Exception e1) {
						RegisterApplicationsMenu.this.engine.getErrorWindow()
								.error(e1);
					}
				}
            }
        });

    }

    private void createRegisterHostDesc() {
        this.registerHostDesc = new JMenuItem("Register Compute/Service Host Description");

        this.registerHostDesc.addActionListener(new AbstractAction() {
            private HostDescriptionRegistrationWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (acquireJCRRegistry()) {
					try {
						HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(
								RegisterApplicationsMenu.this.engine);
						hostDescriptionDialog.open();
					} catch (Exception e1) {
						RegisterApplicationsMenu.this.engine.getErrorWindow()
								.error(e1);
					}
				}
            }
        });

    }

    private void createSearchAndEdit() {
        this.searchAndEdit = new JMenuItem("Search and Edit Description");

        this.searchAndEdit.addActionListener(new AbstractAction() {
            private SearchAndEditWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    RegisterApplicationsMenu.this.myProxyChecker = new MyProxyChecker(
                            RegisterApplicationsMenu.this.engine);
                    boolean loaded = RegisterApplicationsMenu.this.myProxyChecker.loadIfNecessary();
                    if (loaded) {
                        // intended to be blank
                    } else {
                        return; // error
                    }

                    this.window = SearchAndEditWindow.getInstance();
                }
                if (!this.window.isEngineSet()) {
                    this.window.setXBayaEngine(RegisterApplicationsMenu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    RegisterApplicationsMenu.this.engine.getErrorWindow().error(e1);
                }
            }
        });

    }

}