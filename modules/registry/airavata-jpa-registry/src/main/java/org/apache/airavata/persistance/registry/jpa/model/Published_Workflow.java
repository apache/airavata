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
import javax.persistence.OneToMany;
import java.sql.Date;

@Entity
@IdClass(Published_Workflow_PK.class)
public class Published_Workflow {

    @Id
    private String publish_workflow_name;
    private String version;
    private Date published_date;
    private String workflow_content;

    @Id
    private int gateway_ID;

//    @Id
//    @ManyToOne
//    @JoinColumn(name = "gateway_ID")
//    private Gateway gateway;

    public String getPublish_workflow_name() {
        return publish_workflow_name;
    }

    public String getVersion() {
        return version;
    }

    public Date getPublished_date() {
        return published_date;
    }

    public String getWorkflow_content() {
        return workflow_content;
    }

//    public Gateway getGateway() {
//        return gateway;
//    }

    public void setPublish_workflow_name(String publish_workflow_name) {
        this.publish_workflow_name = publish_workflow_name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPublished_date(Date published_date) {
        this.published_date = published_date;
    }

    public void setWorkflow_content(String workflow_content) {
        this.workflow_content = workflow_content;
    }

    public int getGateway_ID() {
        return gateway_ID;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    //    public void setGateway(Gateway gateway) {
//        this.gateway = gateway;
//    }
}

class Published_Workflow_PK {
    private int gateway_ID;
    private String publish_workflow_name;

    public Published_Workflow_PK() {
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


    public int getGateway_ID() {
        return gateway_ID;
    }

    public String getPublish_workflow_name() {
        return publish_workflow_name;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    public void setPublish_workflow_name(String publish_workflow_name) {
        this.publish_workflow_name = publish_workflow_name;
    }

}

