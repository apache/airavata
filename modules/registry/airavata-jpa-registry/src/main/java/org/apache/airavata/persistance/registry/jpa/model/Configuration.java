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
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@IdClass(Configuration_PK.class)
public class Configuration implements Serializable {
    @Id
    private String config_key;
    @Id
    private String config_val;
    @Id
    private String category_id;
    private Timestamp expire_date;

    public String getConfig_key() {
        return config_key;
    }

    public String getConfig_val() {
        return config_val;
    }

    public Timestamp getExpire_date() {
        return expire_date;
    }

    public void setConfig_key(String config_key) {
        this.config_key = config_key;
    }

    public void setConfig_val(String config_val) {
        this.config_val = config_val;
    }

    public void setExpire_date(Timestamp expire_date) {
        this.expire_date = expire_date;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }
}
