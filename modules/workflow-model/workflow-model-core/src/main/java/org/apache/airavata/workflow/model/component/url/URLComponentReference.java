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
package org.apache.airavata.workflow.model.component.url;

import java.util.List;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ws.WSComponent;

public class URLComponentReference extends ComponentReference {

    private List<WSComponent> components;

    /**
     * Constructs a URLComponentReference.
     * 
     * @param name
     * @param components
     */
    public URLComponentReference(String name, List<WSComponent> components) {
        super(name);
        this.components = components;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ComponentReference#getComponent()
     */
    @Override
    public Component getComponent() {
        return this.components.get(0);
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ComponentReference#getComponents()
     */
    @Override
    public List<? extends Component> getComponents() {
        return this.components;
    }
}