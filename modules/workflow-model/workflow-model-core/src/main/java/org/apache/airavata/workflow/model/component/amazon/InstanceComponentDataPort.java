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
package org.apache.airavata.workflow.model.component.amazon;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.amazon.InstanceDataPort;

public class InstanceComponentDataPort extends ComponentDataPort {

    private static final String PORT_NAME = "Parameter";

    private static final String PORT_DESCRIPTION = "This port can be connected to any type.";

    /**
     * 
     * Constructs a InstanceComponentDataPort.
     * 
     * @param name
     */
    public InstanceComponentDataPort(String name) {
        super(name);
        this.type = DataType.STRING;
        setName(PORT_NAME);
        setDescription(PORT_DESCRIPTION);
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ComponentDataPort#createPort()
     */
    @Override
    public DataPort createPort() {
        DataPort n = new InstanceDataPort();
        n.setName(PORT_NAME);
        return n;
    }

}