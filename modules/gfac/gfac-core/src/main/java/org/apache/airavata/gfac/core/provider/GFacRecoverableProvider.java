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
package org.apache.airavata.gfac.core.provider;

import org.apache.airavata.gfac.core.context.JobExecutionContext;

/**
 * This provider type can be used to implement stateful Operation and
 * we recommend to use the ZK client to store and retrieve the states
 * of the handler implementation. Framework level we use
 * ZK to decide handler ran successfully or not so each handler
 * execution details can be found in following zk path
 * /gfac-experiment/<gfac-node-name>/experimentId+taskId/full-qualified-handlername/state
 * ex: /gfac-experiment/gfac-node0/echoExperiment_2c6c11b8-dea0-4ec8-9832-f3e69fe2e6bb+IDontNeedaNode_682faa66-6218-4897-9271-656bfb8b2bd1/org.apache.airavata.gfac.handlers.Test/state
 */
public interface GFacRecoverableProvider extends GFacProvider {
    /**
     * This method can be used to implement recovering part of the stateful handler
     * If you do not want to recover an already ran handler you can simply implement
     * GfacAbstract Handler or GFacHandler or leave this recover method empty.
     *
     * @param jobExecutionContext
     */
    public void recover(JobExecutionContext jobExecutionContext);
}
