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
import org.apache.airavata.common.model.ParserInput;
import org.apache.airavata.registry.entities.appcatalog.ParserIOEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.ParserIORepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing parser inputs.
 * This service provides operations for ParserInput which are stored as ParserIOEntity
 * with direction=INPUT.
 */
@Service
@Transactional
public class ParserInputService {

    private final ParserIORepository parserIORepository;

    public ParserInputService(ParserIORepository parserIORepository) {
        this.parserIORepository = parserIORepository;
    }

    /**
     * Check if a parser input exists.
     *
     * @param parserInputId the parser input ID
     * @return true if exists
     */
    public boolean isExists(String parserInputId) {
        return parserIORepository.existsById(parserInputId);
    }

    /**
     * Get a parser input by ID.
     *
     * @param parserInputId the parser input ID
     * @return the parser input, or null if not found
     * @throws RegistryException if an error occurs
     */
    public ParserInput get(String parserInputId) throws RegistryException {
        return parserIORepository.findById(parserInputId).map(this::toModel).orElse(null);
    }

    /**
     * Create a new parser input.
     *
     * @param parserInput the parser input to create
     * @return the created parser input with generated ID
     * @throws RegistryException if an error occurs
     */
    public ParserInput create(ParserInput parserInput) throws RegistryException {
        if (parserInput.getId() == null || parserInput.getId().isEmpty()) {
            parserInput.setId(UUID.randomUUID().toString());
        }
        ParserIOEntity entity = toEntity(parserInput);
        ParserIOEntity saved = parserIORepository.save(entity);
        return toModel(saved);
    }

    /**
     * Delete a parser input.
     *
     * @param parserInputId the parser input ID
     * @throws RegistryException if an error occurs
     */
    public void delete(String parserInputId) throws RegistryException {
        parserIORepository.deleteById(parserInputId);
    }

    /**
     * Get all inputs for a parser.
     *
     * @param parserId the parser ID
     * @return list of parser inputs
     */
    public List<ParserInput> getByParserId(String parserId) {
        return parserIORepository.findInputsByParserId(parserId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    // ==================== Mapping ====================

    private ParserInput toModel(ParserIOEntity entity) {
        ParserInput model = new ParserInput();
        model.setId(entity.getId());
        model.setParserId(entity.getParserId());
        model.setName(entity.getName());
        model.setRequiredInput(entity.isRequired());
        if (entity.getType() != null) {
            try {
                model.setType(org.apache.airavata.common.model.IOType.valueOf(entity.getType()));
            } catch (IllegalArgumentException e) {
                // Unknown type, leave as null
            }
        }
        return model;
    }

    private ParserIOEntity toEntity(ParserInput model) {
        ParserIOEntity entity = new ParserIOEntity();
        entity.setId(model.getId());
        entity.setParserId(model.getParserId());
        entity.setName(model.getName());
        entity.setDirection(IODirection.INPUT);
        entity.setRequired(model.getRequiredInput());
        if (model.getType() != null) {
            entity.setType(model.getType().name());
        }
        return entity;
    }
}
