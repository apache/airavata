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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorWindow {

    private static final String DEFAULT_ERROR_TITLE = "Error";

    private static final String DEFAULT_WARNING_TITLE = "Warning";

    private static final String DEFAULT_INFORMATION_TITLE = "Information";

    private static final Logger logger = LoggerFactory.getLogger(ErrorWindow.class);

    private Component defaultParent;

    /**
     * @param parent
     */
    public ErrorWindow(Component parent) {
        this.defaultParent = parent;
    }

    /**
     * @param message
     */
    public void error(String message) {
        error(null, null, message, null);
    }

    /**
     * @param message
     * @param e
     */
    public void error(String message, Throwable e) {
        error(null, null, message, e);
    }

    /**
     * @param e
     */
    public void error(Throwable e) {
        error(null, null, null, e);
    }

    /**
     * @param parent
     * @param message
     */
    public void error(Component parent, String message) {
        error(parent, null, message, null);
    }

    /**
     * @param parent
     * @param message
     * @param e
     */
    public void error(Component parent, String message, Throwable e) {
        error(parent, null, message, e);
    }

    /**
     * @param parent
     * @param title
     * @param message
     */
    public void error(Component parent, String title, String message) {
        error(parent, title, message, null);
    }

    /**
     * @param parent
     * @param title
     * @param message
     * @param e
     */
    public void error(Component parent, String title, String message, Throwable e) {

        logger.error(e==null? message:e.getMessage(), e);

        // If the parent component is not specified, set the frame the one.
        if (parent == null) {
            parent = this.defaultParent;
        }

        // If the title is still null, set it to the default.
        if (title == null) {
            title = DEFAULT_ERROR_TITLE;
        }

        // If the message is still null, try to get it from the exception first,
        // and set it to the default if it is still null.
        if (message == null) {
            if (e != null) {
                message = messageSplitter(e.getMessage());
            }
        }
        if (message == null) {
            message = ErrorMessages.UNEXPECTED_ERROR;
        }

        showErrorDialog(parent, title, message, e);
    }

    /**
     * Shows a warning dialog
     * 
     * @param message
     */
    public void warning(String message) {
        warning(null, null, message);
    }

    /**
     * Shows a warning dialog.
     * 
     * @param parent
     * @param title
     * @param message
     */
    public void warning(Component parent, String title, String message) {
        if (parent == null) {
            parent = this.defaultParent;
        }
        if (title == null) {
            title = DEFAULT_WARNING_TITLE;
        }
        showWarningDialog(parent, title, message);
    }

    /**
     * Shows a information dialog
     * 
     * @param message
     */
    public void info(String message) {
        info(null, null, message);
    }

    /**
     * Shows a information dialog.
     * 
     * @param parent
     * @param title
     * @param message
     */
    public void info(Component parent, String title, String message) {
        if (parent == null) {
            parent = this.defaultParent;
        }
        if (title == null) {
            title = DEFAULT_INFORMATION_TITLE;
        }
        showInformationDialog(parent, title, message);
    }

    private void showErrorDialog(Component parent, String title, String message, Throwable e) {

        String[] options = new String[] { "OK", "Detail" };
        int result = JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[0]);

        if (result == 1) {
            // A user clicked "Detail".

            // Gets the stack trace as a string
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            if (e != null) {
                e.printStackTrace(printWriter);
            }
            printWriter.close();
            String stackTrace = stringWriter.toString();

            JLabel messageLabel = new JLabel(messageSplitter(message));

            XBayaTextArea textArea = new XBayaTextArea();
            textArea.setEditable(false);
            textArea.setSize(800, 600);
            textArea.setText(stackTrace);

            GridPanel mainPanel = new GridPanel();
            mainPanel.add(messageLabel);
            mainPanel.add(textArea);
            mainPanel.layout(2, 1, 1, 0);

            JButton okButton = new JButton("OK");
            okButton.setDefaultCapable(true);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);

            final XBayaDialog dialog = new XBayaDialog(SwingUtilities.getWindowAncestor(parent), title, mainPanel,
                    buttonPanel);

            okButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    dialog.hide();
                }
            });

            dialog.setDefaultButton(okButton);
            dialog.show();
        }
    }

    private void showWarningDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showInformationDialog(Component parent, String title, String message) {

        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private String messageSplitter(String message){
        int interval = 150;
        char[] sAr = message.toCharArray();
        int i = 0;
        StringBuffer buffer = new StringBuffer("");
        if(sAr.length > interval){
        do{
            String subString = "";
            if(i + interval > message.length()){
             subString = message.substring(i,message.length());
            }else{
             subString = message.substring(i, i + interval);
            }
            buffer.append(subString);
            if(!subString.contains("\n")){
               buffer.append("\n");
            }
            i = i + interval;
        }while(i < sAr.length);
            return buffer.toString();
        }else{
            return message;
        }
    }

}