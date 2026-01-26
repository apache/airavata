/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.common.model.IODirection;

/**
 * Unified entity for parser inputs and outputs.
 *
 * <p>This entity consolidates the following legacy entities:
 * <ul>
 *   <li>ParserInputEntity - parser input definitions</li>
 *   <li>ParserOutputEntity - parser output definitions</li>
 * </ul>
 *
 * <p>The direction discriminator allows a single table to store both inputs and outputs
 * while maintaining type-specific queries through the {@link IODirection} enum.
 */
@Entity(name = "ParserIOEntity")
@Table(
        name = "PARSER_IO",
        indexes = {
            @Index(name = "idx_parser_io_parser", columnList = "PARSER_ID"),
            @Index(name = "idx_parser_io_direction", columnList = "DIRECTION"),
            @Index(name = "idx_parser_io_parser_direction", columnList = "PARSER_ID, DIRECTION")
        })
public class ParserIOEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARSER_IO_ID", nullable = false)
    private String id;

    @Column(name = "PARSER_ID", nullable = false, insertable = false, updatable = false)
    private String parserId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DIRECTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private IODirection direction;

    @Column(name = "REQUIRED", nullable = false)
    private boolean required;

    @Column(name = "DATA_TYPE", nullable = false)
    private String type;

    @ManyToOne(targetEntity = ParserEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PARSER_ID")
    private ParserEntity parser;

    public ParserIOEntity() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IODirection getDirection() {
        return direction;
    }

    public void setDirection(IODirection direction) {
        this.direction = direction;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ParserEntity getParser() {
        return parser;
    }

    public void setParser(ParserEntity parser) {
        this.parser = parser;
    }
}
