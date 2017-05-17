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
import java.util.Collection;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;


/**
 * @param <E>
 */
public class XBayaList<E> implements XBayaComponent {

    private static final int DEFAULT_WIDTH = 300;

    private static final int DEFAULT_HEIGHT = 200;

    private JList list;

    private JScrollPane scrollPane;

    /**
     * Constructs a XBayaTextArea.
     */
    public XBayaList() {
        init();
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
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.list.setEnabled(enabled);
    }

    /**
     * @return The text area
     */
    public JList getList() {
        return this.list;
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
     * Returns the first selected index; returns -1 if there is no selected item.
     * 
     * @return The first selected index; -1 if there is no selected item.
     */
    public int getSelectedIndex() {
        return this.list.getSelectedIndex();
    }

    /**
     * Selects a single cell.
     * 
     * @param index
     *            the index of the one cell to select
     */
    public void setSelectedIndex(int index) {
        this.list.setSelectedIndex(index);
    }

    /**
     * Returns the first selected value, or <code>null</code> if the selection is empty.
     * 
     * @return the first selected value
     */
    @SuppressWarnings("unchecked")
    public E getSelectedValue() {
        return (E) this.list.getSelectedValue();
    }

    /**
     * @param listData
     */
    public void setListData(Iterable<E> listData) {
        if (listData instanceof Vector) {
            this.list.setListData((Vector) listData);
        } else if (listData instanceof Collection) {
            this.list.setListData(new Vector<E>((Collection<E>) listData));
        } else {
            Vector<E> data = new Vector<E>();
            for (E datum : data) {
                data.add(datum);
            }
            this.list.setListData(data);
        }
    }

    /**
     * @param listData
     */
    public void setListData(E[] listData) {
        this.list.setListData(listData);
    }

    /**
     * @param listener
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        this.list.addListSelectionListener(listener);
    }

    /**
     * @param adapter
     */
    public void addMouseListener(MouseAdapter adapter) {
        this.list.addMouseListener(adapter);
    }

    private void init() {
        this.list = new JList();
        this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.scrollPane = new JScrollPane(this.list);
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

}