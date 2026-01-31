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
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.IODirection;
import org.apache.airavata.common.model.ParserOutput;
import org.apache.airavata.registry.entities.appcatalog.ParserIOEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.ParserIORepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing parser outputs.
 * This service provides operations for ParserOutput which are stored as ParserIOEntity
 * with direction=OUTPUT.
 */
@Service
@Transactional
public class ParserOutputService {

    private final ParserIORepository parserIORepository;

    public ParserOutputService(ParserIORepository parserIORepository) {
        this.parserIORepository = parserIORepository;
    }

    /**
     * Check if a parser output exists.
     *
     * @param parserOutputId the parser output ID
     * @return true if exists
     */
    public boolean isExists(String parserOutputId) {
        return parserIORepository.existsById(parserOutputId);
    }

    /**
     * Get a parser output by ID.
     *
     * @param parserOutputId the parser output ID
     * @return the parser output, or null if not found
     * @throws RegistryException if an error occurs
     */
    public ParserOutput get(String parserOutputId) throws RegistryException {
        return parserIORepository.findById(parserOutputId).map(this::toModel).orElse(null);
    }

    /**
     * Create a new parser output.
     *
     * @param parserOutput the parser output to create
     * @return the created parser output with generated ID
     * @throws RegistryException if an error occurs
     */
    public ParserOutput create(ParserOutput parserOutput) throws RegistryException {
        if (parserOutput.getId() == null || parserOutput.getId().isEmpty()) {
            parserOutput.setId(UUID.randomUUID().toString());
        }
        ParserIOEntity entity = toEntity(parserOutput);
        ParserIOEntity saved = parserIORepository.save(entity);
        return toModel(saved);
    }

    /**
     * Delete a parser output.
     *
     * @param parserOutputId the parser output ID
     * @throws RegistryException if an error occurs
     */
    public void delete(String parserOutputId) throws RegistryException {
        parserIORepository.deleteById(parserOutputId);
    }

    /**
     * Get all outputs for a parser.
     *
     * @param parserId the parser ID
     * @return list of parser outputs
     */
    public List<ParserOutput> getByParserId(String parserId) {
        return parserIORepository.findOutputsByParserId(parserId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    // ==================== Mapping ====================

    private ParserOutput toModel(ParserIOEntity entity) {
        ParserOutput model = new ParserOutput();
        model.setId(entity.getId());
        model.setParserId(entity.getParserId());
        model.setName(entity.getName());
        model.setRequiredOutput(entity.isRequired());
        if (entity.getType() != null) {
            try {
                model.setType(org.apache.airavata.common.model.IOType.valueOf(entity.getType()));
            } catch (IllegalArgumentException e) {
                // Unknown type, leave as null
            }
        }
        return model;
    }

    private ParserIOEntity toEntity(ParserOutput model) {
        ParserIOEntity entity = new ParserIOEntity();
        entity.setId(model.getId());
        entity.setParserId(model.getParserId());
        entity.setName(model.getName());
        entity.setDirection(IODirection.OUTPUT);
        entity.setRequired(model.getRequiredOutput());
        if (model.getType() != null) {
            entity.setType(model.getType().name());
        }
        return entity;
    }
}
