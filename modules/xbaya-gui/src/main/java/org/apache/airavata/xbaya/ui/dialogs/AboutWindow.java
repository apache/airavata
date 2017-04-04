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
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.apache.airavata.common.utils.BrowserLauncher;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul5.XmlConstants;

public class AboutWindow {

    private static final Logger logger = LoggerFactory.getLogger(AboutWindow.class);

    private XBayaEngine engine;

    private XBayaDialog dialog;

    /**
     * Creates the AboutWindow.
     * 
     * @param engine
     */
    public AboutWindow(XBayaEngine engine) {
        this.engine = engine;
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
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.setDefaultCapable(true);
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPanel.add(okButton);
        String styles="<style type=\"text/css\">"+
		        		"body {"+
			        	"font-family:Arial, Helvetica, sans-serif"+
			        	"}"+
        				".centeredImage"+
        				"{"+
        				"	text-align:center;"+
        				"	display:block;"+
        				"}"+
        				"</style>";
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        String imgLogoTag="";//"<img src=\""+SwingUtil.getImageURL("airavata-2.png").toString()+"\" class=\".centeredImage\" /><br>";
        String imgHeadingTag="<div style=\"width:100%;margin-right:10px;margin-left:10px;margin-top:5px;\" ><img src=\""+SwingUtil.getImageURL("airavata-title-text.png").toString()+"\" class=\".centeredImage\" /></div><br>";
        String projectNameText = "";//"<h1>" + XBayaConstants.PROJECT_NAME + "</h1>";
		String message = "<html>"+ styles +"<body align=\"center\">" + "<div style=\"background-color:white;width:100%;margin-bottom:0px;\">"+projectNameText + imgHeadingTag+ imgLogoTag +"</div><br><h2>" +XBayaConstants.APPLICATION_NAME + "</h2>" + "Version: "
				+ XBayaVersion.VERSION + "<br>" + "<a href='" + XBayaConstants.WEB_URL.toString() + "'>" + XBayaConstants.WEB_URL.toString() + "</a>"
				+ "<br>&nbsp</body></html>";
        JEditorPane editorPane = new JEditorPane(XmlConstants.CONTENT_TYPE_HTML, message);
        editorPane.setEditable(false);
        editorPane.setBorder(BorderFactory.createEtchedBorder());
        editorPane.setBackground(buttonPanel.getBackground());
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent event) {
                logger.debug("Event:" + event);
                if (event.getEventType() == EventType.ACTIVATED) {
                    URL url = event.getURL();
                    try {
                        BrowserLauncher.openURL(url.toString());
                    } catch (Exception e) {
                        AboutWindow.this.engine.getGUI().getErrorWindow().error(AboutWindow.this.dialog.getDialog(),
                                e.getMessage(), e);
                    }
                }
            }
        });

        this.dialog = new XBayaDialog(this.engine.getGUI(), XBayaConstants.PROJECT_NAME, editorPane, buttonPanel);
        this.dialog.setDefaultButton(okButton);
//        this.dialog.setCancelButton(okButton);
    }
}