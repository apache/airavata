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
package org.apache.airavata.xbaya.ui.widgets;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.airavata.common.utils.SwingUtil;

public class GridPanel implements XBayaComponent {

    /**
     * Wight none of rows or eolumns. Used by layout().
     */
    public final static int WEIGHT_NONE = SwingUtil.WEIGHT_NONE;

    /**
     * Weight all rows or columns equally. Used by layout().
     */
    public final static int WEIGHT_EQUALLY = SwingUtil.WEIGHT_EQUALLY;

    private JPanel contentPanel;

    private JComponent rootComponent;
    
    private boolean scroll=false;

    /**
     * Constructs a GridPanel.
     */
    public GridPanel() {
        this(false);
    }

    /**
     * Constructs a GridPanel.
     * 
     * @param scroll
     *            true if scroll is needed; false otherwise.
     */
    public GridPanel(boolean scroll) {
        init(scroll);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.widgets.XBayaComponent#getSwingComponent()
     */
    public JComponent getSwingComponent() {
        return this.rootComponent;
    }

    /**
     * @return The panel.
     */
    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    /**
     * @param component
     */
    public void add(XBayaComponent component) {
        add(component.getSwingComponent());
    }

    /**
     * @param component
     * @param index
     */
    public void add(XBayaComponent component, int index) {
        add(component.getSwingComponent(), index);
    }

    /**
     * @param component
     */
    public void add(JComponent component) {
        this.contentPanel.add(component);
    }

    /**
     * @param component
     * @param index
     */
    public void add(JComponent component, int index) {
        this.contentPanel.add(component, index);
    }

    /**
     * @param index
     */
    public void remove(int index) {
        this.contentPanel.remove(index);
    }

    /**
     * Layouts the child components of a specified parent component using GridBagLayout.
     * 
     * @param numRow
     *            The number of rows
     * @param numColumn
     *            The number of columns
     * @param weightedRow
     *            The row to weight
     * @param weightedColumn
     *            The column to weight
     */
    public void layout(int numRow, int numColumn, int weightedRow, int weightedColumn) {
        SwingUtil.layoutToGrid(this.contentPanel, numRow, numColumn, weightedRow, weightedColumn);
    }

    /**
     * @param rowWeights
     * @param columnWeights
     */
    public void layout(double[] rowWeights, double[] columnWeights) {
        SwingUtil.layoutToGrid(this.contentPanel, rowWeights, columnWeights);
    }

    /**
     * @param rowWeights
     * @param columnWeights
     */
    public void layout(List<Double> rowWeights, List<Double> columnWeights) {
        SwingUtil.layoutToGrid(this.contentPanel, rowWeights, columnWeights);
    }

    private void init(boolean scroll) {
        this.contentPanel = new JPanel();
        if (scroll) {
            this.contentPanel.setOpaque(true);
            JScrollPane scrollPane = new JScrollPane(this.contentPanel);
            this.rootComponent = scrollPane;
        } else {
            this.rootComponent = this.contentPanel;
        }
        this.scroll=scroll;
    }
    
    public void resetPanel(){
    	init(scroll);
    }
}