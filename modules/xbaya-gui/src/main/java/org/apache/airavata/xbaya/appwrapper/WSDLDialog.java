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

package org.apache.airavata.xbaya.appwrapper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.airavata.xbaya.XBayaEngine;

public class WSDLDialog extends JDialog {

    private static final long serialVersionUID = -8250430517289749776L;
    private final JPanel contentPanel = new JPanel();
    private String wsdl;
    private boolean wsdlChanged = false;
    private JTextPane txtWSDL;
    private String previousURL = "";
    private File previousFile = null;

    private XBayaEngine engine;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            WSDLDialog dialog = new WSDLDialog(null, "");
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public WSDLDialog(XBayaEngine engine, String wsdl) {
        setWsdl(wsdl);
        setEngine(engine);
        initGUI();
    }

    private void initGUI() {
        setModal(true);
        setTitle("WSDL Editor");
        setBounds(100, 100, 450, 300);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            txtWSDL = new JTextPane();
            txtWSDL.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent arg0) {
                    setWsdl(txtWSDL.getText());
                }
            });
            contentPanel.add(txtWSDL, BorderLayout.CENTER);
        }
        {
            JPanel panel = new JPanel();
            FlowLayout flowLayout = (FlowLayout) panel.getLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            contentPanel.add(panel, BorderLayout.SOUTH);
            {
                JButton btnUrl = new JButton("URL...");
                btnUrl.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clickFromURL();
                    }
                });
                panel.add(btnUrl);
            }
            {
                JButton btnLoadFromFile = new JButton("File...");
                btnLoadFromFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clickFromFile();
                    }
                });
                panel.add(btnLoadFromFile);
            }
            {
                JButton btnClear = new JButton("Clear");
                btnClear.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        txtWSDL.setText("");
                    }
                });
                panel.add(btnClear);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Update");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        setWsdlChanged(true);
                        close();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setWsdlChanged(false);
                        close();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
    }

    private void clickFromURL() {
        while (true) {
            String newWSDLUrl = JOptionPane.showInputDialog("What is the WSDL url", previousURL);
            if (newWSDLUrl != null) {
                previousURL = newWSDLUrl;
                try {
                    URL url = new URL(newWSDLUrl);
                    setupWSDLFromURL(url);
                    break;
                } catch (MalformedURLException e) {
                    getEngine().getErrorWindow().error(
                            "Error in the specified URL " + newWSDLUrl + " : " + e.getLocalizedMessage(), e);
                }
            } else {
                break;
            }
        }
    }

    private void clickFromFile() {
        JFileChooser c = new JFileChooser();
        FileFilter fileFilter = new FileNameExtensionFilter("*.wsdl", "*.WSDL");
        c.setFileFilter(fileFilter);
        c.setDialogTitle("WSDL file");
        c.setDialogType(JFileChooser.OPEN_DIALOG);
        while (true) {
            if (previousFile != null) {
                c.setSelectedFile(previousFile);
            }
            if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                previousFile = c.getSelectedFile();
                try {
                    URL url = previousFile.toURI().toURL();
                    setupWSDLFromURL(url);
                    break;
                } catch (MalformedURLException e) {
                    getEngine().getErrorWindow().error(
                            "Error in the specified File " + previousFile.toString() + " : " + e.getLocalizedMessage(),
                            e);
                }
            } else {
                break;
            }
        }
    }

    private void setupWSDLFromURL(URL url) {
        try {
            String contentFromURL = getContentFromURL(url);
            txtWSDL.setText(contentFromURL);
            setWsdl(txtWSDL.getText());
        } catch (Exception e) {
            getEngine().getErrorWindow().error(
                    "Error occured while trying to retrieve data from URL " + url.toString() + " : "
                            + e.getLocalizedMessage(), e);
        }
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    public void close() {
        setVisible(false);
    }

    public void open() {
        setVisible(true);
    }

    public boolean isWsdlChanged() {
        return wsdlChanged;
    }

    public void setWsdlChanged(boolean wsdlChanged) {
        this.wsdlChanged = wsdlChanged;
    }

    private String getContentFromURL(URL url) throws IOException {
        InputStream in = url.openStream();
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try {

            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ((n = in.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // already closed
                }
            }
        }
        return bais.toString();
    }

    public XBayaEngine getEngine() {
        return engine;
    }

    public void setEngine(XBayaEngine engine) {
        this.engine = engine;
    }
}
