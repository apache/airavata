/*
 * Copyright (c) 2012 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.graph.system.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextComponent;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.lead.LEADTypes;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;
import org.xmlpull.infoset.XmlElement;

/**
 * @author Chathura Herath
 */
public class DifferedInputConfigurationDialog {
    private XBayaEngine engine;

    private DifferedInputNode node;

    private XBayaDialog dialog;

    private GridPanel gridPanel;

    private XBayaTextField nameTextField;

    private XBayaTextArea descriptionTextArea;

    private XBayaLabel valueLabel;

    private XBayaTextField valueTextField;

    private XBayaTextArea valueTextArea;

    private XBayaTextArea metadataTextArea;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public DifferedInputConfigurationDialog(DifferedInputNode node, XBayaEngine engine) {
        this.engine = engine;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        QName type = this.node.getParameterType();
        XBayaTextComponent textComponent;
        boolean knownType = LEADTypes.isKnownType(type);
        if (knownType) {
            textComponent = this.valueTextField;
            this.valueLabel.setText("Default value");
        } else {
            textComponent = this.valueTextArea;
            this.valueLabel.setText("Default value (in XML)");
        }
        this.valueLabel.setLabelFor(textComponent);
        final int index = 5;
        this.gridPanel.remove(index);
        this.gridPanel.add(textComponent, index);
        if (knownType) {
            this.gridPanel.layout(new double[] { 0, 1.0 / 2, 0, 1.0 / 2 },
                    new double[] { 0, 1 });
        } else {
            this.gridPanel.layout(
                    new double[] { 0, 1.0 / 3, 1.0 / 3, 1.0 / 3 },
                    new double[] { 0, 1 });
        }

        // String name = this.node.getConfiguredName();
        // if (name == null) {
        // name = this.node.getName();
        // }
        String name = this.node.getID(); // Show ID.
        this.nameTextField.setText(name);

        this.descriptionTextArea.setText(this.node.getDescription());
        Object value = this.node.getDefaultValue();
        String valueString;
        if (value == null) {
            valueString = "";
        } else if (value instanceof XmlElement) {
            valueString = XMLUtil.xmlElementToString((XmlElement) value);
        } else {
            valueString = value.toString();
        }
        textComponent.setText(valueString);
        XmlElement metadata = this.node.getMetadata();
        String metadataText;
        if (metadata == null) {
            metadataText = WSConstants.EMPTY_APPINFO;
        } else {
            metadataText = XMLUtil.xmlElementToString(metadata);
        }
        this.metadataTextArea.setText(metadataText);

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
    	
        this.dialog.hide();
        this.node.getGUI().closingDisplay();
    }

    private void setInput() {
        QName type = this.node.getParameterType();
        XBayaTextComponent textComponent;
        if (LEADTypes.isKnownType(type)) {
            textComponent = this.valueTextField;
        } else {
            textComponent = this.valueTextArea;
        }

        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();
        String valueString = textComponent.getText();
        String metadataText = this.metadataTextArea.getText();

        if (name.length() == 0) {
            String warning = "The name cannot be empty.";
            this.engine.getErrorWindow().error(warning);
            return;
        }
        Object value = null;
        if (valueString.length() > 0) {
            if (LEADTypes.isKnownType(type)) {
                if (!this.node.isInputValid(valueString)) {
                    String warning = "The defalut value is not valid for "
                            + this.node.getParameterType() + ".";
                    this.engine.getErrorWindow().error(warning);
                }
                value = valueString;
            } else {
                try {
                    value = XMLUtil.stringToXmlElement(valueString);
                } catch (RuntimeException e) {
                    String warning = "The XML for the default value is not valid.";
                    this.engine.getErrorWindow().error(warning, e);
                }
            }
        }
        XmlElement metadata;
        if (metadataText.length() == 0) {
            metadata = null;
        } else {
            try {
                metadata = XMLUtil.stringToXmlElement(metadataText);
            } catch (RuntimeException e) {
                String warning = "The metadata is ill-formed.";
                this.engine.getErrorWindow().error(warning, e);
                return;
            }
        }

        this.node.setConfigured(true);
        this.node.setConfiguredName(name);
        this.node.setDescription(description);
        this.node.setDefaultValue(value);
        this.node.setMetadata(metadata);
        this.node.getGUI().setBodyColor(NodeState.FINISHED.color);
        
        hide();
        this.engine.getGUI().getGraphCanvas().repaint();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description",
                this.descriptionTextArea);

        this.valueTextField = new XBayaTextField(); // for string
        this.valueTextArea = new XBayaTextArea(); // for XML
        // temporaly set text field.
        this.valueLabel = new XBayaLabel("", this.valueTextField);

        this.metadataTextArea = new XBayaTextArea();
        XBayaLabel metadataLabel = new XBayaLabel("Metadata",
                this.metadataTextArea);

        this.gridPanel = new GridPanel();
        this.gridPanel.add(nameLabel);
        this.gridPanel.add(this.nameTextField);
        this.gridPanel.add(descriptionLabel);
        this.gridPanel.add(this.descriptionTextArea);
        this.gridPanel.add(this.valueLabel);
        this.gridPanel.add(this.valueTextField);
        this.gridPanel.add(metadataLabel);
        this.gridPanel.add(this.metadataTextArea);
        this.gridPanel.layout(4, 2, 3, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            @Override
			public void actionPerformed(ActionEvent e) {
                setInput();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
			public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine,
                "Input Parameter Configuration", this.gridPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2012 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */
