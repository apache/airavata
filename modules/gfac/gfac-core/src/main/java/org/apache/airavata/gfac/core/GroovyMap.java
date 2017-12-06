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
package org.apache.airavata.gfac.core;/*
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

import java.util.HashMap;
import java.util.Optional;

public class GroovyMap extends HashMap<String, Object> {


    public GroovyMap() {
        super();
        // to mitigate groovy exception groovy.lang.MissingPropertyException: No such property: <name> for class: groovy.lang.Binding
        addDefaultValues();
    }

    public GroovyMap add(Script name, Object value){
        put(name.name, value);
        return this;
    }

    @Override
    public Object get(Object key) {
        return super.getOrDefault(key, null);
    }

    public Object get(Script script) {
        return get(script.name);
    }

    public Optional<String> getStringValue(Script script) {
        Object obj = get(script);
        if (obj instanceof String) {
            return Optional.of((String) obj);
        } else if (obj == null) {
            return Optional.empty();
        } else {
            throw new IllegalArgumentException("Value is not String type");
        }
    }

    private void addDefaultValues() {
        this.add(Script.SHELL_NAME, null)
                .add(Script.QUEUE_NAME, null)
                .add(Script.NODES, null)
                .add(Script.CPU_COUNT, null)
                .add(Script.MAIL_ADDRESS, null)
                .add(Script.ACCOUNT_STRING, null)
                .add(Script.MAX_WALL_TIME, null)
                .add(Script.JOB_NAME, null)
                .add(Script.STANDARD_OUT_FILE, null)
                .add(Script.STANDARD_ERROR_FILE, null)
                .add(Script.QUALITY_OF_SERVICE, null)
                .add(Script.RESERVATION, null)
                .add(Script.EXPORTS, null)
                .add(Script.MODULE_COMMANDS, null)
                .add(Script.SCRATCH_LOCATION, null)
                .add(Script.WORKING_DIR, null)
                .add(Script.PRE_JOB_COMMANDS, null)
                .add(Script.JOB_SUBMITTER_COMMAND, null)
                .add(Script.EXECUTABLE_PATH, null)
                .add(Script.INPUTS, null)
                .add(Script.INPUTS_ALL, null)
                .add(Script.POST_JOB_COMMANDS, null)
                .add(Script.USED_MEM, null)
                .add(Script.PROCESS_PER_NODE, null)
                .add(Script.CHASSIS_NAME, null)
                .add(Script.INPUT_DIR, null)
                .add(Script.OUTPUT_DIR, null)
                .add(Script.USER_NAME, null)
                .add(Script.GATEWAY_ID, null)
                .add(Script.GATEWAY_USER_NAME, null)
                .add(Script.APPLICATION_NAME, null);
    }

}
