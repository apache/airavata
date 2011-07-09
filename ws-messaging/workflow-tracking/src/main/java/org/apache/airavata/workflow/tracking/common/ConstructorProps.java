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

package org.apache.airavata.workflow.tracking.common;

import java.util.HashMap;
import java.util.Map;

/**
 * simple helper class to set properties as a chain. Extends Properties, so can be passed as constructor to notifier
 * factory. can also load an external propetirs file that conforms to Java Properties file schema at
 * http://java.sun.com/dtd/properties.dtd.
 * 
 * e.g. props = util.Props.newProps(CONSTS.WORKFLOW_ID, "wfId001"). set(CONSTS.NODE_ID, "nodeId001").
 * set(CONSTS.TIMESTEP, "time0001"). set(CONSTS.BROKER_URL, "rainier:12346")); Notifier notifier =
 * NotifierFactory.createNotifier(props);
 */
public class ConstructorProps {

    private Map<ConstructorConsts, Object> localMap;

    public ConstructorProps() {
        localMap = new HashMap<ConstructorConsts, Object>();
    }

    public static ConstructorProps newProps() {
        return new ConstructorProps();
    }

    public static ConstructorProps newProps(ConstructorConsts key, Object value) {
        return newProps().set(key, value);
    }

    public ConstructorProps set(ConstructorConsts key, Object value) {
        if (!key.checkValueType(value.getClass()))
            throw new ClassCastException("passed value class type: " + value.getClass() + " != expected class type: "
                    + key.getValueType() + " for key: " + key);
        localMap.put(key, value);
        return this;
    }

    public Object get(ConstructorConsts key) {
        return localMap.get(key);
    }

    public Object get(ConstructorConsts key, Object defaultValue) {
        Object value = localMap.get(key);
        return value == null ? defaultValue : value;
    }
}
