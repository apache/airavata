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
package org.apache.airavata.registry.utils;

import com.github.dozermapper.core.BeanFactory;
import com.github.dozermapper.core.config.BeanContainer;
import com.github.dozermapper.core.util.MappingUtils;
import com.github.dozermapper.core.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.registry.entities.appcatalog.AWSGroupComputeResourcePrefEntity;
import org.apache.airavata.registry.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.entities.appcatalog.SlurmGroupComputeResourcePrefEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBeanFactory implements BeanFactory {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanFactory.class);

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId, BeanContainer beanContainer) {
        Object result;
        Class<?> destClass = MappingUtils.loadClass(targetBeanId, beanContainer);
        if (GroupComputeResourcePrefEntity.class.equals(destClass)
                && source instanceof GroupComputeResourcePreference pref) {
            ComputeResourceType resourceType = pref.getResourceType();
            if (resourceType == ComputeResourceType.AWS) {
                destClass = AWSGroupComputeResourcePrefEntity.class;
            } else {
                destClass = SlurmGroupComputeResourcePrefEntity.class;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Creating bean of type {}", destClass.getSimpleName());
        }
        result = ReflectionUtils.newInstance(destClass);
        // Check if result is a Thrift model using reflection (to avoid compile-time dependency)
        if (isThriftModel(result)) {
            callSettersOnThriftFieldsWithDefaults(result);
        }
        return result;
    }

    /**
     * Check if an object is a Thrift model using reflection (to avoid compile-time dependency on Thrift).
     */
    private boolean isThriftModel(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            // Check if the class implements TBase interface (Thrift models implement this)
            Class<?> tBaseClass = Class.forName("org.apache.thrift.TBase");
            return tBaseClass.isAssignableFrom(obj.getClass());
        } catch (ClassNotFoundException e) {
            // Thrift library not available, so it's not a Thrift model
            return false;
        }
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
     * @param instance
     */
    private void callSettersOnThriftFieldsWithDefaults(Object instance) {

        try {
            // Use reflection to access Thrift-specific methods and classes
            Class<?> tBaseClass = Class.forName("org.apache.thrift.TBase");
            Class<?> tFieldIdEnumClass = Class.forName("org.apache.thrift.TFieldIdEnum");
            Class<?> fieldMetaDataClass = Class.forName("org.apache.thrift.meta_data.FieldMetaData");

            Field metaDataMapField = instance.getClass().getField("metaDataMap");
            @SuppressWarnings("unchecked")
            Map<Object, Object> metaDataMap = (Map<Object, Object>) metaDataMapField.get(null);

            for (Entry<Object, Object> metaDataEntry : metaDataMap.entrySet()) {
                Object fieldMetaData = metaDataEntry.getValue();
                String fieldName =
                        (String) fieldMetaDataClass.getMethod("getFieldName").invoke(fieldMetaData);

                if (logger.isDebugEnabled()) {
                    logger.debug("processing field {}", fieldName);
                }

                Object fieldIdEnum = metaDataEntry.getKey();
                java.lang.reflect.Method getFieldValueMethod = tBaseClass.getMethod("getFieldValue", tFieldIdEnumClass);
                Object fieldValue = getFieldValueMethod.invoke(instance, fieldIdEnum);

                if (fieldValue != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "field {} has a default value [{}], calling setter to force the field to be set",
                                fieldName,
                                fieldValue);
                    }
                    java.lang.reflect.Method setFieldValueMethod =
                            tBaseClass.getMethod("setFieldValue", tFieldIdEnumClass, Object.class);
                    setFieldValueMethod.invoke(instance, fieldIdEnum, fieldValue);
                }
            }
        } catch (Exception e) {
            // If Thrift classes are not available or reflection fails, skip this optimization
            if (logger.isDebugEnabled()) {
                logger.debug("Could not call setters on Thrift fields with defaults (Thrift may not be available)", e);
            }
        }
    }
}
