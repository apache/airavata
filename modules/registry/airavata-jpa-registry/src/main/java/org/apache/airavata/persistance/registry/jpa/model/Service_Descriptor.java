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
package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.*;

@Entity
public class Service_Descriptor {
    @Id
    private String service_descriptor_ID;
    private String service_descriptor_xml;

    @ManyToOne
    @JoinColumn(name = "gateway_ID")
    private Gateway gateway;

    @ManyToOne
    @JoinColumn(name = "user_ID")
    private Users user;

    public String getService_descriptor_ID() {
        return service_descriptor_ID;
    }

    public String getService_descriptor_xml() {
        return service_descriptor_xml;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setService_descriptor_ID(String service_descriptor_ID) {
        this.service_descriptor_ID = service_descriptor_ID;
    }

    public void setService_descriptor_xml(String service_descriptor_xml) {
        this.service_descriptor_xml = service_descriptor_xml;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}

