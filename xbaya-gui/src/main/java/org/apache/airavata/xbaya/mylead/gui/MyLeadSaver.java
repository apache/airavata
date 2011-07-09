/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadSaver.java,v 1.22 2009/02/02 18:22:41 cherath Exp $
 */
package org.apache.airavata.xbaya.mylead.gui;

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

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.mylead.MyLeadException;
import org.apache.airavata.xbaya.ode.gui.ODEDeploymentClient;
import org.apache.airavata.xbaya.ode.gui.ODEDeploymentWindow;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;
import org.ietf.jgss.GSSCredential;

import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadSaver implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog savingDialog;

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * @param client
     */
    public MyLeadSaver(XBayaEngine client) {
        this.engine = client;

        this.savingDialog = new WaitDialog(this, "Saving the Workflow.", "Saving the Workflow. "
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
     * Saves the workflow.
     * 
     * @param redeploy
     */
    public void save(final boolean redeploy) {
        this.canceled = false;

        final Workflow workflow = MyLeadSaver.this.engine.getWorkflow();
        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(redeploy, workflow);
            }
        };
        this.loadThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.savingDialog.show();
    }

    /**
     * @param redeploy
     * @param workflow
     */
    private void runInThread(boolean redeploy, Workflow workflow) {
        try {
            WaitDialog waitDialog = new WaitDialog(this, "Deploying the Workflow.", "Deploying the Workflow."
                    + "Please wait for a moment.", this.engine);
            GSSCredential proxy = this.engine.getMyProxyClient().getProxy();
            new ODEDeploymentWindow();
            final WorkflowProxyClient client = new WorkflowProxyClient();
            client.setXRegistryUrl(this.engine.getConfiguration().getXRegistryURL());
            client.setEngineURL(this.engine.getConfiguration().getProxyURI());
            client.setXBayaEngine(this.engine);

            new ODEDeploymentClient(this.engine, waitDialog).deploy(client, workflow, proxy, true,
                    System.currentTimeMillis());

            this.engine.getMyLead().setProxy(proxy);
            this.engine.getMyLead().save(workflow, redeploy);
            this.savingDialog.hide();
        } catch (MyLeadException e) {
            if (MyLeadSaver.this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_SAVE_TEMPLATE_ERROR, e);
                this.savingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_SAVE_TEMPLATE_ERROR, e);
                this.savingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.savingDialog.hide();
        } catch (WorkflowEngineException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.savingDialog.hide();
        }
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
