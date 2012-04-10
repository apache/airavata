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
package org.apache.airavata.core.gfac.context;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.schemas.gfac.Parameter;

import java.util.Map;

public class JobContext {

    private Map<Parameter,ActualParameter> parameters;


    private String topic;

    private String serviceName;

    private String brokerURL;

    public JobContext(Map<Parameter, ActualParameter> parameters, String topic, String serviceName,String brokerURL) {
        this.parameters = parameters;
        this.topic = topic;
        this.serviceName = serviceName;
        this.brokerURL = brokerURL;
    }

    public Map<Parameter, ActualParameter> getParameters() {
        return parameters;
    }

    public String getTopic() {
        return topic;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }
}
