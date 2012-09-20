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
@IdClass(Application_Descriptor_PK.class)
public class Application_Descriptor {
    @Id
    private String application_descriptor_ID;
    @Id
    private String gateway_name;

    private String host_descriptor_ID;
    private String service_descriptor_ID;

    @Lob
    private byte[] application_descriptor_xml;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name = "gateway_name")
    private Gateway gateway;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name = "updated_user", referencedColumnName = "user_name")
    private Users user;

    public String getApplication_descriptor_ID() {
        return application_descriptor_ID;
    }

    public byte[] getApplication_descriptor_xml() {
        return application_descriptor_xml;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public String getHost_descriptor_ID() {
        return host_descriptor_ID;
    }

    public String getService_descriptor_ID() {
        return service_descriptor_ID;
    }

    public void setHost_descriptor_ID(String host_descriptor_ID) {
        this.host_descriptor_ID = host_descriptor_ID;
    }

    public void setService_descriptor_ID(String service_descriptor_ID) {
        this.service_descriptor_ID = service_descriptor_ID;
    }

    public void setApplication_descriptor_ID(String application_descriptor_ID) {
        this.application_descriptor_ID = application_descriptor_ID;
    }

    public void setApplication_descriptor_xml(byte[] application_descriptor_xml) {
        this.application_descriptor_xml = application_descriptor_xml;
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

    public String getGateway_name() {
        return gateway_name;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }
}
