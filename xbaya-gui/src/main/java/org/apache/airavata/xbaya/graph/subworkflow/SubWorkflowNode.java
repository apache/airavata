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

package org.apache.airavata.xbaya.graph.subworkflow;

import org.apache.airavata.xbaya.component.SubWorkflowComponent;
import org.apache.airavata.xbaya.graph.Edge;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.wf.Workflow;

public class SubWorkflowNode extends NodeImpl {

    private SubWorkflowNodeGUI gui;
    private Workflow workflow;

    /**
     * Constructs a SubWorkflowNode.
     * 
     * @param graph
     */
    public SubWorkflowNode(Graph graph) {
        super(graph);
        this.workflow = workflow;

    }

    /**
     * @see org.apache.airavata.xbaya.graph.ws.WSNode#getGUI()
     */
    public NodeGUI getGUI() {
        if (this.gui == null) {
            this.gui = new SubWorkflowNodeGUI(this);
        }
        return this.gui;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.ws.WSNode#getComponent()
     */
    @Override
    public SubWorkflowComponent getComponent() {
        return (SubWorkflowComponent) super.getComponent();
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.xbaya.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        GraphUtil.validateConnection(edge);
    }

    // /**
    // * @return the node xml
    // */
    // @Override
    // protected XmlElement toXML() {
    // return null;
    // }
    //
    // /**
    // * @see org.apache.airavata.xbaya.graph.impl.NodeImpl#parse(org.xmlpull.infoset.XmlElement)
    // */
    // @Override
    // protected void parse(XmlElement nodeElement) throws GraphException {
    //
    // }
    //
    // @Deprecated
    // protected void parseComponent(XmlElement componentElement){
    //
    // }

    /**
     * @param workflow
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Workflow getWorkflow() {
        return this.workflow;
    }

}