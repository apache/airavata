/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ParameterPropertyPanel.java,v 1.6 2008/04/01 21:44:32 echintha Exp $
 */
package org.apache.airavata.xbaya.wf.gui;

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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.airavata.xbaya.graph.system.ParameterNode;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComponent;
import org.apache.airavata.xbaya.gui.XBayaList;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.util.WSConstants;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.infoset.XmlElement;

/**
 * @author Satoshi Shirasuna
 */
public class ParameterPropertyPanel implements XBayaComponent {

    private String title;

    private XBayaList parameterList;

    private ParameterListModel listModel;

    private GridPanel panel;

    private XBayaTextArea metadataTextArea;

    private JButton upButton;

    private JButton downButton;

    /**
     * Constructs a ParameterReorderingPanel.
     * 
     * @param title
     */
    public ParameterPropertyPanel(String title) {
        this.title = title;
        initGUI();
    }

    /**
     * @see org.apache.airavata.xbaya.gui.XBayaComponent#getSwingComponent()
     */
    public JComponent getSwingComponent() {
        return getPanel().getSwingComponent();
    }

    /**
     * @return The Swing Panel.
     */
    public GridPanel getPanel() {
        return this.panel;
    }

    /**
     * @param parameterNodes
     * @param nodes
     */
    public void setParameterNodes(List<? extends ParameterNode> parameterNodes) {
        this.listModel = new ParameterListModel(parameterNodes);
        this.parameterList.getList().setModel(this.listModel);
    }

    /**
     * @param metadata
     */
    public void setMetadata(XmlElement metadata) {
        String metadataText;
        if (metadata == null) {
            metadataText = WSConstants.EMPTY_APPINFO;
        } else {
            metadataText = XMLUtil.xmlElementToString(metadata);
        }
        this.metadataTextArea.setText(metadataText);
    }

    /**
     * @return The metadata.
     */
    public String getMetadata() {
        return this.metadataTextArea.getText();
    }

    private void up() {
        int index = this.parameterList.getSelectedIndex();
        this.parameterList.setSelectedIndex(index - 1);
        this.listModel.up(index);
    }

    private void down() {
        int index = this.parameterList.getSelectedIndex();
        this.parameterList.setSelectedIndex(index + 1);
        this.listModel.down(index);
    }

    private void selectionChanged() {
        int index = this.parameterList.getSelectedIndex();
        if (index < 0) {
            // Nothing is selected.
            this.upButton.setEnabled(false);
            this.downButton.setEnabled(false);
        } else {
            if (index == 0) {
                this.upButton.setEnabled(false);
            } else {
                this.upButton.setEnabled(true);
            }
            if (index == this.listModel.getSize() - 1) {
                this.downButton.setEnabled(false);
            } else {
                this.downButton.setEnabled(true);
            }
        }
    }

    private void initGUI() {
        this.parameterList = new XBayaList();
        this.parameterList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                selectionChanged();
            }
        });

        this.upButton = new JButton("UP");
        this.upButton.setEnabled(false);
        this.upButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                up();
            }
        });

        this.downButton = new JButton("DOWN");
        this.downButton.setEnabled(false);
        this.downButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                down();
            }
        });

        GridPanel buttonPanel = new GridPanel();
        buttonPanel.add(this.upButton);
        buttonPanel.add(this.downButton);
        buttonPanel.layout(2, 1, GridPanel.WEIGHT_NONE, 0);

        GridPanel reorderingPanel = new GridPanel();
        reorderingPanel.add(this.parameterList);
        reorderingPanel.add(buttonPanel);
        reorderingPanel.layout(1, 2, 0, 0);

        this.metadataTextArea = new XBayaTextArea();
        JLabel metadataLabel = new JLabel("Metadata");
        metadataLabel.setLabelFor(this.metadataTextArea.getSwingComponent());

        this.panel = new GridPanel();
        this.panel.add(reorderingPanel);
        this.panel.add(metadataLabel);
        this.panel.add(this.metadataTextArea);
        this.panel.layout(3, 1, 2, 0);

        TitledBorder border = BorderFactory.createTitledBorder(this.title);
        this.panel.getSwingComponent().setBorder(border);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
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
