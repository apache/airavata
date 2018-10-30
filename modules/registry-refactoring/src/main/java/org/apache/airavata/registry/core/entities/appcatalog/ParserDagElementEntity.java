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
@Table(name = "PARSER_DAG_ELEMENT")
public class ParserDagElementEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_DAG_ELEMENT_ID")
    private String id;

    @Column(name = "PARENT_PARSER_INFO_ID")
    private String parentParserId;

    @Column(name = "CHILD_PARSER_INFO_ID")
    private String childParserId;

    @Column(name = "PARSING_TEMPLATE_ID")
    private String parsingTemplateId;

    @OneToMany(targetEntity = ParserDagInputOutputMappingEntity.class, cascade = CascadeType.ALL, orphanRemoval = true,
            mappedBy = "parserDagElement", fetch = FetchType.EAGER)
    private List<ParserDagInputOutputMappingEntity> inputOutputMapping;

    @ManyToOne(targetEntity = ParserInfoEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARENT_PARSER_INFO_ID")
    private ParserInfoEntity parentParser;

    @ManyToOne(targetEntity = ParserInfoEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CHILD_PARSER_INFO_ID")
    private ParserInfoEntity childParser;

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

    public List<ParserDagInputOutputMappingEntity> getInputOutputMapping() {
        return inputOutputMapping;
    }

    public void setInputOutputMapping(List<ParserDagInputOutputMappingEntity> inputOutputMapping) {
        this.inputOutputMapping = inputOutputMapping;
    }

    public ParserInfoEntity getParentParser() {
        return parentParser;
    }

    public void setParentParser(ParserInfoEntity parentParser) {
        this.parentParser = parentParser;
    }

    public ParserInfoEntity getChildParser() {
        return childParser;
    }

    public void setChildParser(ParserInfoEntity childParser) {
        this.childParser = childParser;
    }

    public String getParsingTemplateId() {
        return parsingTemplateId;
    }

    public void setParsingTemplateId(String parsingTemplateId) {
        this.parsingTemplateId = parsingTemplateId;
    }

    public ParsingTemplateEntity getParsingTemplate() {
        return parsingTemplate;
    }

    public void setParsingTemplate(ParsingTemplateEntity parsingTemplate) {
        this.parsingTemplate = parsingTemplate;
    }
}
