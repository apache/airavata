/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.JsonListConverter;

@Entity
@Table(name = "PARSING_TEMPLATE")
public class ParsingTemplateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSING_TEMPLATE_ID")
    private String id;

    @Column(name = "APP_INTERFACE_ID")
    private String applicationInterface;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Lob
    @Column(name = "INITIAL_INPUTS_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> initialInputs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(String applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

    public List<Map<String, Object>> getInitialInputs() {
        return initialInputs;
    }

    public void setInitialInputs(List<Map<String, Object>> initialInputs) {
        this.initialInputs = initialInputs;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }
}
