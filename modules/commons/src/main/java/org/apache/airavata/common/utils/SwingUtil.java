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
package org.apache.airavata.common.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

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
        URL imgURL = getImageURL(filename);
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        }
        return icon;
    }

    /**
     * Creates an image from an image contained in the "images" directory.
     * 
     * @param filename
     * @return the Image created
     */
    public static Image createImage(String filename) {
    	Image icon = null;
        URL imgURL = getImageURL(filename);
        if (imgURL != null) {
            icon = Toolkit.getDefaultToolkit().getImage(imgURL);
        }
        return icon;
    }

	public static URL getImageURL(String filename) {
		String path = "/images/" + filename;
        URL imgURL = SwingUtil.class.getResource(path);
		return imgURL;
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
    
    public static void addPlaceHolder(final JTextField field,final String placeHolderText){
    	field.addFocusListener(new FocusListener(){
    		private Color fontColor=field.getForeground();
//    		private String previousText=field.getText();
    		
			public void focusGained(FocusEvent arg0) {
				if (field.getText().equals(placeHolderText)){
					field.setText("");
				}
				field.setForeground(fontColor);
			}

			public void focusLost(FocusEvent arg0) {
				if (field.getText().trim().equals("")){
					fontColor=field.getForeground();
					field.setForeground(Color.GRAY);
					field.setText(placeHolderText);
				}
			}
    	});
    	if (field.getText().trim().equals("")){
    		field.setText(placeHolderText);
    		field.setForeground(Color.GRAY);
    	}
    }
    
}