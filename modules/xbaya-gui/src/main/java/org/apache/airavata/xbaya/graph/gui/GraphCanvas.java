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

package org.apache.airavata.xbaya.graph.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.gui.ComponentSourceTransferable;
import org.apache.airavata.xbaya.component.registry.ComponentReference;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Edge;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphPiece;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.Port.Kind;
import org.apache.airavata.xbaya.graph.dynamic.DynamicNode;
import org.apache.airavata.xbaya.graph.dynamic.PortAddable;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.gui.StreamSourceNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.XBayaExecutionModeListener;
import org.apache.airavata.xbaya.interpretor.XBayaExecutionState;
import org.apache.airavata.xbaya.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;


/**
 * A canvas to display a graph (workflow).
 * 
 */
public class GraphCanvas implements XBayaExecutionModeListener{

    private static final Logger logger = LoggerFactory.getLogger(GraphCanvas.class);

    private XBayaEngine engine;

    private JPanel panel;

    private JScrollPane scrollPane;

    private List<GraphCanvasListener> listeners;

    private Workflow workflow;

    private Graph graph;

    private Node selectedNode;

    private Node draggedNode;

    private Port selectedOutputPort;

    private Port selectedInputPort;

    private Edge selectedEdge;

    private Port draggedPort;

    private Dimension graphDimention;

    private JPopupMenu edgePopup;

    private JPopupMenu nodePopup;

    private Point mousePoint;

    private JMenuItem rerunItem;

    private JMenuItem breakPointItem;

    private PortAddable dynamicNodeWithFreePort;

    private File workflowFile;
    
    /*
     * For multiple selection
     */
    private boolean crtlPressed;

    private Point mousePointForSelection;

    private List<Node> multipleSelectedNodes;

    private XmlElement originalWorkflowElement;
    
    boolean editable=false;
    /**
     * Creates a GraphPanel.
     * 
     * @param engine
     *            The XBayaEngine
     */
    public GraphCanvas(XBayaEngine engine) {

        this.engine = engine;

        this.listeners = new LinkedList<GraphCanvasListener>();

        // To avoid null check. Do not call newWorkflow() here because something
        // are not initialized yet at this point.
        this.workflow = new Workflow();
        this.graph = this.workflow.getGraph();
        engine.getConfiguration().registerExecutionModeChangeListener(this);
        graph.setName(generateNewWorkflowName());
        initGUI();
        executionModeChanged(engine.getConfiguration());
    }

	private String generateNewWorkflowName() {
		String baseName="Workflow";
        List<GraphCanvas> graphCanvases = this.engine.getGUI().getGraphCanvases();
        List<String> existingNames=new ArrayList<String>();
        for (GraphCanvas graphCanvas : graphCanvases) {
        	existingNames.add(graphCanvas.getWorkflow().getName());
		}
        int i=1;
        String newName=baseName+i;
        while(existingNames.contains(newName)){
        	i++;
        	newName=baseName+i;
        }
		return newName;
	}

    /**
     * @return The panel.
     */
    public JComponent getSwingComponent() {
        return this.scrollPane;
    }

    /**
     * @return The workflow
     */
    public Workflow getWorkflow() {
        return this.workflow;
    }

    /**
     * Returns the workflow.
     * 
     * @return The workflow
     */
    public synchronized Workflow getWorkflowWithImage() {
        BufferedImage image = createImage();
        this.workflow.setImage(image);
        return this.workflow;
    }

    /**
     * @return the current graph
     */
    public synchronized Graph getGraph() {
        return this.graph;
    }

    /**
     * Sets workflow.
     * 
     * @param workflow
     *            The workflow to set.
     */
    public synchronized void setWorkflow(Workflow workflow) {
        reset();
        this.workflow = workflow;
        this.graph = this.workflow.getGraph();
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.GRAPH_LOADED, this, this.workflow));
        updateSize();
        this.panel.repaint();
        updateOriginalWorkflowElement();
        executionModeChanged(engine.getConfiguration());
    }

    /**
     * Creates a new graph.
     */
    public synchronized void newWorkflow() {
        Workflow newWorkflow = new Workflow();
        setWorkflow(newWorkflow);
    }

    /**
     * @param name
     * @param description
     */
    public void setNameAndDescription(String name, String description) {
        this.workflow.setName(name);
        this.workflow.setDescription(description);
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.NAME_CHANGED, this, this.workflow));
    }

    /**
     * Creates a new Node from a specified Component and adds it.
     * 
     * @param component
     *            The Component to add.
     * @param location
     *            The location to add the node.
     */
    public synchronized Node addNode(Component component, Point location) {
        if (component != null) {
            Node node = this.workflow.addNode(component);
            node.setPosition(location);
            selectNode(node);
            updateSize();
            this.panel.repaint();
            return node;
        }
        return null;
    }

    /**
     * Creates a new Node from a specified Component and adds it.
     * 
     * @param component
     *            The Component to add.
     */
    public void addNode(Component component) {
        Point location = getRandomPosition();
        addNode(component, location);
    }

    /**
     * Removes the selected graph piece if any.
     * 
     * @throws GraphException
     */
    public synchronized void removeSelected() throws GraphException {
        removeSelectedEdge();
        removeSelectedNode();
    }

    /**
     * Removes the selected Node if any
     * 
     * @throws GraphException
     */
    public synchronized void removeSelectedNode() throws GraphException {
        if (this.selectedNode != null) {

            // deselect ports if they belong to this node.
            if (this.selectedNode.containsPort(this.selectedInputPort)) {
                deselectInputPort();
            }
            if (this.selectedNode.containsPort(this.selectedOutputPort)) {
                deselectOutputPort();
            }

            this.workflow.removeNode(this.selectedNode);
            deselectNode();

            updateSize();
            this.panel.repaint();
        }

        /*
         * Delete multiple nodes as well
         */
        if (this.multipleSelectedNodes != null) {

            for (Node node : this.multipleSelectedNodes) {
                // deselect ports if they belong to this node.
                if (node.containsPort(this.selectedInputPort)) {
                    deselectInputPort();
                }
                if (node.containsPort(this.selectedOutputPort)) {
                    deselectOutputPort();
                }

                this.workflow.removeNode(node);
            }
            deselectNode();

            updateSize();
            this.panel.repaint();
        }
    }

    /**
     * Removes the selected edge if any.
     */
    public synchronized void removeSelectedEdge() {
        try {
            if (this.selectedEdge != null) {
                this.graph.removeEdge(this.selectedEdge);
                deselectEdge();
                this.panel.repaint();
            }
        } catch (GraphException e) {
            // Should not happen
            logger.error(e.getMessage(), e);
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);

        }
    }

    /**
     * 
     */
    public synchronized void addOrRemoveEdge() {

        try {
            if (this.selectedEdge != null) {
                this.graph.removeEdge(this.selectedEdge);
                deselectEdge();

            } else if ((this.selectedOutputPort != null) && (this.selectedInputPort != null)) {

                if (this.graph.containsEdge(this.selectedOutputPort, this.selectedInputPort)) {
                    // If both ports are selected and they are connected
                    // already, the edge will be deleted.
                    this.graph.removeEdge(this.selectedOutputPort, this.selectedInputPort);
                    deselectEdge();

                } else {
                    // Create a new edge
                    connect(this.selectedOutputPort, this.selectedInputPort);
                }
            }
            this.panel.repaint();
        } catch (GraphException e) {
            // Should not happen
            logger.error(e.getMessage(), e);
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);

        }
    }

    /**
     * Returns the selectedNode.
     * 
     * @return The selectedNode
     */
    public Node getSelectedNode() {
        return this.selectedNode;
    }

    /**
     * Returns the selectedInputPort.
     * 
     * @return The selectedInputPort
     */
    public Port getSelectedInputPort() {
        return this.selectedInputPort;
    }

    /**
     * Returns the selectedOutputPort.
     * 
     * @return The selectedOutputPort
     */
    public Port getSelectedOutputPort() {
        return this.selectedOutputPort;
    }

    /**
     * Repaints the panel.
     */
    public void repaint() {
        this.panel.repaint();
    }

    /**
     * @param listener
     */
    public synchronized void addGraphCanvasListener(GraphCanvasListener listener) {
        this.listeners.add(listener);
    }

    /**
     * @param listener
     */
    public synchronized void removeGraphCanvasListener(GraphCanvasListener listener) {
        this.listeners.remove(listener);
    }

    private void mouseClicked(MouseEvent event) {
        /*
         * If there is multi-selected and a click on a node, switch to that node or deselect node if it is already
         * selected
         */
        Point point = event.getPoint();
        GraphPiece clicked = this.graph.getGUI().getGraphPieceAt(point);
        if ((clicked instanceof Node) && this.multipleSelectedNodes != null) {
            Node node = (Node) clicked;
            if (!this.crtlPressed) {
                selectNode(node);
            }
            return;
        }

        // delegate the event.
        this.graph.getGUI().mouseClicked(event, this.engine);
    }

    private void mousePressed(MouseEvent event) {
        Point point = event.getPoint();

        // Get focus to handle key board events
        this.panel.requestFocusInWindow();

        // Get select item
        GraphPiece selected = this.graph.getGUI().getGraphPieceAt(point);

        /*
         * Doing Nothing if pressed is on the selected node
         */
        if (this.multipleSelectedNodes != null) {
            maybeShowPopup(event);
            if (this.crtlPressed && this.multipleSelectedNodes.contains(selected)) {
                deselectNode((Node) selected);
                return;
            } else if (this.multipleSelectedNodes.contains(selected)) {
                this.mousePoint = point;
                this.panel.setCursor(SwingUtil.MOVE_CURSOR);
                return;
            } else if ((selected instanceof Node) && this.crtlPressed) {
                this.mousePoint = point;
                this.multipleSelectedNodes.add((Node) selected);
                this.panel.setCursor(SwingUtil.MOVE_CURSOR);
                selectNodes(this.multipleSelectedNodes);
                return;
            }
        }
        // control selection
        if ((selected instanceof Node) && this.crtlPressed) {
            this.multipleSelectedNodes = new ArrayList<Node>();
            if (this.selectedNode != null) {
                this.multipleSelectedNodes.add(this.selectedNode);
            }
            this.multipleSelectedNodes.add((Node) selected);
            this.panel.setCursor(SwingUtil.MOVE_CURSOR);
            selectNodes(this.multipleSelectedNodes);
            return;
        }

        deselectNode();
        deselectEdge();

        if (selected instanceof Node) {
            Node node = (Node) selected;
            selectNode(node);
            if (!node.getGUI().isInConfig(point)) {
                this.draggedNode = node;
                node.getGUI().setDraggedFlag(true);
                this.panel.setCursor(SwingUtil.MOVE_CURSOR);
            }

        } else if (selected instanceof Port) {
            Port port = (Port) selected;
            port.getGUI().setSelectedFlag(true);
            switch (port.getKind()) {
            case DATA_IN:
            case CONTROL_IN:
                selectInputPort(port);
                break;
            case CONTROL_OUT:
            case DATA_OUT:
            case EPR:
                selectOutputPort(port);
                break;
            }

            this.draggedPort = port;

        } else if (selected instanceof Edge) {
            Edge edge = (Edge) selected;
            selectEdge(edge);
        } else {
            /*
             * If nothing is selected
             */
            this.mousePointForSelection = event.getPoint();
        }

        maybeShowPopup(event);

        this.mousePoint = point;
        this.panel.repaint();
        event.consume();
    }

    private void mouseReleased(MouseEvent event) {
        Point point = event.getPoint();
        if (this.draggedNode != null) {
            this.draggedNode.getGUI().setDraggedFlag(false);
            this.panel.setCursor(SwingUtil.DEFAULT_CURSOR);

            // Check if it s stream grouping
            if (draggedNode instanceof InputNode) {
                StreamSourceNode streamNode = this.graph.getGUI().getStreamSourceAt(point);
                if (streamNode != null) {
                    streamNode.addInputNode((InputNode) draggedNode);
                }

            }
            this.draggedNode = null;

        }

        if (this.draggedPort != null) {
            GraphPiece graphPiece = this.graph.getGUI().getGraphPieceAt(point);
            if (graphPiece instanceof DynamicNode) {
                if (this.draggedPort.getKind() == Kind.DATA_OUT && draggedPort instanceof DataPort) {
                    this.panel.setCursor(SwingUtil.CROSSHAIR_CURSOR);
                    DynamicNode dynamicNode = (DynamicNode) graphPiece;
                    dynamicNode.getComponent();
                    DataPort freePort = dynamicNode.getFreeInPort();
                    try {
                        freePort.copyType((DataPort) draggedPort);
                    } catch (GraphException e) {
                        engine.getErrorWindow().error(e);
                        return;
                    }
                    // selectInputPort(freePort);
                    connect(this.draggedPort, freePort);
                    this.dynamicNodeWithFreePort = null;
                }

            } else if (graphPiece instanceof Port) {
                Port port = (Port) graphPiece;
                if (this.draggedPort.getKind() == Kind.DATA_OUT && port.getKind() == Kind.DATA_IN) {
                    connect(this.draggedPort, port);
                } else if (port.getKind() == Kind.DATA_OUT && this.draggedPort.getKind() == Kind.DATA_IN) {
                    connect(port, this.draggedPort);
                } else if (this.draggedPort.getKind() == Kind.CONTROL_OUT && port.getKind() == Kind.CONTROL_IN) {
                    connect(this.draggedPort, port);
                } else if (this.draggedPort.getKind() == Kind.CONTROL_IN && port.getKind() == Kind.CONTROL_OUT) {
                    connect(port, this.draggedPort);
                } else if (this.draggedPort.getKind() == Kind.EPR && port.getKind() == Kind.DATA_IN) {
                    connect(this.draggedPort, port);
                } else if (this.draggedPort.getKind() == Kind.DATA_IN && port.getKind() == Kind.EPR) {
                    connect(port, this.draggedPort);
                }
            }
            this.draggedPort = null;
        }

        if (this.dynamicNodeWithFreePort != null) {
            try {
                this.dynamicNodeWithFreePort.removeLastDynamicallyAddedInPort();
            } catch (GraphException e) {
                this.engine.getErrorWindow().error(e);
            }
        }

        /*
         * Multiple selected
         */
        if (this.mousePointForSelection != null) {
            double width = Math.abs(this.mousePoint.getX() - this.mousePointForSelection.getX());
            double height = Math.abs(this.mousePoint.getY() - this.mousePointForSelection.getY());
            int x = (int) (this.mousePoint.getX() > this.mousePointForSelection.getX() ? this.mousePointForSelection
                    .getX() : this.mousePoint.getX());
            int y = (int) (this.mousePoint.getY() > this.mousePointForSelection.getY() ? this.mousePointForSelection
                    .getY() : this.mousePoint.getY());

            this.multipleSelectedNodes = this.graph.getGUI().getNodesIn(new Rectangle(x, y, (int) width, (int) height));
            selectNodes(this.multipleSelectedNodes);

            // clear mousepoint
            this.mousePointForSelection = null;
        }

        if (this.multipleSelectedNodes != null) {
            this.panel.setCursor(SwingUtil.DEFAULT_CURSOR);
        }

        maybeShowPopup(event);

        updateSize();
        this.panel.repaint();
        event.consume();
    }

    private void mouseDragged(MouseEvent event) {
        Point point = event.getPoint();

        if (editable) {
			/*
			 * Move nodes
			 */
			if (this.multipleSelectedNodes != null) {
				if (point.x < 0) {
					point.x = 0;
				}
				if (point.y < 0) {
					point.y = 0;
				}
				int diffX = point.x - this.mousePoint.x;
				int diffY = point.y - this.mousePoint.y;
				for (Node node : this.multipleSelectedNodes) {
					Point newPoint = new Point();
					Point currentPoint = node.getPosition();
					newPoint.x = currentPoint.x + diffX;
					if (newPoint.x < 0) {
						newPoint.x = 0;
					}
					newPoint.y = currentPoint.y + diffY;
					if (newPoint.y < 0) {
						newPoint.y = 0;
					}
					node.setPosition(newPoint);
				}
				this.panel.repaint();
				event.consume();
			}
			if (this.draggedNode != null) {
				if (point.x < 0) {
					point.x = 0;
				}
				if (point.y < 0) {
					point.y = 0;
				}
				int diffX = point.x - this.mousePoint.x;
				int diffY = point.y - this.mousePoint.y;
				Point newPoint = new Point();
				Point currentPoint = this.draggedNode.getPosition();
				newPoint.x = currentPoint.x + diffX;
				if (newPoint.x < 0) {
					newPoint.x = 0;
				}
				newPoint.y = currentPoint.y + diffY;
				if (newPoint.y < 0) {
					newPoint.y = 0;
				}
				this.draggedNode.setPosition(newPoint);

				this.panel.repaint();
				event.consume();
			}
			if (this.draggedPort != null) {
				GraphPiece piece = this.graph.getGUI().getGraphPieceAt(point);
				if (piece instanceof Port) {
					Port port = (Port) piece;
					// Display the information of port that is close to the mouse
					// pointer.
					if (this.draggedPort.getKind() == Kind.DATA_IN
							&& port.getKind() == Kind.DATA_OUT) {
						this.panel.setCursor(SwingUtil.CROSSHAIR_CURSOR);
						selectOutputPort(port);
					} else if (this.draggedPort.getKind() == Kind.DATA_OUT
							&& port.getKind() == Kind.DATA_IN) {
						this.panel.setCursor(SwingUtil.CROSSHAIR_CURSOR);
						selectInputPort(port);
					} else if (this.draggedPort.getKind() == Kind.DATA_IN
							&& port.getKind() == Kind.EPR) {
						this.panel.setCursor(SwingUtil.CROSSHAIR_CURSOR);
						selectOutputPort(port);
					} else if (this.draggedPort.getKind() == Kind.EPR
							&& port.getKind() == Kind.DATA_IN) {
						this.panel.setCursor(SwingUtil.CROSSHAIR_CURSOR);
						selectInputPort(port);
					} else {
						this.panel.setCursor(SwingUtil.DEFAULT_CURSOR);
					}
				} else if (piece instanceof PortAddable) {
					PortAddable dynamicNode = (PortAddable) piece;
					dynamicNode.getFreeInPort();
					this.dynamicNodeWithFreePort = dynamicNode;
				} else {

					this.panel.setCursor(SwingUtil.DEFAULT_CURSOR);
				}

				this.panel.repaint();
				event.consume();
			}
			this.mousePoint = point;
			// draw rectangle
			if (this.mousePointForSelection != null) {
				this.panel.repaint();
			}
		}

    }

    private void mouseMoved(MouseEvent event) {
        Point point = event.getPoint();
        GraphPiece graphPiece = this.graph.getGUI().getGraphPieceAt(point);
        if (graphPiece instanceof Node) {
            Node node = (Node) graphPiece;
            if (node.getGUI().isInConfig(point)) {
                this.panel.setCursor(SwingUtil.HAND_CURSOR);
            } else {
                this.panel.setCursor(SwingUtil.DEFAULT_CURSOR);
            }
        } else if (graphPiece instanceof Port) {
            this.panel.setCursor(SwingUtil.CROSSHAIR_CURSOR);
        } else {
            this.panel.setCursor(SwingUtil.DEFAULT_CURSOR);
        }

    }

    private void keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (editable && keyCode == KeyEvent.VK_DELETE) {
            try {
                removeSelected();
            } catch (GraphException e) {
                // Should not happen
                logger.error(e.getMessage(), e);
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            } catch (Error e) {
                logger.error(e.getMessage(), e);
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            }
        }

        /*
         * Multiple select with shift
         */
        if (keyCode == KeyEvent.VK_CONTROL) {
            this.crtlPressed = true;
        }
    }

    private void keyReleased(KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.VK_CONTROL) {
            this.crtlPressed = false;
        }
    }

    private void drop(final DropTargetDropEvent event) {
        logger.debug("Event:" + event);
        Transferable transferable = event.getTransferable();
        try {
            // Cannot cast transferable.
            final ComponentReference componentReference = (ComponentReference) transferable
                    .getTransferData(ComponentSourceTransferable.FLAVOR);
            final Point location = event.getLocation();

            // The component might not have loaded if the network is slow.
            new Thread() {
                @Override
                public void run() {
                    try {
                        Component component = componentReference.getComponent();
                        addNode(component, location);
                        // To be able to delete the added node by the keyboard.
                        GraphCanvas.this.panel.requestFocusInWindow();
                        // XXX this sometimes throws exception.
                        event.dropComplete(true);
                    } catch (ComponentException e) {
                        // If there is any error, the component tree viewer
                        // shows the error dialog.
                        logger.error(e.getMessage(), e);
                        event.dropComplete(false);
                    } catch (ComponentRegistryException e) {
                        logger.error(e.getMessage(), e);
                        event.dropComplete(false);
                    }
                }
            }.start();

        } catch (UnsupportedFlavorException e) {
            // Should not happen.
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            // Should not happen.
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @return The image
     */
    private BufferedImage createImage() {
        Rectangle bounds = this.graph.getGUI().getBounds();
        BufferedImage image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        // Background
        final Color background = new Color(226, 226, 222);
        graphics.setBackground(background);
        graphics.clearRect(0, 0, bounds.width, bounds.height);

        paintComponent(graphics);

        return image;
    }

    /**
     * Connects two ports specified.
     * 
     * @param fromPort
     * @param toPort
     */
    private void connect(Port fromPort, Port toPort) {
        try {
            // check the validity of the connection.
            Edge edge = this.graph.addEdge(fromPort, toPort);
            selectEdge(edge);
        } catch (GraphException e) {
            logger.error(e.getMessage(), e);
            this.engine.getErrorWindow().warning(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR);
        }
    }

    private void paintComponent(Graphics2D g) {
        this.graph.getGUI().paint(g);

        // Draws a creating edge.
        if (this.draggedPort != null) {
            Point p1, p2;
            Kind kind = this.draggedPort.getKind();
            if (kind == Kind.DATA_OUT || kind == Kind.CONTROL_OUT || kind == Kind.EPR) {
                p1 = this.draggedPort.getGUI().getPosition();
                p2 = this.mousePoint;
            } else if (kind == Kind.DATA_IN || kind == Kind.CONTROL_IN) {
                p1 = this.mousePoint;
                p2 = this.draggedPort.getGUI().getPosition();
            } else {
                // This should not happen.
                throw new XBayaRuntimeException();
            }
            g.setColor(Color.RED);

            Stroke originalStroke = g.getStroke();
            if (kind == Kind.CONTROL_IN || kind == Kind.CONTROL_OUT) {
                g.setStroke(EdgeGUI.CONTROL_EDGE_STROKE);
            }
            EdgeGUI.paintLine(p1, p2, g);
            g.setStroke(originalStroke);

        }

        // Draw rectangular for selection
        if (this.mousePointForSelection != null) {
            double width = Math.abs(this.mousePoint.getX() - this.mousePointForSelection.getX());
            double height = Math.abs(this.mousePoint.getY() - this.mousePointForSelection.getY());
            int x = (int) (this.mousePoint.getX() > this.mousePointForSelection.getX() ? this.mousePointForSelection
                    .getX() : this.mousePoint.getX());
            int y = (int) (this.mousePoint.getY() > this.mousePointForSelection.getY() ? this.mousePointForSelection
                    .getY() : this.mousePoint.getY());
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, // End cap
                                                                     // style
                    BasicStroke.JOIN_MITER, // Join style
                    15.0f, // Miter limit
                    new float[] { 5.0f, 5.0f }, // Dash pattern
                    3.0f)); // Dash phase
            g.drawRect(x, y, (int) width, (int) height);
        }
    }

    /**
     * Gets an random position of for a new node. This method is called when a new node is added to the graph.
     * 
     * @return The position
     */
    private Point getRandomPosition() {
        Rectangle area = this.panel.getVisibleRect();
        int x = (int) (area.x + (area.width - NodeGUI.MINIMUM_WIDTH) * Math.random());
        int y = (int) (area.y + (area.height - NodeGUI.MINIMUM_HEIGHT) * Math.random());
        return new Point(x, y);
    }

    /**
     * Updates the size of this Panel.
     */
    private void updateSize() {

        Rectangle bounds = this.graph.getGUI().getBounds();
        Dimension newDimention = new Dimension(bounds.width, bounds.height);

        if (!newDimention.equals(this.graphDimention)) {

            // Updates this Panel's preferred size because the area taken up by
            // the graph has changed.
            this.panel.setPreferredSize(newDimention);

            // Let the scroll pane know to update itself and its scrollbars.
            this.panel.revalidate();

            this.graphDimention = newDimention;
        }
    }

    /**
     * Sets the selected node.
     * 
     * Use this method to send the event to the listeners.
     * 
     * @param node
     */
    private void setSelectedNode(Node node) {
        this.selectedNode = node;
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.NODE_SELECTED, this, this.workflow));
    }

    /**
     * Selects a node. The selected node changes its color.
     * 
     * @param node
     *            The node to select.
     */
    private void selectNode(Node node) {
        deselectNode();
        node.getGUI().setSelectedFlag(true);
        setSelectedNode(node);
    }

    private void selectNodes(List<Node> nodes) {
        deselectNode();
        for (Node node : nodes) {
            node.getGUI().setSelectedFlag(true);
            node.getGUI().setDraggedFlag(true);
        }
        this.multipleSelectedNodes = nodes;
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.NODE_SELECTED, this, this.workflow));
    }

    /**
     * Deselects a node that is currently selected if any.
     */
    private void deselectNode() {
        if (this.selectedNode != null) {
            this.selectedNode.getGUI().setSelectedFlag(false);
            this.selectedNode.getGUI().setDraggedFlag(false);
            setSelectedNode(null);
        }
        if (this.multipleSelectedNodes != null) {
            for (Node node : this.multipleSelectedNodes) {
                node.getGUI().setSelectedFlag(false);
                node.getGUI().setDraggedFlag(false);
            }
            this.multipleSelectedNodes = null;
        }
    }

    private void deselectNode(Node node) {
        if (this.multipleSelectedNodes != null && this.multipleSelectedNodes.contains(node)) {
            node.getGUI().setSelectedFlag(false);
            node.getGUI().setDraggedFlag(false);
            this.multipleSelectedNodes.remove(node);
        }
    }

    private void setSelectedInputPort(Port port) {
        this.selectedInputPort = port;
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.INPUT_PORT_SELECTED, this,
                this.workflow));
    }

    private void selectInputPort(Port port) {
        deselectInputPort();
        port.getGUI().setSelectedFlag(true);
        setSelectedInputPort(port);
    }

    private void deselectInputPort() {
        if (this.selectedInputPort != null) {
            this.selectedInputPort.getGUI().setSelectedFlag(false);
            setSelectedInputPort(null);
        }
    }

    private void setSelectedOutputPort(Port port) {
        this.selectedOutputPort = port;
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.OUTPUT_PORT_SELECTED, this,
                this.workflow));
    }

    private void selectOutputPort(Port port) {
        deselectOutputPort();
        port.getGUI().setSelectedFlag(true);
        setSelectedOutputPort(port);
    }

    private void deselectOutputPort() {
        if (this.selectedOutputPort != null) {
            this.selectedOutputPort.getGUI().setSelectedFlag(false);
            setSelectedOutputPort(null);
        }
    }

    private void selectEdge(Edge edge) {
        if (edge != null) {
            deselectEdge();
            edge.getGUI().setSelectedFlag(true);
            this.selectedEdge = edge;

            // When an edge is selected, ports on both sides will be selected
            // too.
            selectOutputPort(edge.getFromPort());
            selectInputPort(edge.getToPort());
        }
    }

    private void deselectEdge() {
        if (this.selectedEdge != null) {
            this.selectedEdge.getGUI().setSelectedFlag(false);
            this.selectedEdge = null;
        }
    }

    private void reset() {
        setSelectedNode(null);
        this.draggedNode = null;
        setSelectedInputPort(null);
        setSelectedOutputPort(null);
        this.selectedEdge = null;
        this.multipleSelectedNodes = null;
    }

    private void notifyListeners(GraphCanvasEvent event) {
        for (GraphCanvasListener listener : this.listeners) {
            listener.graphCanvasChanged(event);
        }
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                GraphCanvas.this.paintComponent((Graphics2D) g);
            }
        };

        this.panel.setLayout(null);
        this.panel.setOpaque(true); // To make the background color visible.
        this.panel.setBackground(new Color(255, 255, 255));
        this.panel.setDoubleBuffered(true);

        this.panel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                GraphCanvas.this.mouseClicked(event);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                GraphCanvas.this.mousePressed(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                GraphCanvas.this.mouseReleased(event);
                notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.WORKFLOW_CHANGED, GraphCanvas.this, GraphCanvas.this.workflow));
            }
        });

        this.panel.addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent event) {
                GraphCanvas.this.mouseDragged(event);
            }

            public void mouseMoved(MouseEvent event) {
                GraphCanvas.this.mouseMoved(event);
            }
        });

        this.panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                GraphCanvas.this.keyPressed(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
                GraphCanvas.this.keyReleased(event);
            }

        });

        this.scrollPane = new JScrollPane(this.panel);

        // Set up drag and drop
        DropTargetListener dropTargetListener = new DropTargetAdapter() {
            public void drop(DropTargetDropEvent event) {
                GraphCanvas.this.drop(event);
            }
        };
        new DropTarget(this.panel, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener);

        createPopupMenu();
    }

    private void createPopupMenu() {
        createEdgePopupMenu();
        createNodePopupMenu();
    }

    private void createNodePopupMenu() {
        this.nodePopup = new JPopupMenu();
        if (editable) {
			JMenuItem deleteItem = new JMenuItem("Delete");
			deleteItem.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent event) {
					try {
						removeSelectedNode();
					} catch (GraphException e) {
						// Should not happen
						logger.error(e.getMessage(), e);
						GraphCanvas.this.engine.getGUI().getErrorWindow()
								.error(ErrorMessages.UNEXPECTED_ERROR, e);
					} catch (RuntimeException e) {
						logger.error(e.getMessage(), e);
						GraphCanvas.this.engine.getGUI().getErrorWindow()
								.error(ErrorMessages.UNEXPECTED_ERROR, e);
					} catch (Error e) {
						logger.error(e.getMessage(), e);
						GraphCanvas.this.engine.getGUI().getErrorWindow()
								.error(ErrorMessages.UNEXPECTED_ERROR, e);
					}

				}
			});
			this.nodePopup.add(deleteItem);
		}
		rerunItem = new JMenuItem("ReRun");
        rerunItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    rerunSelectedNode();
                } catch (RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    GraphCanvas.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } catch (Error e) {
                    logger.error(e.getMessage(), e);
                    GraphCanvas.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }

            }
        });

        breakPointItem = new JMenuItem("Add break Point");
        breakPointItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    toggleBreakPointToNode();
                } catch (RuntimeException e) {
                    logger.error(e.getMessage(), e);
                    GraphCanvas.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } catch (Error e) {
                    logger.error(e.getMessage(), e);
                    GraphCanvas.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }

            }
        });
  

    }

    private synchronized void toggleBreakPointToNode() {
        if (this.selectedNode != null) {
            this.selectedNode.setBreak(!this.selectedNode.isBreak());
            this.repaint();
        }
    }

    private void rerunSelectedNode() {
        if (this.selectedNode != null) {

            ArrayList<Node> exploreNodes = new ArrayList<Node>();
            exploreNodes.add(this.selectedNode);
            while (exploreNodes.size() != 0) {
                Node node = exploreNodes.get(0);
                List<DataPort> outputPorts = node.getOutputPorts();
                for (DataPort dataPort : outputPorts) {
                    exploreNodes.addAll(dataPort.getToNodes());
                }
                node.getGUI().setBodyColor(NodeGUI.DEFAULT_BODY_COLOR);

                exploreNodes.remove(0);
            }
            this.repaint();
        }
    }

    private void prepareNodePopupMenu(Node node) {
        this.nodePopup.remove(rerunItem);
        this.nodePopup.remove(breakPointItem);

        if (this.engine.getWorkflow().getExecutionState() == XBayaExecutionState.PAUSED && !(node instanceof InputNode)) {
            this.nodePopup.add(rerunItem);

        }
        if (this.engine.getWorkflow().getExecutionState() != XBayaExecutionState.NONE) {
            if (node.isBreak()) {
                breakPointItem.setText("Remove break Point");
            } else {
                breakPointItem.setText("Add break Point");
            }
            this.nodePopup.add(breakPointItem);
        }
       
    }

    private void createEdgePopupMenu() {
        this.edgePopup = new JPopupMenu();
        if (editable) {
			JMenuItem deleteItem = new JMenuItem("Delete");
			deleteItem.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					removeSelectedEdge();
				}
			});
			this.edgePopup.add(deleteItem);
		}
    }

    private void maybeShowPopup(MouseEvent event) {
        if (event.isPopupTrigger()) {
            GraphPiece piece = this.graph.getGUI().getGraphPieceAt(event.getPoint());
            if (piece instanceof Node) {
                prepareNodePopupMenu((Node) piece);
                this.nodePopup.show(event.getComponent(), event.getX(), event.getY());
            } else if (piece instanceof Edge) {
                this.edgePopup.show(event.getComponent(), event.getX(), event.getY());
            }
        }
    }


	public File getWorkflowFile() {
		return workflowFile;
	}

	public void setWorkflowFile(File workflowFile) {
		this.workflowFile = workflowFile;
	}
	
	public boolean isWorkflowChanged(){
		try {
			if (originalWorkflowElement==null){
				updateOriginalWorkflowElement();
			}
			return !XMLUtil.isEqual(originalWorkflowElement, getWorkflow().toXML());
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}
	
	public void workflowSaved(){
		updateOriginalWorkflowElement();
        notifyListeners(new GraphCanvasEvent(GraphCanvasEvent.GraphCanvasEventType.WORKFLOW_CHANGED, this, this.workflow));
	}

	private void updateOriginalWorkflowElement() {
		originalWorkflowElement = getWorkflow().toXML();
	}

	@Override
	public void executionModeChanged(XBayaConfiguration config) {
		editable=config.getXbayaExecutionMode()==XBayaExecutionMode.IDE;
		getGraph().setEditable(editable);
	}
}