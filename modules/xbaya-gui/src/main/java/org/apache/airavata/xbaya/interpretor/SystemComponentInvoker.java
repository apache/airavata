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

package org.apache.airavata.xbaya.interpretor;

import java.util.Hashtable;
import java.util.Map;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.invoker.Invoker;

import xsul.wsif.WSIFMessage;
import xsul.xwsif_runtime.WSIFClient;

public class SystemComponentInvoker implements Invoker {

    private Map<String, Object> outputs = new Hashtable<String, Object>();

    /**
     * 
     * @param key
     * @param value
     */
    public void addOutput(String key, Object value) {
        outputs.put(key, value);
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getOutput(java.lang.String)
     */
    @Override
    public Object getOutput(String name) {
        Object out = null;
        while (out == null) {
            try {
                out = this.outputs.get(name);
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return out;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getOutputs()
     */
    @Override
    public WSIFMessage getOutputs() {
        return null;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#invoke()
     */
    @Override
    public boolean invoke() {
        return true;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#setInput(java.lang.String, java.lang.Object)
     */
    @Override
    public void setInput(String name, Object value) {
    }

    /**
     * @see org.apache.airavata.xbaya.wXPath Operatorsorkflow.Invoker#setOperation(java.lang.String)
     */
    @Override
    public void setOperation(String operationName) {
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#setup()
     */
    @Override
    public void setup() {
    }

    @Override
    public WSIFClient getClient() {
        return null;
    }

    @Override
    public WSIFMessage getInputs() throws WorkflowException {
        return null;
    }

    @Override
    public WSIFMessage getFault() throws WorkflowException {
        return null;
    }
}