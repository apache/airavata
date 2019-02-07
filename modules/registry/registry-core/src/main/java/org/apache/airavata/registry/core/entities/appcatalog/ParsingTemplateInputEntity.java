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
@Table(name = "PARSING_TEMPLATE_INPUT")
public class ParsingTemplateInputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSING_TEMPLATE_INPUT_ID")
    private String id;

    @Column(name = "TARGET_PARSER_INPUT_ID")
    private String targetInputId;

    @Column(name = "APPLICATION_OUTPUT_NAME")
    private String applicationOutputName;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "PARSING_TEMPLATE_ID")
    private String parsingTemplateId;

    @ManyToOne(targetEntity = ParserInputEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "TARGET_PARSER_INPUT_ID")
    private ParserInputEntity input;

    @ManyToOne(targetEntity = ParsingTemplateEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSING_TEMPLATE_ID")
    private ParsingTemplateEntity parsingTemplate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetInputId() {
        return targetInputId;
    }

    public void setTargetInputId(String targetInputId) {
        this.targetInputId = targetInputId;
    }

    public String getApplicationOutputName() {
        return applicationOutputName;
    }

    public void setApplicationOutputName(String applicationOutputName) {
        this.applicationOutputName = applicationOutputName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParsingTemplateId() {
        return parsingTemplateId;
    }

    public void setParsingTemplateId(String parsingTemplateId) {
        this.parsingTemplateId = parsingTemplateId;
    }

    public ParserInputEntity getInput() {
        return input;
    }

    public void setInput(ParserInputEntity input) {
        this.input = input;
    }

    public ParsingTemplateEntity getParsingTemplate() {
        return parsingTemplate;
    }

    public void setParsingTemplate(ParsingTemplateEntity parsingTemplate) {
        this.parsingTemplate = parsingTemplate;
    }
}
