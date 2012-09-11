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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Date;

@Entity
@IdClass(User_Workflow_PK.class)
public class User_Workflow {
    @Id
    private String gateway_name;
    @Id
    private String owner;
    @Id
    private String template_name;

    @ManyToOne
    @JoinColumn(name = "gateway_name")
    private Gateway gateway;

    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "user_name")
    private Users user;

    private String path;
    private Date last_updated_date;
    private String workflow_graph;

    public String getTemplate_name() {
        return template_name;
    }

    public Users getUser() {
        return user;
    }

    public void setTemplate_name(String template_name) {
        this.template_name = template_name;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public String getOwner() {
        return owner;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPath() {
        return path;
    }

    public Date getLast_updated_date() {
        return last_updated_date;
    }

    public String getWorkflow_graph() {
        return workflow_graph;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setWorkflow_graph(String workflow_graph) {
        this.workflow_graph = workflow_graph;
    }

    public void setLast_updated_date(Date last_updated_date) {
        this.last_updated_date = last_updated_date;
    }

}

class User_Workflow_PK {
    private String template_name;
    private String gateway_name;
    private String owner;

    public User_Workflow_PK() {
        ;
    }

    @Override
	public boolean equals(Object o) {
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}

    public String getTemplate_name() {
        return template_name;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTemplate_name(String template_name) {
        this.template_name = template_name;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }
}
