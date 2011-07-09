/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: SwingUtil.java,v 1.5 2008/04/01 21:44:28 echintha Exp $
 */

package org.apache.airavata.xbaya.util;

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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * @author Satoshi Shirasuna
 */
public class SwingUtil {

    /**
     * Minimum size, zero.
     */
    public static final Dimension MINIMUM_SIZE = new Dimension(0, 0);

    /**
     * The default distance between components.
     */
    public static final int PAD = 6;

    /**
     * Default cursor.
     */
    public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * Hand cursor.
     */
    public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    /**
     * Cross hair cursor.
     */
    public static final Cursor CROSSHAIR_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

    /**
     * Move cursor.
     */
    public static final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);

    /**
     * Wait cursor.
     */
    public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

    /**
     * Creates an icon from an image contained in the "images" directory.
     * 
     * @param filename
     * @return the ImageIcon created
     */
    public static ImageIcon createImageIcon(String filename) {
        ImageIcon icon = null;
        String path = "/images/" + filename;
        URL imgURL = SwingUtil.class.getResource(path);
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        }
        return icon;
    }

    /**
     * Return the Frame of a specified component if any.
     * 
     * @param component
     *            the specified component
     * 
     * @return the Frame of a specified component if any; otherwise null
     */
    public static Frame getFrame(Component component) {
        Frame frame;
        Component parent;
        while ((parent = component.getParent()) != null) {
            component = parent;
        }
        if (component instanceof Frame) {
            frame = (Frame) component;
        } else {
            frame = null;
        }
        return frame;
    }

    /**
     * Wight none of rows or eolumns. Used by layoutToGrid().
     */
    public final static int WEIGHT_NONE = -1;

    /**
     * Weight all rows or columns equally. Used by layoutToGrid().
     */
    public final static int WEIGHT_EQUALLY = -2;

    /**
     * Layouts the child components of a specified parent component using GridBagLayout.
     * 
     * @param parent
     *            The specified parent component
     * @param numRow
     *            The number of rows
     * @param numColumn
     *            The number of columns
     * @param weightedRow
     *            The row to weight
     * @param weightedColumn
     *            The column to weight
     */
    public static void layoutToGrid(Container parent, int numRow, int numColumn, int weightedRow, int weightedColumn) {
        GridBagLayout layout = new GridBagLayout();
        parent.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(SwingUtil.PAD, SwingUtil.PAD, SwingUtil.PAD, SwingUtil.PAD);

        for (int row = 0; row < numRow; row++) {
            constraints.gridy = row;
            if (weightedRow == WEIGHT_EQUALLY) {
                constraints.weighty = 1;
            } else if (row == weightedRow) {
                constraints.weighty = 1;
            } else {
                constraints.weighty = 0;
            }
            for (int column = 0; column < numColumn; column++) {
                constraints.gridx = column;
                if (weightedColumn == WEIGHT_EQUALLY) {
                    constraints.weightx = 1;
                } else if (column == weightedColumn) {
                    constraints.weightx = 1;
                } else {
                    constraints.weightx = 0;
                }
                Component component = parent.getComponent(row * numColumn + column);
                layout.setConstraints(component, constraints);
            }
        }
    }

    /**
     * @param parent
     * @param rowWeights
     * @param columnWeights
     */
    public static void layoutToGrid(Container parent, double[] rowWeights, double[] columnWeights) {
        GridBagLayout layout = new GridBagLayout();
        parent.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(SwingUtil.PAD, SwingUtil.PAD, SwingUtil.PAD, SwingUtil.PAD);

        for (int row = 0; row < rowWeights.length; row++) {
            constraints.gridy = row;
            constraints.weighty = rowWeights[row];
            for (int column = 0; column < columnWeights.length; column++) {
                constraints.gridx = column;
                constraints.weightx = columnWeights[column];
                Component component = parent.getComponent(row * columnWeights.length + column);
                layout.setConstraints(component, constraints);
            }
        }
    }

    /**
     * @param parent
     * @param rowWeights
     * @param columnWeights
     */
    @SuppressWarnings("boxing")
    public static void layoutToGrid(Container parent, List<Double> rowWeights, List<Double> columnWeights) {
        GridBagLayout layout = new GridBagLayout();
        parent.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(SwingUtil.PAD, SwingUtil.PAD, SwingUtil.PAD, SwingUtil.PAD);

        for (int row = 0; row < rowWeights.size(); row++) {
            constraints.gridy = row;
            constraints.weighty = rowWeights.get(row);
            for (int column = 0; column < columnWeights.size(); column++) {
                constraints.gridx = column;
                constraints.weightx = columnWeights.get(column);
                Component component = parent.getComponent(row * columnWeights.size() + column);
                layout.setConstraints(component, constraints);
            }
        }
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in a grid. Each
     * component in a column is as wide as the maximum preferred width of the components in that column; height is
     * similarly determined for each row. The parent is made just big enough to fit them all.
     * 
     * @param parent
     * 
     * @param rows
     *            number of rows
     * @param cols
     *            number of columns
     */
    public static void makeSpringCompactGrid(Container parent, int rows, int cols) {
        makeSpringCompactGrid(parent, rows, cols, PAD, PAD, PAD, PAD);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in a grid. Each
     * component in a column is as wide as the maximum preferred width of the components in that column; height is
     * similarly determined for each row. The parent is made just big enough to fit them all.
     * 
     * @param parent
     * 
     * @param rows
     *            number of rows
     * @param cols
     *            number of columns
     * @param initialX
     *            x location to start the grid at
     * @param initialY
     *            y location to start the grid at
     * @param xPad
     *            x padding between cells
     * @param yPad
     *            y padding between cells
     */
    private static void makeSpringCompactGrid(Container parent, int rows, int cols, int initialX, int initialY,
            int xPad, int yPad) {

        SpringLayout layout = new SpringLayout();
        parent.setLayout(layout);

        // Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2007 The Trustees of Indiana University. All rights reserved.
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
