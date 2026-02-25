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
package org.apache.airavata.core.service;

import java.util.List;
import org.apache.airavata.core.mapper.EntityMapper;
import org.apache.airavata.core.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Base implementation of {@link CrudService} that eliminates per-domain CRUD boilerplate.
 *
 * <p>Subclasses provide the concrete entity mapper, repository, and ID accessor hooks.
 * Standard create/get/update/delete/list operations are handled here; domain-specific
 * methods are added in the concrete service.
 *
 * @param <E> the JPA entity type
 * @param <M> the domain model type
 */
public abstract class AbstractCrudService<E, M> implements CrudService<M> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final JpaRepository<E, String> repository;
    protected final EntityMapper<E, M> mapper;

    protected AbstractCrudService(JpaRepository<E, String> repository, EntityMapper<E, M> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Return the current ID value from the model (e.g. model.getResourceId()). */
    protected abstract String getId(M model);

    /** Set the ID on the model (e.g. model.setResourceId(id)). */
    protected abstract void setId(M model, String id);

    /** Return entities scoped to a gateway. Typically delegates to repository.findByGatewayId(). */
    protected abstract List<E> findByGateway(String gatewayId);

    /** Return a human-readable name for this domain (e.g. "Resource", "Application"). Used in log messages. */
    protected abstract String entityName();

    @Override
    public String create(M model) {
        String id = IdGenerator.ensureId(getId(model));
        setId(model, id);
        E entity = mapper.toEntity(model);
        repository.save(entity);
        logger.debug("Created {} id={}", entityName(), id);
        return id;
    }

    @Override
    public M get(String id) {
        return repository.findById(id).map(mapper::toModel).orElse(null);
    }

    @Override
    public void update(String id, M model) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException(entityName() + " not found: " + id);
        }
        setId(model, id);
        E entity = mapper.toEntity(model);
        repository.save(entity);
        logger.debug("Updated {} id={}", entityName(), id);
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException(entityName() + " not found: " + id);
        }
        repository.deleteById(id);
        logger.debug("Deleted {} id={}", entityName(), id);
    }

    @Override
    public List<M> listByGateway(String gatewayId) {
        return mapper.toModelList(findByGateway(gatewayId));
    }
}
