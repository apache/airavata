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

package org.apache.airavata.xbaya.component.registry;

import java.io.IOException;
import java.util.List;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentException;

public abstract class ComponentReference {

    private String name;

    /**
     * Creates a ComponentLeaf
     * 
     * @param name
     *            The name of the ComponentLeaf
     */
    public ComponentReference(String name) {
        this.name = name;
    }

    /**
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The component.
     * @throws ComponentRegistryException
     * @throws ComponentException
     * @throws IOException
     */
    public abstract Component getComponent() throws ComponentRegistryException, ComponentException;

    /**
     * @return The list of components
     * @throws ComponentRegistryException
     * @throws ComponentException
     * @throws IOException
     */
    public abstract List<? extends Component> getComponents() throws ComponentRegistryException, ComponentException;

    /**
     * The result of this method is used by DefaultMutableTreeNode to show a name in JTree.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name;
    }
}