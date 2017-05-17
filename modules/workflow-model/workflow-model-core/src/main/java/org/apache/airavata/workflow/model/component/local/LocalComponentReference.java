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
package org.apache.airavata.workflow.model.component.local;

import java.io.File;
import java.util.List;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.ws.WSComponent;

public class LocalComponentReference extends ComponentReference {

    private LocalComponentRegistry registry;

    private File file;

    private List<WSComponent> components;

    /**
     * Constructs a LocalComponentNode.
     * 
     * @param name
     * @param file
     * @param registry
     */
    public LocalComponentReference(String name, File file, LocalComponentRegistry registry) {
        super(name);
        this.file = file;
        this.registry = registry;
    }

    /**
     * @throws ComponentException
     * @throws ComponentRegistryException
     * @see org.apache.airavata.workflow.model.component.ComponentReference#getComponent()
     */
    @Override
    @Deprecated
    public Component getComponent() throws ComponentException, ComponentRegistryException {
        return getComponents().get(0);
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ComponentReference#getComponents()
     */
    @Override
    public List<WSComponent> getComponents() throws ComponentRegistryException, ComponentException {
        if (this.components == null) {
            this.components = this.registry.getComponents(this.file);
        }
        return this.components;
    }
}