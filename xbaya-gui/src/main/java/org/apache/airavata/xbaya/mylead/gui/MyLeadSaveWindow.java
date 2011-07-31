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

package org.apache.airavata.xbaya.mylead.gui;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gpel.gui.GPELDeployWindow;

public class MyLeadSaveWindow extends GPELDeployWindow {

    private MyLeadSaver saver;

    /**
     * Constructs a MyLeadSaveWorkflowWindow.
     * 
     * @param engine
     */
    public MyLeadSaveWindow(XBayaEngine engine) {
        super(engine);
        this.saver = new MyLeadSaver(engine);
        initGUI();
    }

    @Override
    protected void deploy(boolean redeploy) {
        // Set the name and description to the graph.
        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();

        if (name.trim().length() == 0) {
            this.engine.getErrorWindow().warning("You need to set the name of the workflow.");
        } else {
            // this.workflow.setName(name);
            // this.workflow.setDescription(description);
            // Use this method in order to change the name of the tab.
            this.engine.getGUI().getGraphCanvas().setNameAndDescription(name, description);
            hide();
            this.saver.save(redeploy);
        }
    }

    private void initGUI() {
        this.dialog.setTitle("Save the workflow template to myLEAD and XRegistry");
    }
}