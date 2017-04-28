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
package org.apache.airavata.workflow.engine.interpretor;

public enum WorkflowExecutionMessage {
	NODE_STATE_CHANGED, 
	//this.engine.getGUI().getGraphCanvas().repaint();
	EXECUTION_STATE_CHANGED,
	EXECUTION_RESUME,
	EXECUTION_TASK_START,
	EXECUTION_TASK_END,
	EXECUTION_ERROR,
	EXECUTION_CLEANUP,
	OPEN_SUBWORKFLOW,
	HANDLE_DEPENDENT_NODES_DIFFERED_INPUTS,
	INPUT_WORKFLOWINTERPRETER_FOR_WORKFLOW,
	INPUT_GSS_CREDENTIAL,
	INPUT_LEAD_CONTEXT_HEADER,
	INPUT_GFAC_INVOKER,
	
}
