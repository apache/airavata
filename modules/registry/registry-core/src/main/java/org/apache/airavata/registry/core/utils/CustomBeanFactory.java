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

package org.apache.airavata.registry.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.TFieldRequirementType;
import org.apache.thrift.meta_data.FieldMetaData;
import org.dozer.BeanFactory;
import org.dozer.util.MappingUtils;
import org.dozer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBeanFactory implements BeanFactory {

    private final static Logger logger = LoggerFactory.getLogger(CustomBeanFactory.class);

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId) {
        Object result;
        Class<?> destClass = MappingUtils.loadClass(targetBeanId);
        if (logger.isDebugEnabled()) {
            logger.debug("Creating bean of type " + destClass.getSimpleName());
        }
        result = ReflectionUtils.newInstance(destClass);
        if (result instanceof TBase) {

            callSettersOnThriftFieldsWithDefaults((TBase) result);
        }
        return result;
    }

    /**
     * Thrift fields with default values aren't serialized and sent over the wire if
     * their setters were never called. However, Dozer doesn't call the setter on
     * the field of a target object when the target field's value already matches
     * the source's field value. This results in the Thrift data model object field
     * having the default value but it doesn't get serialized with that value (and
     * for required fields validation fails). The following changes the semantics of
     * defaulted Thrift fields a bit so that they are always "set" even if the
     * source object had no such field, but this matches the more general semantics
     * of what is expected from fields that have default values and it works around
     * an annoyance with required default fields that would fail validation
     * otherwise.
     * 
     * <p>
     * See AIRAVATA-3268 and AIRAVATA-3328 for more information.
     * 
     * @param <T>
     * @param <F>
     * @param instance
     */
    private <T extends TBase<T, F>, F extends TFieldIdEnum> void callSettersOnThriftFieldsWithDefaults(
            TBase<T, F> instance) {

        try {
            Field metaDataMapField = instance.getClass().getField("metaDataMap");
            Map<F, FieldMetaData> metaDataMap = (Map<F, FieldMetaData>) metaDataMapField.get(null);
            for (Entry<F, FieldMetaData> metaDataEntry : metaDataMap.entrySet()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("processing field " + metaDataEntry.getValue().fieldName);
                }
                Object fieldValue = instance.getFieldValue(metaDataEntry.getKey());
                if (fieldValue != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("field " + metaDataEntry.getValue().fieldName + " has a default value ["
                                + fieldValue + "], calling setter to force the field to be set");
                    }
                    instance.setFieldValue(metaDataEntry.getKey(), fieldValue);
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            MappingUtils.throwMappingException(e);
        }
    }

}
