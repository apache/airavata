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

package org.apache.airavata.xbaya.gpel.component;

import java.util.Collections;
import java.util.List;

import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentReference;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.gpel.client.GcSearchResult;

public class GPELComponentReference extends ComponentReference {

    private GPELRegistry registry;

    private GcSearchResult result;

    private List<WSComponent> components;

    /**
     * Constructs a ResourceCatalogComponentNode.
     * 
     * @param registry
     * 
     * @param result
     */
    public GPELComponentReference(GPELRegistry registry, GcSearchResult result) {
        super(result.getTitle());
        this.registry = registry;
        this.result = result;
    }

    /**
     * @return The component.
     * @throws ComponentRegistryException
     * @throws ComponentException
     */
    @Override
    public WSComponent getComponent() throws ComponentRegistryException, ComponentException {
        return getComponents().get(0);
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentReference#getComponents()
     */
    @Override
    public List<WSComponent> getComponents() throws ComponentRegistryException, ComponentException {
        if (this.components == null) {
            WSComponent component = this.registry.getComponent(this.result.getId());
            this.components = Collections.singletonList(component);
        }
        return this.components;
    }
}