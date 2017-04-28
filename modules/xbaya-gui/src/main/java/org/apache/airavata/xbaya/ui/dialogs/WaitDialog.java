/**
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
 */
package org.apache.airavata.xbaya.ui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.utils.Cancelable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitDialog {

    private static final Logger logger = LoggerFactory.getLogger(WaitDialog.class);

    private XBayaGUI xbayaGUI;

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
    public WaitDialog(Cancelable cancelable, String title, String message, XBayaGUI xbayaGUI) {
        this.cancelable = cancelable;
        this.title = title;
        this.message = message;
        this.xbayaGUI = xbayaGUI;
        this.done = true;
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        synchronized (this) {
            // We cannot check if this.done is false because show() might be
            // called more than once at the same time.
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
                logger.error(e.getMessage(), e);
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

        this.dialog = new XBayaDialog(this.xbayaGUI, this.title, label, buttonPanel);

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
        notifyAll();
    }
}