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
package org.apache.airavata.workflow.model.graph;

import java.awt.Point;
import java.util.Collection;
import java.util.List;

import org.apache.airavata.workflow.model.component.Component;

public interface Node extends GraphPiece {

	public static enum NodeExecutionState{
		WAITING,
		EXECUTING,
		FAILED,
		FINISHED
	}
    /**
     * Returns the ID of the node.
     * 
     * @return the ID of the node
     */
    public String getID();

    /**
     * Returns the name of the node.
     * 
     * @return the name of the node
     */
    public String getName();

    /**
     * Sets the name of the node.
     * 
     * @param name
     *            The name of the node
     */
    public void setName(String name);

    /**
     * Returns the component.
     * 
     * @return The component
     */
    public Component getComponent();

    /**
     * Sets the component.
     * 
     * @param component
     *            The component to set.
     */
    public void setComponent(Component component);

    /**
     * @return The graph
     */
    public Graph getGraph();

    /**
     * Returns the List of input ports.
     * 
     * @return the List of input ports
     */
    public List<DataPort> getInputPorts();

    /**
     * Returns the List of output ports.
     * 
     * @return the List of output ports
     */
    public List<DataPort> getOutputPorts();

    /**
     * Returns the input port of the specified index.
     * 
     * @param index
     *            The specified index
     * @return the input port of the specified index
     */
    public DataPort getInputPort(int index);

    /**
     * Returns the output port of the specified index.
     * 
     * @param index
     *            The specified index
     * @return the uses port of the specified index
     */
    public Port getOutputPort(int index);

    /**
     * @return The controlInPort.
     */
    public ControlPort getControlInPort();

    /**
     * @return The List of controlOutPorts.
     */
    public List<? extends ControlPort> getControlOutPorts();

    /**
     * @return The EPR (End Point Reference) Port.
     */
    public Port getEPRPort();

    /**
     * Returns all ports that belong to this node.
     * 
     * @return All ports that belong to this node.
     */
    public Collection<? extends Port> getAllPorts();

    /**
     * Checks if this node contains a specified port.
     * 
     * @param port
     *            The specified port
     * @return true if this node contains port; false otherwise
     */
    public boolean containsPort(Port port);

    /**
     * Sets the position of the node.
     * 
     * @param point
     *            The position
     */
    public void setPosition(Point point);

    /**
     * Gets the position of the node.
     * 
     * @return The position of the node.
     */
    public Point getPosition();

    /**
     * Gets whether this node has a break point set in it.
     * 
     * @return Whether execution should pause at this node
     */
    public boolean isBreak();

    /**
     * Sets or removes a break point in this node
     * 
     * @param breakVal
     *            whether to add or remove a break point
     */
    public void setBreak(boolean breakVal);

    /**
     * Check to see if all the input pors are already connected
     * 
     * @return
     */
    public boolean isAllInPortsConnected();

    /**
     * @param fromPortID
     */
    public DataPort getOutputPort(String fromPortID);

    /**
     * @param id
     */
    public DataPort getInputPort(String id);

   
  

    /**
     * @param b
     */
    public void setRequireJoin(boolean b);

    /**
     * @return
     */
    public boolean getRequireJoin();

    public NodeExecutionState getState();
    
    public void setState(NodeExecutionState state);

//    public void executeDynamically();
    
	public void registerObserver(NodeObserver o);
	public void removeObserver(NodeObserver o);
	
	public static enum NodeUpdateType{
		STATE_CHANGED,
		OTHER
	}
	public static interface NodeObserver{
		public void nodeUpdated(NodeUpdateType type);
	} 
	
}