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
package org.apache.airavata.workflow.model.component.dynamic;

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.dynamic.DynamicPort;
import org.apache.airavata.common.utils.WSConstants;

public class DynamicComponentPort extends ComponentDataPort {

    private DynamicComponent component;

    public DynamicComponentPort(DynamicComponent component) {
        super();
        this.component = component;
        this.type = DataType.STRING;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ComponentDataPort#createPort()
     */
    @Override
    public DataPort createPort() {
        DynamicPort port = new DynamicPort();
        port.setComponentPort(this);

        return port;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    /**
     * Returns the component.
     * 
     * @return The component
     */
    public DynamicComponent getComponent() {
        return this.component;
    }

}