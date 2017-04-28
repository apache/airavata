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
package org.apache.airavata.xbaya.ui.views;

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

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.system.ParameterNode;
import org.apache.airavata.xbaya.core.workflow.ParameterListModel;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaComponent;
import org.apache.airavata.xbaya.ui.widgets.XBayaList;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.xmlpull.infoset.XmlElement;

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
     * @see org.apache.airavata.xbaya.ui.widgets.XBayaComponent#getSwingComponent()
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