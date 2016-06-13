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
package org.apache.airavata.userprofile.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.CaseFormat;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.Map;

/**
 * This abstract class represents a generic de-serializer for converting JSON to Thrift-based entities.
 *
 * @param <E> An implementation of the {@link org.apache.thrift.TFieldIdEnum} interface.
 * @param <T> An implementation of the {@link org.apache.thrift.TBase} interface.
 */
public abstract class AbstractThriftDeserializer<E extends TFieldIdEnum, T extends TBase<T, E>> extends JsonDeserializer<T> {

    private static Logger log = LoggerFactory.getLogger(AbstractThriftDeserializer.class);

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final T instance = newInstance();
        final ObjectMapper mapper = (ObjectMapper)jp.getCodec();
        final ObjectNode rootNode = (ObjectNode)mapper.readTree(jp);
        final Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.fields();

        while(iterator.hasNext()) {
            final Map.Entry<String, JsonNode> currentField = iterator.next();
            try {
                /*
                 * If the current node is not a null value, process it.  Otherwise,
                 * skip it.  Jackson will treat the null as a 0 for primitive
                 * number types, which in turn will make Thrift think the field
                 * has been set. Also we ignore the MongoDB specific _id field
                 */
                if(!currentField.getKey().equalsIgnoreCase("_id")
                        && currentField.getValue().getNodeType() != JsonNodeType.NULL) {
                    final E field = getField(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, currentField.getKey()));
                    final JsonParser parser = currentField.getValue().traverse();
                    parser.setCodec(mapper);
                    final Object value = mapper.readValue(parser, generateValueType(instance, field));
                    if(value != null) {
                        log.debug(String.format("Field %s produced value %s of type %s.",
                                currentField.getKey(), value, value.getClass().getName()));
                        instance.setFieldValue(field, value);
                    } else {
                        log.debug("Field {} contains a null value.  Skipping...", currentField.getKey());
                    }
                } else {
                    log.debug("Field {} contains a null value.  Skipping...", currentField.getKey());
                }
            } catch (final NoSuchFieldException | IllegalArgumentException e) {
                log.error("Unable to de-serialize field '{}'.", currentField.getKey(), e);
                ctxt.mappingException(e.getMessage());
            }
        }

        try {
            // Validate that the instance contains all required fields.
            validate(instance);
        } catch (final TException e) {
            log.error(String.format("Unable to deserialize JSON '%s' to type '%s'.",
                    jp.getValueAsString(), instance.getClass().getName(), e));
            ctxt.mappingException(e.getMessage());
        }

        return instance;
    }

    /**
     * Returns the {@code <E>} enumerated value that represents the target
     * field in the Thrift entity referenced in the JSON document.
     * @param fieldName The name of the Thrift entity target field.
     * @return The {@code <E>} enumerated value that represents the target
     *   field in the Thrift entity referenced in the JSON document.
     */
    protected abstract E getField(String fieldName);

    /**
     * Creates a new instance of the Thrift entity class represented by this deserializer.
     * @return A new instance of the Thrift entity class represented by this deserializer.
     */
    protected abstract T newInstance();

    /**
     * Validates that the Thrift entity instance contains all required fields after deserialization.
     * @param instance A Thrift entity instance.
     * @throws org.apache.thrift.TException if unable to validate the instance.
     */
    protected abstract void validate(T instance) throws TException;

    /**
     * Generates a {@link JavaType} that matches the target Thrift field represented by the provided
     * {@code <E>} enumerated value.  If the field's type includes generics, the generics will
     * be added to the generated {@link JavaType} to support proper conversion.
     * @param thriftInstance The Thrift-generated class instance that will be converted to/from JSON.
     * @param field A {@code <E>} enumerated value that represents a field in a Thrift-based entity.
     * @return The {@link JavaType} representation of the type associated with the field.
     * @throws NoSuchFieldException if unable to determine the field's type.
     * @throws SecurityException if unable to determine the field's type.
     */
    protected JavaType generateValueType(final T thriftInstance, final E field) throws NoSuchFieldException, SecurityException {
        final TypeFactory typeFactory = TypeFactory.defaultInstance();

        final Field declaredField = thriftInstance.getClass().getDeclaredField(field.getFieldName());
        if(declaredField.getType().equals(declaredField.getGenericType())) {
            log.debug("Generating JavaType for type '{}'.", declaredField.getType());
            return typeFactory.constructType(declaredField.getType());
        } else {
            final ParameterizedType type = (ParameterizedType)declaredField.getGenericType();
            final Class<?>[] parameterizedTypes = new Class<?>[type.getActualTypeArguments().length];
            for(int i=0; i<type.getActualTypeArguments().length; i++) {
                parameterizedTypes[i] = (Class<?>)type.getActualTypeArguments()[i];
            }
            log.debug("Generating JavaType for type '{}' with generics '{}'", declaredField.getType(), parameterizedTypes);
            return typeFactory.constructParametricType(declaredField.getType(), parameterizedTypes);
        }
    }

}
