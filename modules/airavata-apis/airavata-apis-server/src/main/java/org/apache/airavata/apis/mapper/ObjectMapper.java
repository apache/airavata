package org.apache.airavata.apis.mapper;

public interface ObjectMapper<E, M> {

    M mapEntityToModel(E entity);

    E mapModelToEntity(M model);
}
