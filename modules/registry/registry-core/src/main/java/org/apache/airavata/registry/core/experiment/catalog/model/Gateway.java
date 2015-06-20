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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "GATEWAY")
public class Gateway {
    private final static Logger logger = LoggerFactory.getLogger(Gateway.class);
    private String gatewayId;
    private String gatewayName;
    private String domain;
    private String emailAddress;
    private Collection<GatewayWorker> gatewayWorkers;
    private Collection<Project> projects;

    @Id
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Basic
    @Column(name = "GATEWAY_NAME")
    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @Basic
    @Column(name = "DOMAIN")
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Basic
    @Column(name = "EMAIL_ADDRESS")
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Gateway gateway = (Gateway) o;
//
//        if (domain != null ? !domain.equals(gateway.domain) : gateway.domain != null) return false;
//        if (emailAddress != null ? !emailAddress.equals(gateway.emailAddress) : gateway.emailAddress != null)
//            return false;
//        if (gatewayId != null ? !gatewayId.equals(gateway.gatewayId) : gateway.gatewayId != null) return false;
//        if (gatewayName != null ? !gatewayName.equals(gateway.gatewayName) : gateway.gatewayName != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = gatewayId != null ? gatewayId.hashCode() : 0;
//        result = 31 * result + (gatewayName != null ? gatewayName.hashCode() : 0);
//        result = 31 * result + (domain != null ? domain.hashCode() : 0);
//        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
//        return result;
//    }

    @OneToMany(mappedBy = "gateway")
    public Collection<GatewayWorker> getGatewayWorkers() {
        return gatewayWorkers;
    }

    public void setGatewayWorkers(Collection<GatewayWorker> gatewayWorkersByGatewayId) {
        this.gatewayWorkers = gatewayWorkersByGatewayId;
    }

    @OneToMany(mappedBy = "gateway")
    public Collection<Project> getProjects() {
        return projects;
    }

    public void setProjects(Collection<Project> projectByGatewayId) {
        this.projects = projectByGatewayId;
    }
}