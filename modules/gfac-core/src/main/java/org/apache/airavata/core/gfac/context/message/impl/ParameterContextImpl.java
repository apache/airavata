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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.core.gfac.context.message.MessageContext;

/**
 * This class contains actual parameters in service invocation.
 */
public class ParameterContextImpl implements MessageContext<ActualParameter> {

    private Map<String, ActualParameter> value;

    public ParameterContextImpl() {
        this.value = new LinkedHashMap<String, ActualParameter>();
    }

    public Iterator<String> getNames() {
        return this.value.keySet().iterator();
    }

    public ActualParameter getValue(String name) {
        return this.value.get(name);
    }

    public String getStringValue(String name) {
        if (this.value.containsKey(name))
            return MappingFactory.toString(this.value.get(name));
        else
            return null;
    }

    public void add(String name, ActualParameter value) {
        this.value.put(name, value);
    }

    public void remove(String name) {
        this.value.remove(name);
    }

    public void setValue(String name, ActualParameter value) {
        this.value.put(name, value);
    }

}
