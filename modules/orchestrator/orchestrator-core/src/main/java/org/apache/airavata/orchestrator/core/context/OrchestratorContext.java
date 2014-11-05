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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.gfac.client.GFACInstance;
import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.zookeeper.ZooKeeper;

/**
 * This is the context object used in orchestrator which
 */
public class OrchestratorContext {
    private List<GFACInstance> gfacInstanceList;

    private OrchestratorConfiguration orchestratorConfiguration;

    private Registry newRegistry;

    private static ZooKeeper zk; // this instance can be accessed by the Validators and other components
    
    public OrchestratorContext(List<GFACInstance> gfacInstanceList) {
        if (gfacInstanceList != null) {
            this.gfacInstanceList = gfacInstanceList;
        }else {
            this.gfacInstanceList = new ArrayList<GFACInstance>();
        }
    }

    public OrchestratorContext() {
        this(null);
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

    public Registry getNewRegistry() {
        return newRegistry;
    }

    public void setNewRegistry(Registry newRegistry) {
        this.newRegistry = newRegistry;
    }

    public void setGfacInstanceList(List<GFACInstance> gfacInstanceList) {
        this.gfacInstanceList.addAll(gfacInstanceList);
    }

    public  void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public static ZooKeeper getZk() {
        return zk;
    }
}
