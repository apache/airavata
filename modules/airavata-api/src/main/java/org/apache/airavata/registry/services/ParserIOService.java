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
package org.apache.airavata.registry.services;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.IOType;
import org.apache.airavata.common.model.ParserIO;
import org.apache.airavata.common.model.ParserInput;
import org.apache.airavata.common.model.ParserOutput;
import org.apache.airavata.registry.entities.appcatalog.ParserIOEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.ParserIORepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unified service for parser inputs and outputs.
 * Replaces the legacy ParserInputService and ParserOutputService.
 */
@Service
@Transactional
public class ParserIOService {
    private final ParserIORepository parserIORepository;

    public ParserIOService(ParserIORepository parserIORepository) {
        this.parserIORepository = parserIORepository;
    }

    // ==================== Unified API ====================

    public boolean exists(String id) {
        return parserIORepository.existsById(id);
    }

    public ParserIO get(String id) throws RegistryException {
        ParserIOEntity entity = parserIORepository.findById(id).orElse(null);
        if (entity == null) return null;
        return toModel(entity);
    }

    public ParserIO create(ParserIO parserIO) throws RegistryException {
        ParserIOEntity entity = toEntity(parserIO);
        ParserIOEntity saved = parserIORepository.save(entity);
        return toModel(saved);
    }

    public void delete(String id) throws RegistryException {
        parserIORepository.deleteById(id);
    }

    public List<ParserIO> getByParserId(String parserId) {
        return parserIORepository.findByParserId(parserId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ParserIO> getInputsByParserId(String parserId) {
        return parserIORepository.findInputsByParserId(parserId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ParserIO> getOutputsByParserId(String parserId) {
        return parserIORepository.findOutputsByParserId(parserId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    // ==================== Legacy API for backward compatibility ====================

    /**
     * Get parser input by ID (legacy API).
     */
    public ParserInput getInput(String parserInputId) throws RegistryException {
        ParserIO io = get(parserInputId);
        if (io == null) return null;
        return io.toParserInput();
    }

    /**
     * Create parser input (legacy API).
     */
    public ParserInput createInput(ParserInput parserInput) throws RegistryException {
        ParserIO io = ParserIO.fromParserInput(parserInput);
        ParserIO saved = create(io);
        return saved.toParserInput();
    }

    /**
     * Get parser output by ID (legacy API).
     */
    public ParserOutput getOutput(String parserOutputId) throws RegistryException {
        ParserIO io = get(parserOutputId);
        if (io == null) return null;
        return io.toParserOutput();
    }

    /**
     * Create parser output (legacy API).
     */
    public ParserOutput createOutput(ParserOutput parserOutput) throws RegistryException {
        ParserIO io = ParserIO.fromParserOutput(parserOutput);
        ParserIO saved = create(io);
        return saved.toParserOutput();
    }

    // ==================== Mapping ====================

    private ParserIO toModel(ParserIOEntity entity) {
        ParserIO model = new ParserIO();
        model.setId(entity.getId());
        model.setParserId(entity.getParserId());
        model.setName(entity.getName());
        model.setDirection(entity.getDirection());
        model.setRequired(entity.isRequired());
        if (entity.getType() != null) {
            try {
                model.setType(IOType.valueOf(entity.getType()));
            } catch (IllegalArgumentException e) {
                // Unknown type, leave as null
            }
        }
        return model;
    }

    private ParserIOEntity toEntity(ParserIO model) {
        ParserIOEntity entity = new ParserIOEntity();
        entity.setId(model.getId());
        entity.setParserId(model.getParserId());
        entity.setName(model.getName());
        entity.setDirection(model.getDirection());
        entity.setRequired(model.isRequired());
        if (model.getType() != null) {
            entity.setType(model.getType().name());
        }
        return entity;
    }
}
