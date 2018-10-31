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
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "PARSER_OUTPUT")
public class ParserOutputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_OUTPUT_ID")
    private String id;

    @Column(name = "PARSER_OUTPUT_NAME")
    private String name;

    @Column(name = "PARSER_OUTPUT_REQUIRED")
    private boolean requiredOutput;

    @Column(name = "PARSER_ID")
    private String parserId;

    @ManyToOne(targetEntity = ParserEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_ID")
    private ParserEntity parser;

    @Column(name = "OUTPUT_TYPE")
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequiredOutput() {
        return requiredOutput;
    }

    public void setRequiredOutput(boolean requiredOutput) {
        this.requiredOutput = requiredOutput;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }

    public ParserEntity getParser() {
        return parser;
    }

    public void setParser(ParserEntity parser) {
        this.parser = parser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
