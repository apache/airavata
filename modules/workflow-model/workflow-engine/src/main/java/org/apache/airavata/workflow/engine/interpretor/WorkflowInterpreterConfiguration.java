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

import org.apache.airavata.workflow.model.wf.Workflow;

public class WorkflowInterpreterConfiguration {
//	public static final int GUI_MODE = 1;
//	public static final int SERVER_MODE = 2;
//	
	private boolean offline=false;
	private boolean runWithCrossProduct=false;
	private Workflow workflow;
	private Boolean actOnProvenance = null;

	public WorkflowInterpreterConfiguration(Workflow workflow) {
		this(workflow,true);
	}
	
    public WorkflowInterpreterConfiguration(Workflow workflow,
                                            boolean offline) {
        this.offline = offline;
        this.workflow = workflow;
    }
	
	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public boolean isRunWithCrossProduct() {
		return runWithCrossProduct;
	}

	public void setRunWithCrossProduct(boolean runWithCrossProduct) {
		this.runWithCrossProduct = runWithCrossProduct;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Boolean isActOnProvenance() {
		return actOnProvenance;
	}

	public void setActOnProvenance(Boolean actOnProvenance) {
		this.actOnProvenance = actOnProvenance;
	}
}
