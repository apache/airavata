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
package org.apache.airavata.workflow.model.component.system;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.apache.airavata.workflow.model.component.dynamic.DynamicComponent;

public class SystemComponentRegistry extends ComponentRegistry {

    private static final String NAME = "System Components";

    private Map<String, Component> componentMap;

    /**
     * Creates a SystemComponentRegistry.
     */
    public SystemComponentRegistry() {
        // Use LinkedHashMap to preserve the order.
        this.componentMap = new LinkedHashMap<String, Component>();
        this.componentMap.put(InputComponent.NAME, new InputComponent());
        this.componentMap.put(DifferedInputComponent.NAME, new DifferedInputComponent());
        this.componentMap.put(S3InputComponent.NAME, new S3InputComponent());
        this.componentMap.put(OutputComponent.NAME, new OutputComponent());
        this.componentMap.put(ConstantComponent.NAME, new ConstantComponent());
        this.componentMap.put(MemoComponent.NAME, new MemoComponent());
        this.componentMap.put(IfComponent.NAME, new IfComponent());
        this.componentMap.put(EndifComponent.NAME, new EndifComponent());
        this.componentMap.put(ReceiveComponent.NAME, new ReceiveComponent());
        this.componentMap.put(ForEachComponent.NAME, new ForEachComponent());
        this.componentMap.put(EndForEachComponent.NAME, new EndForEachComponent());
        this.componentMap.put(DoWhileComponent.NAME, new DoWhileComponent());
        this.componentMap.put(EndDoWhileComponent.NAME, new EndDoWhileComponent());
        this.componentMap.put(BlockComponent.NAME, new BlockComponent());
        this.componentMap.put(EndBlockComponent.NAME, new EndBlockComponent());
        this.componentMap.put(DynamicComponent.NAME, new DynamicComponent());
        this.componentMap.put(StreamSourceComponent.NAME, new StreamSourceComponent());
        this.componentMap.put(ExitComponent.NAME, new ExitComponent());
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns a ComponentTree.
     *
     * @return The ComponentTree
     */
    @Override
    public List<ComponentReference> getComponentReferenceList() {
        List<ComponentReference> tree = new ArrayList<ComponentReference>();
        for (String name : this.componentMap.keySet()) {
            Component component = this.componentMap.get(name);
            SystemComponentReference componentReference = new SystemComponentReference(name, component);
            tree.add(componentReference);
        }
        return tree;
    }

    /**
     * @param name2
     * @param workflowComponent
     */
    public void addComponent(String name2, SubWorkflowComponent workflowComponent) {

        this.componentMap.put(name2, workflowComponent);
    }

}