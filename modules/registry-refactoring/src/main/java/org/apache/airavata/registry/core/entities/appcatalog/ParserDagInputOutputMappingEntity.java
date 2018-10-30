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
@Table(name = "PARSER_DAG_IO_MAPPING")
public class ParserDagInputOutputMappingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_DAG_IO_MAPPING_ID")
    private String id;

    @Column(name = "PARSER_INPUT_ID")
    private String inputId;

    @Column(name = "PARSER_OUTPUT_ID")
    private String outputId;

    @Column(name = "PARSER_DAG_ELEMENT_ID")
    private String parserDagElementId;

    @ManyToOne(targetEntity = ParserInputEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_INPUT_ID")
    private ParserInputEntity input;

    @ManyToOne(targetEntity = ParserOutputEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_OUTPUT_ID")
    private ParserOutputEntity output;

    @ManyToOne(targetEntity = ParserDagElementEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_DAG_ELEMENT_ID")
    private ParserDagElementEntity parserDagElement;

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

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public String getParserDagElementId() {
        return parserDagElementId;
    }

    public void setParserDagElementId(String parserDagElementId) {
        this.parserDagElementId = parserDagElementId;
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

    public ParserDagElementEntity getParserDagElement() {
        return parserDagElement;
    }

    public void setParserDagElement(ParserDagElementEntity parserDagElement) {
        this.parserDagElement = parserDagElement;
    }
}
