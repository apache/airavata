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

package org.apache.airavata.xbaya.amazonEC2.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;

public class AmazonEC2Menu {

    private JMenu amazonEC2Menu;

    private JMenuItem amazonAuthenticationItem;

    private JMenuItem amazonEC2ToolItem;

    private JMenuItem amazonS3ToolItem;

    private JMenuItem configAndDeploy;

    private XBayaEngine engine;

    /**
     * Constructs a AmazonEC2Menu.
     * 
     * @param engine
     */
    public AmazonEC2Menu(XBayaEngine engine) {
        this.engine = engine;

        createAmazonEC2Menu();
    }

    /**
     * create menu
     */
    private void createAmazonEC2Menu() {
        createAmazonAuthenticationItem();
        createAmazonEC2ToolItem();
        createAmazonS3ToolItem();
        createConfigAndRunItem();

        this.amazonEC2Menu = new JMenu("Amazon");

        this.amazonEC2Menu.add(this.amazonAuthenticationItem);
        this.amazonEC2Menu.addSeparator();
        this.amazonEC2Menu.add(this.amazonEC2ToolItem);
        this.amazonEC2Menu.add(this.amazonS3ToolItem);
        this.amazonEC2Menu.addSeparator();
        this.amazonEC2Menu.add(this.configAndDeploy);
    }

    @SuppressWarnings("serial")
	private void createAmazonAuthenticationItem() {
        this.amazonAuthenticationItem = new JMenuItem("Security Credentials");
        this.amazonAuthenticationItem.addActionListener(new AbstractAction() {
            private ChangeCredentialWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new ChangeCredentialWindow(AmazonEC2Menu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2Menu.this.engine.getErrorWindow().error(e1);
                }
            }
        });
    }

    @SuppressWarnings("serial")
	private void createAmazonEC2ToolItem() {
        this.amazonEC2ToolItem = new JMenuItem("EC2 Instances Management");
        this.amazonEC2ToolItem.addActionListener(new AbstractAction() {
            private EC2InstancesManagementWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new EC2InstancesManagementWindow(AmazonEC2Menu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2Menu.this.engine.getErrorWindow().error(e1);
                }
            }
        });
    }

    @SuppressWarnings("serial")
	private void createAmazonS3ToolItem() {
        this.amazonS3ToolItem = new JMenuItem("S3 Upload/Download Tool");
        this.amazonS3ToolItem.addActionListener(new AbstractAction() {
            private AmazonS3UtilsWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = AmazonS3UtilsWindow.getInstance(AmazonEC2Menu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2Menu.this.engine.getErrorWindow().error(e1);
                }
            }
        });
    }

    /**
	 * 
	 */
    @SuppressWarnings("serial")
	private void createConfigAndRunItem() {
        this.configAndDeploy = new JMenuItem("Config and Deploy Job Flow");
        this.configAndDeploy.addActionListener(new AbstractAction() {
            private AmazonEC2InvokerWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new AmazonEC2InvokerWindow(AmazonEC2Menu.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2Menu.this.engine.getErrorWindow().error(e1);
                }

            }
        });

    }

    /**
     * @return amazonEC2Menu
     */
    public JMenu getMenu() {
        return this.amazonEC2Menu;
    }

}