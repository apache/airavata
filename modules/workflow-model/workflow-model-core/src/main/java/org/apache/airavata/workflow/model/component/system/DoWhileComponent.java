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
package org.apache.airavata.workflow.model.component.system;

import org.apache.airavata.workflow.model.component.ComponentControlPort;
import org.apache.airavata.workflow.model.component.ComponentPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.DoWhileNode;

public class DoWhileComponent extends SystemComponent {

	/**
	 * The name of the const component
	 */
	public static final String NAME = "DoWhile";

	/**
	 * The name of control output port if condition is true
	 */
	public static final String TRUE_PORT_NAME = "DoWhile_True";

	/**
	 * The name of control output port if condition is false
	 */
	public static final String FALSE_PORT_NAME = "DoWhile_False";

	private static final String DESCRIPTION = "A system component that represents a while condition to branch.";

	private static final String INPUT_PORT_NAME = "Input_initial";

	private static final String INPUT_PORT_DESCRIPTION = "The input value from the previous node for do";

	private static final String INPUT_PREDICATE_PORT_NAME = "Input_predicate";

	private static final String INPUT_PREDICATE_PORT_DESCRIPTION = "The input value is used to evaluate the while condition.";

	private static final String CONTROL_OUT_TRUE_DESCRIPTION = "If the condition is true, services connected to this port will be executed.";
	private static final String CONTROL_OUT_FALSE_DESCRIPTION = "If the condition is false, services will end";

	private static final String OUTPUT_PORT_NAME = "Output";

	private static final String OUTPUT_PORT_DESCRIPTION = "Port to pass value of conditional parameter";

	private SystemComponentDataPort inputPort;

	private SystemComponentDataPort outputPort;

	/**
	 * Constructs a DoWhileComponent.
	 *
	 */
	public DoWhileComponent() {
		setName(NAME);
		setDescription(DESCRIPTION);

		this.inputPort = new SystemComponentDataPort(INPUT_PORT_NAME);
		this.inputPort.setDescription(INPUT_PORT_DESCRIPTION);
		this.inputs.add(this.inputPort);
		this.inputPort = new SystemComponentDataPort(INPUT_PREDICATE_PORT_NAME);
		this.inputPort.setDescription(INPUT_PREDICATE_PORT_DESCRIPTION);
		this.inputs.add(this.inputPort);

		this.controlInPort = new ComponentControlPort();

		this.outputPort = new SystemComponentDataPort(OUTPUT_PORT_NAME);
		this.outputPort.setDescription(OUTPUT_PORT_DESCRIPTION);
		this.outputs.add(this.outputPort);

		ComponentControlPort outputPortTrue = new ComponentControlPort(
				TRUE_PORT_NAME);
		outputPortTrue.setDescription(CONTROL_OUT_TRUE_DESCRIPTION);
		this.controlOutPorts.add(outputPortTrue);

		ComponentControlPort outputPortFalse = new ComponentControlPort(
				FALSE_PORT_NAME);
		outputPortFalse.setDescription(CONTROL_OUT_FALSE_DESCRIPTION);
		this.controlOutPorts.add(outputPortFalse);
	}

	/**
	 * Returns the input component port.
	 *
	 * This method is used by IfNode to create additional output port.
	 *
	 * @return The input component port.
	 */
	public SystemComponentDataPort getInputPort() {
		return this.inputPort;
	}

	/**
	 * @see edu.indiana.extreme.xbaya.component.Component#getInputPort(int)
	 */
	@Override
	public ComponentPort getInputPort(int index) {
		return getInputPort();
	}

	/**
	 * Returns the input component port.
	 *
	 * This method is used by EndifNode to create additional output port.
	 *
	 * @return The output component port.
	 */
	public SystemComponentDataPort getOutputPort() {
		return this.outputPort;
	}

	/**
	 * @see edu.indiana.extreme.xbaya.component.Component#getOutputPort(int)
	 */
	@Override
	public ComponentPort getOutputPort(int index) {
		return getOutputPort();
	}

	/**
	 * @see edu.indiana.extreme.xbaya.component.Component#createNode(edu.indiana.extreme.xbaya.graph.Graph)
	 */
	@Override
	public Node createNode(Graph graph) {
		DoWhileNode node = new DoWhileNode(graph);

		node.setName(NAME);
		node.setComponent(this);

		// Creates a unique ID for the node. This has to be after setName().
		node.createID();

		createPorts(node);
		return node;
	}
}