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

/**
 * Edge is a link to one node to another, basically edge should have outPort of a workflow node ,
 * which is starting point and inPort of a workflow node, which is end point of the edge.
 */

public interface Edge {

    public String getId();

    public void setEdgeModel(EdgeModel edgeModel);

    public EdgeModel getEdgeModel();

    public InPort getToPort();

    public void setToPort(InPort inPort);

    public OutPort getFromPort();

    public void setFromPort(OutPort outPort);


}
