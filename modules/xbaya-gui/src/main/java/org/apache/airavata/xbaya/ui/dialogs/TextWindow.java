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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.apache.airavata.common.utils.BrowserLauncher;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul5.XmlConstants;

public class TextWindow {

    private static final Logger logger = LoggerFactory.getLogger(TextWindow.class);

    private XBayaEngine engine;

    private XBayaDialog dialog;
    
    private String key;
    private String value;
    private String title;
    
    /**
     * Creates the AboutWindow.
     * 
     * @param engine
     */
    public TextWindow(XBayaEngine engine, String key, String value, String title) {
        this.engine = engine;
        this.key=key;
        this.value=value;
        this.title=title;
        init();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    @SuppressWarnings("serial")
	private void init() {
        final JEditorPane editorPane = new JEditorPane(XmlConstants.CONTENT_TYPE_HTML, StringUtil.createHTMLUrlTaggedString(value));
        editorPane.setEditable(false);
        editorPane.setBackground(Color.WHITE);
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent event) {
                logger.debug("Event:" + event);
                if (event.getEventType() == EventType.ACTIVATED) {
                    URL url = event.getURL();
                    try {
                        BrowserLauncher.openURL(url.toString());
                    } catch (Exception e) {
                        TextWindow.this.engine.getGUI().getErrorWindow().error(TextWindow.this.dialog.getDialog(),
                                e.getMessage(), e);
                    }
                }
            }
        });
        JScrollPane pane = new JScrollPane(editorPane);
        GridPanel gridPanel = new GridPanel();
        XBayaTextField txtKey=new XBayaTextField(key);
        txtKey.getTextField().setEditable(false);
        gridPanel.add(txtKey);
        gridPanel.add(pane);
        gridPanel.getContentPanel().setBorder(BorderFactory.createEtchedBorder());
        gridPanel.layout(2, 1, 1, 0);
        
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Close");
        okButton.setDefaultCapable(true);
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        JButton btnCopy = new JButton("Copy to clipboard");
        btnCopy.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
			}
        	
        });
        buttonPanel.add(btnCopy);
        buttonPanel.add(okButton);
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        
        this.dialog = new XBayaDialog(this.engine.getGUI(), title, gridPanel, buttonPanel);
        dialog.getDialog().setMinimumSize(new Dimension(400, 400));
        this.dialog.setDefaultButton(okButton);
    }
}
