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
package org.apache.airavata.xbaya.workflow.proxy;

import java.net.URISyntaxException;

import org.apache.airavata.workflow.model.wf.Workflow;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import xsul.lead.LeadContextHeader;

public interface WorkflowContext {

    public void prepare(WorkflowClient client, Workflow workflow) throws GSSException, URISyntaxException;

    public LeadContextHeader getHeader();

    public GSSCredential getCredentials();

    public String getTopic();

}
