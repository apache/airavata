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
package org.apache.airavata.xbaya.ui.graph;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Node.NodeObserver;
import org.apache.airavata.workflow.model.graph.Node.NodeUpdateType;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler.NodeState;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public abstract class NodeGUI implements GraphPieceGUI, NodeObserver {

    /**
     * BREAK_POINT_BORDER_COLOR
     */
    protected static final Color BREAK_POINT_BORDER_COLOR = new Color(53, 103, 157);

    /**
     * The minimum width of the node.
     */
    protected static final int MINIMUM_WIDTH = 100;

    /**
     * The minimum height of the node
     */
    protected static final int MINIMUM_HEIGHT = 37;

    protected static final int TEXT_GAP_X = 5;

    protected static final int TEXT_GAP_Y = 2;

    protected static final Color TEXT_COLOR = Color.black;

    protected static final int PORT_GAP = 13;

    protected static final int PORT_INITIAL_GAP = 10;

    protected static final Color EDGE_COLOR = Color.GRAY;

    protected static final Color DEFAULT_HEAD_COLOR = Color.white;

    protected static final Color SELECTED_HEAD_COLOR = Color.pink;

    /**
     * The default body color.
     */
    public static final Color DEFAULT_BODY_COLOR = new Color(250, 220, 100);

    protected static final Color DRAGGED_BODY_COLOR = Color.lightGray;

    protected static final Color BREAK_POINT_COLOR = new Color(174, 197, 221);

    protected Node node;

    protected Dimension dimension;

    protected int headHeight;

    protected boolean selected = false;

    protected boolean dragged = false;

    protected Color headColor;

    protected Color bodyColor;

    protected List<Paintable> paintables;
    
    /**
     * @param node
     */
    public NodeGUI(Node node) {
        this.node = node;
        this.bodyColor = DEFAULT_BODY_COLOR;
        this.headColor = DEFAULT_HEAD_COLOR;
        // The followings are just to make sure that it has some size.
        this.dimension = new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        this.paintables = new LinkedList<Paintable>();
        node.registerObserver(this);
    }

    /**
     * Sets the color of the body.
     * 
     * @param color
     *            The color
     */
    public void setBodyColor(Color color) {
        this.bodyColor = color;
    }

    /**
     * @return The color of the body.
     */
    public Color getBodyColor() {
        return this.bodyColor;
    }

    /**
     * Sets the color of the head.
     * 
     * @param color
     *            The color to set
     */
    public void setHeadColor(Color color) {
        this.headColor = color;
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        // Nothing by default
    }

    /**
     * @param paintable
     */
    public void addPaintable(Paintable paintable) {
        this.paintables.add(paintable);
    }

    /**
     * @param paintable
     */
    public void removePaintable(Paintable paintable) {
        this.paintables.remove(paintable);
    }

    /**
     * @param flag
     */
    protected void setSelectedFlag(boolean flag) {
        this.selected = flag;
        if (this.selected) {
            this.headColor = SELECTED_HEAD_COLOR;
        } else {
            this.headColor = DEFAULT_HEAD_COLOR;
        }
    }

    /**
     * @param flag
     */
    protected void setDraggedFlag(boolean flag) {
        this.dragged = flag;
    }

    /**
     * Returns the position of the node.
     * 
     * @return the position of the node
     */
    protected Point getPosition() {
        return getNode().getPosition();
    }

    /**
     * Gets the bounding Rectangle of this Node.
     * 
     * @return A rectangle indicating this component's bounds
     */
    protected Rectangle getBounds() {
        return new Rectangle(getNode().getPosition(), this.dimension);
    }

    /**
     * Checks if a user's click is to select the node.
     * 
     * @param point
     *            The location.
     * @return true if the user's click is to select the node; false otherwise
     */
    protected boolean isIn(Point point) {
        Rectangle bounds = getBounds();
        return bounds.contains(point);
    }

    /**
     * Checks if a user's click is to select the configuration
     * 
     * @param point
     * @return true if the user's click is to select the node, false otherwise
     */
    @SuppressWarnings("unused")
    protected boolean isInConfig(Point point) {
        return false;
    }

    /**
     * Calculates the width of the node and x-coordinate of the ports. This method has to be called before painting any
     * parts of the graph.
     * 
     * @param g
     */
    protected void calculatePositions(Graphics g) {
        Font oldFont = g.getFont();
        g.setFont(new Font(oldFont.getFontName(),Font.BOLD,oldFont.getSize()));
        FontMetrics fm = g.getFontMetrics();

        this.headHeight = fm.getHeight() + TEXT_GAP_Y * 2;

        int maxNumPort = Math.max(getNode().getOutputPorts().size(), getNode().getInputPorts().size());
        this.dimension.height = Math.max(this.headHeight + PORT_INITIAL_GAP + PORT_GAP * maxNumPort, MINIMUM_HEIGHT);
        this.dimension.width = Math.max(MINIMUM_WIDTH, fm.stringWidth(getNode().getID()) + TEXT_GAP_X * 5);

        /* Calculates the position of ports */
        setPortPositions();
        g.setFont(oldFont);
    }

    /**
     * @param g
     */
    protected void paint(Graphics2D g) {
        Shape componentShape = getComponentShape();
        
        // Draws the body.
        drawBody(g, componentShape, getComponentBodyColor());
        
        // Draws the head.
		drawHeader(g, getComponentHeaderShape(), getComponentHeaderText(), getComponentHeaderColor());
        
        // Draw a small circle to indicate the break
        drawBreaks(g, getNode().getPosition());

        // Edge
		drawEdge(g, componentShape, getComponentEdgeColor());

        // Paint all ports
        drawPorts(g, getAllPorts());

        // Paint extras
        drawExtras(g);
    }
    
    /** Following functions need to be overridden for if the component shape/text/color is different **/
	
	protected final Collection<? extends Port> getAllPorts() {
		return getNode().getAllPorts();
	}

	protected Node getNode() {
		return this.node;
	}

	protected final Color getComponentEdgeColor() {
		return EDGE_COLOR;
	}

	protected Color getComponentHeaderColor() {
		return this.headColor;
	}

	protected Shape getComponentHeaderShape() {
		Point position = getNode().getPosition();
		RoundRectangle2D headerBoundaryRect = new RoundRectangle2D.Double(position.x, position.y, this.dimension.width, this.headHeight,DrawUtils.ARC_SIZE, DrawUtils.ARC_SIZE);
		return headerBoundaryRect;
	}

	protected final Color getComponentBodyColor() {
		Color paintBodyColor;
        if (this.dragged) {
        	paintBodyColor=DRAGGED_BODY_COLOR;
        } else {
        	paintBodyColor=this.bodyColor;
        }
		return paintBodyColor;
	}

	protected Shape getComponentShape() {
		Point position = getNode().getPosition();
        RoundRectangle2D completeComponentBoundaryRect = new RoundRectangle2D.Float(position.x, position.y, this.dimension.width, this.dimension.height, DrawUtils.ARC_SIZE, DrawUtils.ARC_SIZE);
		return completeComponentBoundaryRect;
	}

	protected String getComponentHeaderText() {
		// XXX it's debatable if we should show the ID or the name.
        // String headerText = this.node.getName();
        String headerText = getNode().getID();
		return headerText;
	}

	/**---------------------------------------------------------------------------------**/
	
	protected void drawBody(Graphics2D g,
			Shape shape, Color paintBodyColor) {
		DrawUtils.initializeGraphics2D(g);
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.translate(5,5);
		Shape shadow = affineTransform.createTransformedShape(shape);
		Composite oldComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65F));
		g.setColor(Color.GRAY);
		g.fill(shadow);
		g.setComposite(oldComposite);
		DrawUtils.gradientFillShape(g, getEndColor(paintBodyColor), paintBodyColor, shape);
	}

	protected void drawBreaks(Graphics2D g, Point position) {
		if (getNode().isBreak()) {
			DrawUtils.initializeGraphics2D(g);
            g.setColor(BREAK_POINT_COLOR);
            int r = this.headHeight / 4;
            g.fillOval(position.x + this.dimension.width - 3 * r, position.y + r, 2 * r, 2 * r);
            g.setColor(BREAK_POINT_BORDER_COLOR);
            g.drawOval(position.x + this.dimension.width - 3 * r, position.y + r, 2 * r, 2 * r);
        }
	}

	protected void drawExtras(Graphics2D g) {
		DrawUtils.initializeGraphics2D(g);
		for (Paintable paintable : this.paintables) {
            paintable.paint(g, getNode().getPosition());
        }
	}

	protected void drawPorts(Graphics2D g, Collection<? extends Port> ports) {
		DrawUtils.initializeGraphics2D(g);
		for (Port port : ports) {
            NodeController.getGUI(port).paint(g);
        }
	}
	
	protected void drawPorts(Graphics2D g, Node node) {
		drawPorts(g, node.getAllPorts());
	}

	protected void drawEdge(Graphics2D g,
			Shape completeComponentBoundaryShape, Color edgeColor) {
		DrawUtils.initializeGraphics2D(g);
		g.setColor(edgeColor);
		//uncomment the commented lines to enable a double line edge
//		g.setStroke(new BasicStroke(4.0f));
        g.draw(completeComponentBoundaryShape);
//        g.setColor(Color.white);
//        g.setStroke(new BasicStroke(3.0f));
//        g.draw(completeComponentBoundaryShape);
	}
	protected void drawHeader(Graphics2D g, Shape shape,
			String headerText, Color headColor, boolean lowerBorderflat) {
		drawHeader(g, shape, headerText, headColor, shape, lowerBorderflat);
	}
	
	protected void drawHeader(Graphics2D g, Shape shape,
			String headerText, Color headColor) {
		drawHeader(g, shape, headerText, headColor, true);
	}
	
	protected void drawHeader(Graphics2D g, Shape shape,
			String headerText, Color headColor,
			Shape headerDrawBoundaryShape) {
		drawHeader(g, shape, headerText, headColor, headerDrawBoundaryShape, true);
	}
	
	protected void drawHeader(Graphics2D g, Shape shape,
			String headerText, Color headColor,
			Shape headerDrawBoundaryShape, boolean lowerBorderflat) {
		DrawUtils.initializeGraphics2D(g);
        if (lowerBorderflat) {
    		g.setColor(getEndColor(headColor));
    		Rectangle rect=new Rectangle((int) shape.getBounds().getX()+1, (int) (shape.getBounds()
					.getY() + shape.getBounds().getHeight() - DrawUtils.ARC_SIZE),
					(int) shape.getBounds().getWidth(), DrawUtils.ARC_SIZE);
			DrawUtils.gradientFillShape(g, getEndColor(headColor), headColor, rect);
		}
        DrawUtils.gradientFillShape(g, getEndColor(headColor), headColor, headerDrawBoundaryShape);
        
        // Text
        g.setColor(TEXT_COLOR);
        Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getFontName(),Font.BOLD,oldFont.getSize()));
        Rectangle2D bounds = g.getFontMetrics().getStringBounds(headerText, g);
        g.drawString(headerText, (int)(shape.getBounds().getX() + (shape.getBounds().getWidth()-bounds.getWidth())/2), 
		(int)(shape.getBounds().getY() + (shape.getBounds().getHeight()+bounds.getHeight())/2));
        g.setFont(oldFont);
	}

    /**
     * Sets up the position of ports
     */
    protected void setPortPositions() {
        // inputs
        List<? extends Port> inputPorts = getNode().getInputPorts();
        for (int i = 0; i < inputPorts.size(); i++) {
            Port port = inputPorts.get(i);
            Point offset = new Point(PortGUI.DATA_PORT_SIZE / 2, this.headHeight + PORT_INITIAL_GAP + PORT_GAP * i);
            NodeController.getGUI(port).setOffset(offset);
        }

        // outputs
        List<? extends Port> outputPorts = getNode().getOutputPorts();
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            // Use getBounds() instead of this.dimension because subclass might
            // overwrite getBounds() to have different shape.
            Point offset = new Point(this.getBounds().width - PortGUI.DATA_PORT_SIZE / 2, this.headHeight
                    + PORT_INITIAL_GAP + PORT_GAP * i);
            NodeController.getGUI(port).setOffset(offset);
        }

        // control-in
        Port controlInPort = getNode().getControlInPort();
        if (controlInPort != null) {
        	NodeController.getGUI(controlInPort).setOffset(new Point(0, 0));
        }

        // control-outs
        for (Port controlOutPort : getNode().getControlOutPorts()) {
            // By default, all ports will be drawn at the same place. Subclass
            // should rearrange them if there are more than one control-out
            // ports.
        	NodeController.getGUI(controlOutPort).setOffset(new Point(getBounds().width, getBounds().height));
        }
    }

    /**
     * @param workflowName
     * @param state
     */
    public void setToken(String workflowName, NodeState state) {
        List<DataPort> inputPorts = getNode().getInputPorts();
        switch (state) {
        case EXECUTING:

            for (DataPort dataPort : inputPorts) {
            	NodeController.getGUI(((DataPort) dataPort.getFromPort())).removeToken(workflowName);
            	NodeController.getGUI(dataPort).addToken(workflowName);
            }
            break;
        case FINISHED:
            for (DataPort dataPort : inputPorts) {
            	NodeController.getGUI(dataPort).removeToken(workflowName);
            }

            List<DataPort> outputPorts = getNode().getOutputPorts();
            for (DataPort dataPort : outputPorts) {
            	NodeController.getGUI(dataPort).addToken(workflowName);
            }
            break;
        case FAILED:

            break;

        }

    }

    /**
	 * 
	 */
    public void resetTokens() {

        List<DataPort> inputPorts = getNode().getInputPorts();
        for (DataPort dataPort : inputPorts) {
            NodeController.getGUI(dataPort).reset();
        }
        List<DataPort> outputPorts = getNode().getOutputPorts();
        for (DataPort dataPort : outputPorts) {
            NodeController.getGUI(dataPort).reset();
        }
    }
    
    protected Color getEndColor(Color bodyColor){
    	return Color.white;
    }
    
    @Override
    public void nodeUpdated(NodeUpdateType type) {
    	switch(type){
    	case STATE_CHANGED:
    		updateNodeColor();
    		break;
		default:
			break;
    	}
    	
    }

	private void updateNodeColor() {
		switch(node.getState()){
		case WAITING:
			setBodyColor(NodeState.DEFAULT.color); break;
		case EXECUTING:
			setBodyColor(NodeState.EXECUTING.color); break;
		case FAILED:
			setBodyColor(NodeState.FAILED.color); break;
		case FINISHED:
			setBodyColor(NodeState.FINISHED.color); break;
		}
	}
}