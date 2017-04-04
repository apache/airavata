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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.workflow.model.ode;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.airavata.workflow.model.component.ComponentException;
//import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
//import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
//import org.apache.airavata.workflow.model.gpel.script.BPELScript;
//import org.apache.airavata.workflow.model.gpel.script.BPELScriptType;
//import org.apache.airavata.workflow.model.graph.GraphException;
//import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.graph.system.OutputNode;
//import org.apache.airavata.workflow.model.wf.Workflow;
//
//public class ODEClient {
//
//    public static final String STREAM_SOURCE_NS = "http://extreme.indiana.edu/streaming/source";
//
//    public ODEClient() {
//
//    }
//
//    public List<InputNode> getInputNodes(Workflow workflow) {
//        LinkedList<InputNode> ret = new LinkedList<InputNode>();
//        List<NodeImpl> nodes = workflow.getGraph().getNodes();
//        for (NodeImpl nodeImpl : nodes) {
//            if (nodeImpl instanceof InputNode) {
//                ret.add((InputNode) nodeImpl);
//            }
//
//        }
//        return ret;
//    }
//
//    /**
//     * Returns workflow inputs and can be used to get workflow input metadata
//     * 
//     * @param workflow
//     * @return
//     */
//    public List<WSComponentPort> getInputs(Workflow workflow) {
//        List<WSComponentPort> inputs;
//        try {
//            if (workflow.getWorkflowWSDL() == null) {
//                BPELScript script = new BPELScript(workflow);
//                script.create(BPELScriptType.BPEL2);
//                workflow.setWorkflowWSDL(script.getWorkflowWSDL().getWsdlDefinitions());
//                workflow.setGpelProcess(script.getGpelProcess());
//
//            }
//            inputs = workflow.getInputs();
//            return inputs;
//        } catch (GraphException e) {
//            throw new WorkflowRuntimeException(e);
//        } catch (ComponentException e) {
//            throw new WorkflowRuntimeException(e);
//        }
//
//    }
//
//    /**
//     * @param workflow
//     * @return
//     */
//    public LinkedList<OutputNode> getoutNodes(Workflow workflow) {
//        List<NodeImpl> nodes = workflow.getGraph().getNodes();
//        LinkedList<OutputNode> ret = new LinkedList<OutputNode>();
//        for (NodeImpl nodeImpl : nodes) {
//            if (nodeImpl instanceof OutputNode) {
//                ret.add((OutputNode) nodeImpl);
//            }
//        }
//        return ret;
//
//    }
//
//}