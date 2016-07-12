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
package org.apache.airavata.gfac.core.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageContext extends AbstractContext {

    private Map<String, Object> parameters;

    public MessageContext(Map<String, Object> parameters){
        this.parameters = parameters;
    }

    public MessageContext(){
        this.parameters = new LinkedHashMap<String, Object>();
    }

    public Object getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    public void addParameter(String name, Object value){
        parameters.put(name, value);
    }

    public Map<String,Object> getParameters(){
        return parameters;
    }

}
