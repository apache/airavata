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
package org.apache.airavata.orchestrator.core.context;

import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.OrchestratorRegistry;

import java.util.ArrayList;
import java.util.List;

public class OrchestratorContext {
    private List<GFACInstance> gfacInstanceList;

    private OrchestratorConfiguration orchestratorConfiguration;

    private AiravataRegistry2 registry;

    public OrchestratorContext(List<GFACInstance> gfacInstanceList) {
        this.gfacInstanceList = new ArrayList<GFACInstance>();
    }

    public List<GFACInstance> getGfacInstanceList() {
        return gfacInstanceList;
    }

    public void addGfacInstanceList(GFACInstance instance) {
        this.gfacInstanceList.add(instance);
    }

    public OrchestratorConfiguration getOrchestratorConfiguration() {
        return orchestratorConfiguration;
    }

    public void setOrchestratorConfiguration(OrchestratorConfiguration orchestratorConfiguration) {
        this.orchestratorConfiguration = orchestratorConfiguration;
    }

    public OrchestratorRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry2 registry) {
        this.registry = registry;
    }
}
