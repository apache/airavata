/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: WaitDialog.java,v 1.9 2008/04/01 21:44:25 echintha Exp $
 */
package org.apache.airavata.xbaya.gui;

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

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.util.SwingUtil;

import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class WaitDialog {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Cancelable cancelable;

    private String title;

    private String message;

    private XBayaDialog dialog;

    private boolean done;

    /**
     * Constructs a WaitDialog.
     * 
     * @param cancelable
     * @param title
     * @param message
     * @param engine
     */
    public WaitDialog(Cancelable cancelable, String title, String message, XBayaEngine engine) {
        this.cancelable = cancelable;
        this.title = title;
        this.message = message;
        this.engine = engine;
        this.done = true;
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        synchronized (this) {
            // We cannot check if this.done is false because show() might be
            // called more than once at the same time.
            // if (this.done == false) {
            // throw new IllegalStateException();
            // }
            this.done = false;
            // We cannot make the whole method synchronized because
            // this.dialog.show() blocks.
            if (this.dialog == null) {
                initGUI();
            }
        }
        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    public synchronized void hide() {
        if (this.done) {
            // The dialog is already hidden.
            return;
        }
        while (this.dialog == null || !this.dialog.getDialog().isVisible()) {
            try {
                // Wait for at least one show is called. We have to rely on
                // ComponentEvent because this.dialog.show() blocks.
                wait();
            } catch (InterruptedException e) {
                logger.caught(e);
            }
        }
        this.done = true;
        this.dialog.hide();
    }

    private void initGUI() {
        JLabel label = new JLabel(this.message, SwingConstants.CENTER);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                WaitDialog.this.dialog.hide();
                WaitDialog.this.cancelable.cancel();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, this.title, label, buttonPanel);

        this.dialog.getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.dialog.getDialog().setCursor(SwingUtil.WAIT_CURSOR);

        this.dialog.getDialog().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                shown();
            }
        });
    }

    private synchronized void shown() {
        logger.entering();
        notifyAll();
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
