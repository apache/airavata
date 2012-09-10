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

package org.apache.airavata.xbaya.model.registrybrowser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry2;

public class XBayaWorkflowTemplates {
    private AiravataRegistry2 registry;

    public XBayaWorkflowTemplates(AiravataRegistry2 registry) {
        setRegistry(registry);
    }

    public AiravataRegistry2 getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry2 registry) {
        this.registry = registry;
    }

    public List<XBayaWorkflowTemplate> getWorkflows() {
        List<XBayaWorkflowTemplate> workflows = new ArrayList<XBayaWorkflowTemplate>();
        try {
			Map<String, String> workflowMap = registry.getWorkflows();
			for (String xBayaWorkflowName : workflowMap.keySet()) {
			    workflows.add(new XBayaWorkflowTemplate(xBayaWorkflowName,workflowMap.get(xBayaWorkflowName)));
			}
		} catch (RegistryException e) {
			e.printStackTrace();
		}
        return workflows;
    }
}
