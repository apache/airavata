/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadLoader.java,v 1.16 2009/02/02 07:05:14 cherath Exp $
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

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;

import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadLoader implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private MyLead myLead;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * @param engine
     */
    public MyLeadLoader(XBayaEngine engine) {
        this(engine, engine.getMyLead());
    }

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * This method is used to load workflows from a specified location, not from the user's default location.
     * 
     * @param client
     * @param connection
     */
    public MyLeadLoader(XBayaEngine client, MyLead connection) {
        this.engine = client;
        this.myLead = connection;

        this.loadingDialog = new WaitDialog(this, "Loading the Workflow.", "Loading the Workflow. "
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
     * @param resouceID
     * @param blocking
     *            true for blocking call, false for non-blocking call
     */
    public void load(final QName resouceID, boolean blocking) {
        this.canceled = false;

        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(new QName(XBayaConstants.LEAD_NS, resouceID.getLocalPart()));
            }
        };
        this.loadThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.loadingDialog.show();

        if (blocking) {
            try {
                this.loadThread.join();
            } catch (InterruptedException e) {
                logger.caught(e);
            }
        }
    }

    /**
     * @param resouceID
     */
    private void runInThread(QName resouceID) {
        try {
            Workflow workflow;
            XRegistryAccesser xregistryAccesser = new XRegistryAccesser(this.engine);
            workflow = xregistryAccesser.getWorkflow(resouceID);
            this.loadingDialog.hide();
            if (this.canceled) {
                return;
            }
            this.engine.setWorkflow(workflow);
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_LOAD_TEMPLATE_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.loadingDialog.hide();
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
