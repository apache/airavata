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

package org.apache.airavata.xbaya.component.registry;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;

import xsul5.MLogger;

public class ComponentRegistryLoader implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * @param engine
     */
    public ComponentRegistryLoader(XBayaEngine engine) {
        this.engine = engine;

        this.loadingDialog = new WaitDialog(this, "Loading a Component List.", "Loading a Component List. "
                + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.loadThread.interrupt();
    }

    /**
     * Loads the workflow.
     * 
     * @param registry
     * 
     */
    public void load(final ComponentRegistry registry) {
        this.canceled = false;

        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(registry);
            }
        };
        this.loadThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.loadingDialog.show();
    }

    /**
     * @param registry
     */
    private void runInThread(ComponentRegistry registry) {
        logger.entering();
        try {
            ComponentTreeNode componentTree = registry.getComponentTree();
            if (this.canceled) {
                return;
            }
            this.engine.getGUI().getComponentSelector().addComponentTree(componentTree);
            this.loadingDialog.hide();
        } catch (ComponentRegistryException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.loadingDialog.hide();
        }
    }
}