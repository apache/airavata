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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Unified domain model for parser inputs and outputs.
 * The direction field distinguishes between INPUT and OUTPUT.
 */
public class ParserIO {
    private String id;
    private String parserId;
    private String name;
    private IODirection direction;
    private boolean required;
    private IOType type;

    public ParserIO() {}

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

    public IOType getType() {
        return type;
    }

    public void setType(IOType type) {
        this.type = type;
    }

    /**
     * Convert to legacy ParserInput (only if direction is INPUT).
     */
    public ParserInput toParserInput() {
        if (direction != IODirection.INPUT) {
            throw new IllegalStateException("Cannot convert OUTPUT to ParserInput");
        }
        ParserInput input = new ParserInput();
        input.setId(id);
        input.setParserId(parserId);
        input.setName(name);
        input.setRequiredInput(required);
        input.setType(type);
        return input;
    }

    /**
     * Convert to legacy ParserOutput (only if direction is OUTPUT).
     */
    public ParserOutput toParserOutput() {
        if (direction != IODirection.OUTPUT) {
            throw new IllegalStateException("Cannot convert INPUT to ParserOutput");
        }
        ParserOutput output = new ParserOutput();
        output.setId(id);
        output.setParserId(parserId);
        output.setName(name);
        output.setRequiredOutput(required);
        output.setType(type);
        return output;
    }

    /**
     * Create from legacy ParserInput.
     */
    public static ParserIO fromParserInput(ParserInput input) {
        ParserIO io = new ParserIO();
        io.setId(input.getId());
        io.setParserId(input.getParserId());
        io.setName(input.getName());
        io.setRequired(input.getRequiredInput());
        io.setType(input.getType());
        io.setDirection(IODirection.INPUT);
        return io;
    }

    /**
     * Create from legacy ParserOutput.
     */
    public static ParserIO fromParserOutput(ParserOutput output) {
        ParserIO io = new ParserIO();
        io.setId(output.getId());
        io.setParserId(output.getParserId());
        io.setName(output.getName());
        io.setRequired(output.getRequiredOutput());
        io.setType(output.getType());
        io.setDirection(IODirection.OUTPUT);
        return io;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParserIO parserIO = (ParserIO) o;
        return required == parserIO.required
                && Objects.equals(id, parserIO.id)
                && Objects.equals(parserId, parserIO.parserId)
                && Objects.equals(name, parserIO.name)
                && direction == parserIO.direction
                && type == parserIO.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parserId, name, direction, required, type);
    }

    @Override
    public String toString() {
        return "ParserIO{" +
                "id='" + id + '\'' +
                ", parserId='" + parserId + '\'' +
                ", name='" + name + '\'' +
                ", direction=" + direction +
                ", required=" + required +
                ", type=" + type +
                '}';
    }
}
