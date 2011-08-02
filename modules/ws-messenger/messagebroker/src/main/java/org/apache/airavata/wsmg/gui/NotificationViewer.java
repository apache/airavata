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

package org.apache.airavata.wsmg.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class NotificationViewer {

    boolean packFrame = false;

    /*
     * private static String getAxis2Repository(String[] cmdArgs) {
     * 
     * // if its in system properties get it.
     * 
     * if (System.getProperty(WsmgCommonConstants.CONFIG_AXIS2_REPO) != null) { File repodir = new File(System
     * .getProperty(WsmgCommonConstants.CONFIG_AXIS2_REPO));
     * 
     * if (repodir.isDirectory()) { return repodir.getAbsolutePath(); }
     * 
     * throw new RuntimeException( "axis2 repository given in system parameter is invalid: " +
     * repodir.getAbsolutePath());
     * 
     * }
     * 
     * if (cmdArgs.length > 1) {
     * 
     * if (cmdArgs[0].startsWith("-" + WsmgCommonConstants.CONFIG_AXIS2_REPO)) {
     * 
     * File repoDir = new File(cmdArgs[1]);
     * 
     * if (repoDir.isDirectory()) { return repoDir.getAbsolutePath(); }
     * 
     * throw new RuntimeException( "axis2 repository given as a  command line argument is invalid: " +
     * repoDir.getAbsolutePath()); }
     * 
     * throw new RuntimeException("unknown commandline argument");
     * 
     * }
     * 
     * String axis2Home = System.getenv().get("AXIS2_HOME");
     * 
     * 
     * if (axis2Home != null) {
     * 
     * String repo = axis2Home.endsWith(File.pathSeparator) ? axis2Home + "repository" : axis2Home + File.separator +
     * "repository";
     * 
     * File repoDir = new File(repo);
     * 
     * if (repoDir.isDirectory()) {
     * 
     * 
     * return repoDir.getAbsolutePath(); } }
     * 
     * return null; }
     * 
     * private static void printHelp() { System.out.println("unable to determine axis2 repository");
     * System.out.println("please provide the system property: " + WsmgCommonConstants.CONFIG_AXIS2_REPO);
     * System.out.println("or set AXIS2_HOME envirnment variable"); }
     */

    /**
     * Construct and show the application.
     */
    public NotificationViewer() {
        NotificationViewerFrame frame = new NotificationViewerFrame();
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their
        // layout
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    /**
     * Application entry point.
     * 
     * @param args
     *            String[]
     */
    public static void main(String[] args) {

        /*
         * final String axis2Repo = getAxis2Repository(args);
         * 
         * if (axis2Repo == null) { printHelp(); System.exit(1); }
         */

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                new NotificationViewer();
            }
        });
    }
}
