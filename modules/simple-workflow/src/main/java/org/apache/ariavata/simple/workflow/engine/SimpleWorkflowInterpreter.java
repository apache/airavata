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

package org.apache.ariavata.simple.workflow.engine;

import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;

import java.util.List;

/**
 * Created by shameera on 1/29/15.
 */
public class SimpleWorkflowInterpreter {


    public SimpleWorkflowInterpreter(Experiment experiment, String credentialStoreToken) {
        // read the workflow file and build the topology to a DAG. Then execute that dag
        // get workflowInputNode list and start processing
        // next() will return ready task and block the thread if no task in ready state.
    }

    /**
     * This method block the calling thread until next task is ready to return;
     *
     * @return nest task to launch;
     */
    public TaskDetails next() {
        return null;
    }

    private List<WorkflowInputNode> parseWorkflowDescription(){
        return null;
    }


}
