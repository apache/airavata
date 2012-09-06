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

@Entity
@IdClass(Gateway_Worker_PK.class)
public class Gateway_Worker {
    @Id
    private int gateway_ID;

    @Id
    private int user_ID;

    @ManyToOne
    @JoinColumn(name = "gateway_ID")
    private Gateway gateway;


    @ManyToOne
    @JoinColumn(name = "user_ID")
    private Users user;

    public int getUser_ID() {
        return user_ID;
    }

    public void setUser_ID(int user_ID) {
        this.user_ID = user_ID;
    }

    public int getGateway_ID() {
        return gateway_ID;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public Users getUser() {
        return user;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}

class Gateway_Worker_PK {
    private int gateway_ID;
    private int user_ID;

    public Gateway_Worker_PK() {
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

    public int getUser_ID() {
        return user_ID;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    public void setUser_ID(int user_ID) {
        this.user_ID = user_ID;
    }
}
