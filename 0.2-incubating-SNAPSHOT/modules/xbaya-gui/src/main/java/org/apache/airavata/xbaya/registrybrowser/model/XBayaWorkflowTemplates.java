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

package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.apache.airavata.registry.api.Registry;

public class XBayaWorkflowTemplates {
    private Registry registry;

    public XBayaWorkflowTemplates(Registry registry) {
        setRegistry(registry);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public List<XBayaWorkflowTemplate> getWorkflows() {
        List<XBayaWorkflowTemplate> workflows = new ArrayList<XBayaWorkflowTemplate>();
        Map<QName, Node> workflowMap = registry.getWorkflows(registry.getUsername());
        for (Node xBayaWorkflowNode : workflowMap.values()) {
            workflows.add(new XBayaWorkflowTemplate(xBayaWorkflowNode));
        }
        return workflows;
    }
}
