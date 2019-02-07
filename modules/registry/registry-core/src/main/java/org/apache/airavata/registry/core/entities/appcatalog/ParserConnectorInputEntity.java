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
@Table(name = "PARSER_CONNECTOR_INPUT")
public class ParserConnectorInputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_CONNECTOR_INPUT_ID")
    private String id;

    @Column(name = "PARSER_INPUT_ID")
    private String inputId;

    @Column(name = "PARSER_OUTPUT_ID")
    private String parentOutputId;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "PARSER_CONNECTOR_ID")
    private String parserConnectorId;

    @ManyToOne(targetEntity = ParserInputEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_INPUT_ID")
    private ParserInputEntity input;

    @ManyToOne(targetEntity = ParserOutputEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_OUTPUT_ID")
    private ParserOutputEntity output;

    @ManyToOne(targetEntity = ParserConnectorEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_CONNECTOR_ID")
    private ParserConnectorEntity parserConnector;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public String getParentOutputId() {
        return parentOutputId;
    }

    public void setParentOutputId(String parentOutputId) {
        this.parentOutputId = parentOutputId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParserConnectorId() {
        return parserConnectorId;
    }

    public void setParserConnectorId(String parserConnectorId) {
        this.parserConnectorId = parserConnectorId;
    }

    public ParserInputEntity getInput() {
        return input;
    }

    public void setInput(ParserInputEntity input) {
        this.input = input;
    }

    public ParserOutputEntity getOutput() {
        return output;
    }

    public void setOutput(ParserOutputEntity output) {
        this.output = output;
    }

    public ParserConnectorEntity getParserConnector() {
        return parserConnector;
    }

    public void setParserConnector(ParserConnectorEntity parserConnector) {
        this.parserConnector = parserConnector;
    }
}
