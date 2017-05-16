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

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


/**
 * @param <T>
 */
public class XbayaEnhancedList<T extends TableRenderable> implements XBayaComponent {

    private static final int DEFAULT_WIDTH = 400;

    private static final int DEFAULT_HEIGHT = 200;

    private boolean checkbox;

    private DefaultTableModel model;

    private JTable table;

    private JScrollPane scrollPane;

    private Vector<T> tableList;

    /**
     * Constructs a XbayaEnhancedList.
     * 
     */
    public XbayaEnhancedList() {
        this(true);
    }

    /**
     * 
     * Constructs a XbayaEnhancedList.
     * 
     * @param checkbox
     */
    public XbayaEnhancedList(boolean checkbox) {
        this.checkbox = checkbox;
        init();
    }

    /**
     * Init XbayaEnhancedList
     */
    private void init() {

        this.tableList = new Vector<T>();

        this.table = new JTable(new DefaultTableModel());
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.table.setRowSelectionAllowed(true);

        this.scrollPane = new JScrollPane(this.table);
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * @return The swing component.
     */
    public JScrollPane getSwingComponent() {
        return getScrollPane();
    }

    /**
     * @return The scroll pane.
     */
    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    /**
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        Dimension size = new Dimension(width, height);
        this.scrollPane.setMinimumSize(size);
        this.scrollPane.setPreferredSize(size);
    }

    /**
     * @return table
     */
    public JTable getTable() {
        return this.table;
    }

    /**
     * @param tableData
     * @param listData
     */
    public void setListData(Iterable<T> tableData) {

        /*
         * Create a model for the table for the first time
         */
        if (this.model == null) {

            this.model = new DefaultTableModel() {
                /*
                 * JTable uses this method to determine the default renderer/ editor for each cell. If we didn't
                 * implement this method, then the last column would contain text ("true"/"false"), rather than a check
                 * box.
                 */
                @SuppressWarnings("unchecked")
                @Override
                public Class getColumnClass(int c) {
                    if (getValueAt(0, c) == null)
                        return String.class;
                    return getValueAt(0, c).getClass();
                }

                /*
                 * Don't need to implement this method unless your table's editable.
                 */
                @Override
                public boolean isCellEditable(int row, int col) {
                    // Note that the data/cell address is constant,
                    // no matter where the cell appears onscreen.
                    if (XbayaEnhancedList.this.checkbox && col > 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            };

            /*
             * Setup Column Title
             */
            boolean noData = true;
            for (T entry : tableData) {
                if (this.checkbox) {
                    this.model.addColumn("Selection");
                }

                for (int i = 0; i < entry.getColumnCount(); i++) {
                    this.model.addColumn(entry.getColumnTitle(i));
                }
                noData = false;
                break;
            }

            // empty input
            if (noData) {
                this.model = null;
                return;
            }

            this.table.setModel(this.model);
        }

        clear();

        ArrayList<Object> objList = new ArrayList<Object>();
        for (T entry : tableData) {

            // add checkbox if needed
            if (this.checkbox) {
                objList.add(Boolean.FALSE);
            }

            for (int i = 0; i < entry.getColumnCount(); i++) {
                objList.add(entry.getValue(i));
            }
            this.model.addRow(objList.toArray());
            this.tableList.add(entry);

            // clear list
            objList.clear();
        }
    }

    /**
     * @return T
     */
    public T getSelectedValue() {
        int result = getSelectedIndex();
        if (result < 0) {
            return null;
        }
        return this.tableList.get(result);
    }

    /**
     * @return selected values
     */
    public List<T> getSelectedValues() {
        List<T> resultList = new ArrayList<T>();
        for (Integer i : getSelectedIndices()) {
            resultList.add(this.tableList.get(i.intValue()));
        }
        return resultList;
    }

    /**
     * remove rows selected This method must be called at the last step
     */
    public void removeSelectedRows() {
        int count = 0;
        for (Integer i : getSelectedIndices()) {
            this.model.removeRow(i.intValue() - count);
            this.tableList.remove(i.intValue() - count);
            count++;
        }
    }

    /**
     * Clear the list contents
     */
    public void clear() {
        if (this.model != null) {
            for (int i = this.model.getRowCount() - 1; i >= 0; i--) {
                this.model.removeRow(i);
            }
        }
        this.tableList.clear();
    }

    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.table.setEnabled(enabled);
    }

    /**
     * Returns the first selected index; returns -1 if there is no selected item.
     * 
     * @return The first selected index; -1 if there is no selected item.
     */
    public int getSelectedIndex() {
        List<Integer> intList = getSelectedIndices();
        if (intList.size() > 1) {
            return -2;
        } else if (intList.size() == 0) {
            return -1;
        }
        return intList.get(0).intValue();
    }

    /**
     * @return selected indices
     */
    public List<Integer> getSelectedIndices() {
        List<Integer> intList = new ArrayList<Integer>();

        if (!this.checkbox) {
            intList.add(new Integer(this.table.getSelectedRow()));
        } else {
            for (int i = 0; i < this.getTable().getModel().getRowCount(); i++) {
                if (((Boolean) this.getTable().getModel().getValueAt(i, 0)).booleanValue()) {
                    intList.add(new Integer(i));
                }
            }
        }
        return intList;
    }

    /**
     * @param listener
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        this.table.getSelectionModel().addListSelectionListener(listener);
    }

    /**
     * @param adapter
     */
    public void addMouseListener(MouseAdapter adapter) {
        this.table.addMouseListener(adapter);
    }
}