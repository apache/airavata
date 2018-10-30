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
    private boolean requiredFile;

    @Column(name = "PARSER_INFO_ID")
    private String parserInfoId;

    @ManyToOne(targetEntity = ParserInfoEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_INFO_ID")
    private ParserInfoEntity parserInfo;

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

    public boolean isRequiredFile() {
        return requiredFile;
    }

    public void setRequiredFile(boolean requiredFile) {
        this.requiredFile = requiredFile;
    }

    public String getParserInfoId() {
        return parserInfoId;
    }

    public void setParserInfoId(String parserInfoId) {
        this.parserInfoId = parserInfoId;
    }

    public ParserInfoEntity getParserInfo() {
        return parserInfo;
    }

    public void setParserInfo(ParserInfoEntity parserInfo) {
        this.parserInfo = parserInfo;
    }
}
