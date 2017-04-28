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
package org.apache.airavata.workflow.core.dag.edge;

import org.apache.airavata.model.EdgeModel;
import org.apache.airavata.workflow.core.dag.port.InPort;
import org.apache.airavata.workflow.core.dag.port.OutPort;


public class DirectedEdge implements Edge {

    private InPort toPort;
    private OutPort fromPort;
    private EdgeModel edgeModel;

    public DirectedEdge(EdgeModel edgeModel) {
        this.edgeModel = edgeModel;
    }

    @Override
    public String getId() {
        return getEdgeModel().getEdgeId();
    }

    @Override
    public void setEdgeModel(EdgeModel edgeModel) {
        this.edgeModel = edgeModel;
    }

    @Override
    public EdgeModel getEdgeModel() {
        return edgeModel;
    }

    @Override
    public InPort getToPort() {
        return toPort;
    }

    @Override
    public void setToPort(InPort inPort) {
        this.toPort = inPort;
    }

    @Override
    public OutPort getFromPort() {
        return fromPort;
    }

    @Override
    public void setFromPort(OutPort outPort) {
        this.fromPort = outPort;
    }
}
