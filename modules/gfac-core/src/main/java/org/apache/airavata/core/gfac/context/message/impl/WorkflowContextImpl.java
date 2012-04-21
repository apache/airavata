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

package org.apache.airavata.core.gfac.context.message.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.airavata.core.gfac.context.message.MessageContext;

/**
 * This class contains actual parameters in service invocation.
 */
public class WorkflowContextImpl implements MessageContext<String> {

    public static final String WORKFLOW_CONTEXT_NAME = "workflow_context";
    public static final String WORKFLOW_ID = "workflowId";

    private Map<String, String> value;

    public WorkflowContextImpl() {
        this.value = new HashMap<String, String>();
    }

    public Iterator<String> getNames() {
        return this.value.keySet().iterator();
    }

    public String getValue(String name) {
        return this.value.get(name);
    }

    public String getStringValue(String name) {
        return this.value.get(name);
    }

    public void add(String name, String value) {
        this.value.put(name, value);
    }

    public void setValue(String name, String value) {
        this.value.put(name, value);
    }

	@Override
	public void remove(String name) {
		 this.value.remove(name);

	}

}
