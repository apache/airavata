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

package org.apache.airavata.xbaya.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.amazonEC2.gui.AmazonEC2Menu;
import org.apache.airavata.xbaya.appwrapper.RegisterApplicationsMenu;
import org.apache.airavata.xbaya.component.gui.ComponentMenu;
import org.apache.airavata.xbaya.experiment.gui.ExperimentMenu;
import org.apache.airavata.xbaya.file.gui.FileMenu;
import org.apache.airavata.xbaya.gpel.gui.GPELMenu;
import org.apache.airavata.xbaya.monitor.gui.MonitorMenu;
import org.apache.airavata.xbaya.mylead.gui.MyLeadMenu;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyMenu;
import org.apache.airavata.xbaya.pegasus.gui.PegasusMenu;
import org.apache.airavata.xbaya.wf.gui.WorkflowMenu;

public class XBayaMenu implements XBayaComponent {

    private XBayaEngine engine;

    private JMenuBar menuBar;

    private WorkflowMenu workflowMenu;

    private FileMenu fileMenu;

    private ExperimentMenu experimentMenu;

    private GPELMenu gpelMenu;

    private PegasusMenu pegasusMenu;

    private AmazonEC2Menu amazonEC2Menu;

    private MyLeadMenu myLeadMenu;

    private ComponentMenu componentMenu;

    private MonitorMenu monitorMenu;

    private MyProxyMenu myProxyMenu;

    private RegisterApplicationsMenu registerApplications;

    /**
     * Constructs an XwfMenu.
     * 
     * @param engine
     */
    public XBayaMenu(XBayaEngine engine) {
        this.engine = engine;

        this.workflowMenu = new WorkflowMenu(this.engine);
        this.fileMenu = new FileMenu(this.engine);
        this.experimentMenu = new ExperimentMenu(this.engine);
        this.gpelMenu = new GPELMenu(this.engine);
        this.pegasusMenu = new PegasusMenu(this.engine);
        this.amazonEC2Menu = new AmazonEC2Menu(this.engine);
        this.myLeadMenu = new MyLeadMenu(this.engine);
        this.componentMenu = new ComponentMenu(this.engine);
        this.monitorMenu = new MonitorMenu(this.engine);
        this.myProxyMenu = new MyProxyMenu(this.engine);
        this.registerApplications = new RegisterApplicationsMenu(this.engine);

        createMenuBar();
    }

    /**
     * Returns the menu bar.
     * 
     * @return The menu bar.
     */
    public JMenuBar getSwingComponent() {
        return this.menuBar;
    }

    /**
     * Creates the menu bar.
     */
    private void createMenuBar() {

        this.menuBar = new JMenuBar();

        this.menuBar.add(this.workflowMenu.getMenu());
        this.menuBar.add(this.fileMenu.getMenu());
        this.menuBar.add(this.componentMenu.getMenu());
        this.menuBar.add(this.experimentMenu.getMenu());
        this.menuBar.add(this.gpelMenu.getMenu());
        this.menuBar.add(this.pegasusMenu.getMenu());
        this.menuBar.add(this.amazonEC2Menu.getMenu());
        this.menuBar.add(this.myLeadMenu.getMenu());
        this.menuBar.add(this.myProxyMenu.getMenu());
        this.menuBar.add(this.monitorMenu.getMenu());
        this.menuBar.add(this.registerApplications.getMenu());

        // Space before Help
        this.menuBar.add(Box.createHorizontalGlue());

        this.menuBar.add(createHelpMenu());

    }

    private JMenu createHelpMenu() {
        // Help
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About " + XBayaConstants.APPLICATION_SHORT_NAME);
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.addActionListener(new AbstractAction() {
            private AboutWindow window;

            public void actionPerformed(ActionEvent event) {
                if (this.window == null) {
                    this.window = new AboutWindow(XBayaMenu.this.engine);
                }
                this.window.show();
            }
        });
        helpMenu.add(aboutItem);

        return helpMenu;
    }

}