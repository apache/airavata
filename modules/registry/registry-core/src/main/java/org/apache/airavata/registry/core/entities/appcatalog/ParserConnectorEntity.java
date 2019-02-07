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
import java.util.List;

@Entity
@Table(name = "PARSER_CONNECTOR")
public class ParserConnectorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_CONNECTOR_ID")
    private String id;

    @Column(name = "PARENT_PARSER_ID")
    private String parentParserId;

    @Column(name = "CHILD_PARSER_ID")
    private String childParserId;

    @Column(name = "PARSING_TEMPLATE_ID")
    private String parsingTemplateId;

    @OneToMany(targetEntity = ParserConnectorInputEntity.class, cascade = CascadeType.ALL, orphanRemoval = true,
            mappedBy = "parserConnector", fetch = FetchType.EAGER)
    private List<ParserConnectorInputEntity> connectorInputs;

    @ManyToOne(targetEntity = ParserEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARENT_PARSER_ID")
    private ParserEntity parentParser;

    @ManyToOne(targetEntity = ParserEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CHILD_PARSER_ID")
    private ParserEntity childParser;

    @ManyToOne(targetEntity = ParsingTemplateEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSING_TEMPLATE_ID")
    private ParsingTemplateEntity parsingTemplate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentParserId() {
        return parentParserId;
    }

    public void setParentParserId(String parentParserId) {
        this.parentParserId = parentParserId;
    }

    public String getChildParserId() {
        return childParserId;
    }

    public void setChildParserId(String childParserId) {
        this.childParserId = childParserId;
    }

    public String getParsingTemplateId() {
        return parsingTemplateId;
    }

    public void setParsingTemplateId(String parsingTemplateId) {
        this.parsingTemplateId = parsingTemplateId;
    }

    public List<ParserConnectorInputEntity> getConnectorInputs() {
        return connectorInputs;
    }

    public void setConnectorInputs(List<ParserConnectorInputEntity> connectorInputs) {
        this.connectorInputs = connectorInputs;
    }

    public ParserEntity getParentParser() {
        return parentParser;
    }

    public void setParentParser(ParserEntity parentParser) {
        this.parentParser = parentParser;
    }

    public ParserEntity getChildParser() {
        return childParser;
    }

    public void setChildParser(ParserEntity childParser) {
        this.childParser = childParser;
    }

    public ParsingTemplateEntity getParsingTemplate() {
        return parsingTemplate;
    }

    public void setParsingTemplate(ParsingTemplateEntity parsingTemplate) {
        this.parsingTemplate = parsingTemplate;
    }
}
