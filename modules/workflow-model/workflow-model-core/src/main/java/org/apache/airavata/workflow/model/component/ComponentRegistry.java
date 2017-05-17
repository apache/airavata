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
package org.apache.airavata.workflow.model.component;

import java.util.List;

import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;

public abstract class ComponentRegistry {

    /**
     * Returns a List of hirarchical Component References. This method should refresh the list even if subclass uses the cache.
     * 
     * @return The ComponentTreeR
     * @throws ComponentRegistryException
     */
    public abstract List<ComponentReference> getComponentReferenceList() throws ComponentRegistryException;

    /**
     * Used by the ComponentTreeViewer to display a node.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return The name to show in the tree.
     */
    public abstract String getName();
    
}