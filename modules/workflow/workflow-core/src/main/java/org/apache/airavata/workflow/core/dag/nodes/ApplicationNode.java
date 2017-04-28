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
package org.apache.airavata.workflow.core.dag.nodes;

import org.apache.airavata.workflow.core.dag.port.InPort;
import org.apache.airavata.workflow.core.dag.port.OutPort;

import java.util.Collection;
import java.util.List;

public interface ApplicationNode extends WorkflowNode {

    public String getApplicationId();

    public String getApplicationName();

    public void addInPort(InPort inPort);

    public List<InPort> getInputPorts();

    public void addOutPort(OutPort outPort);

    public List<OutPort> getOutputPorts();

    public void addInputPorts(Collection<? extends InPort> inPorts);

    public void addOutPorts(Collection<? extends OutPort> outPorts);

}
