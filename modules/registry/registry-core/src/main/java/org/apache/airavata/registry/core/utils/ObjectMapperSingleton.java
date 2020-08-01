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

import org.apache.commons.lang3.ClassUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.dozer.CustomFieldMapper;
import org.dozer.DozerBeanMapper;
import org.dozer.classmap.ClassMap;
import org.dozer.fieldmap.FieldMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ObjectMapperSingleton extends DozerBeanMapper{
    private final static Logger logger = LoggerFactory.getLogger(ObjectMapperSingleton.class);

    private static ObjectMapperSingleton instance;

    private ObjectMapperSingleton(){}

    public static ObjectMapperSingleton getInstance(){
        if(instance == null) {
            instance = new ObjectMapperSingleton();
            instance.setMappingFiles(
                    new ArrayList<String>(){{
                        add("dozer_mapping.xml");
                    }});
            instance.setCustomFieldMapper(new SkipUnsetPrimitiveFieldMapper());
        }
        return instance;
    }

    private static class SkipUnsetPrimitiveFieldMapper implements CustomFieldMapper {
        @Override
        public boolean mapField(Object source, Object destination, Object sourceFieldValue, ClassMap classMap, FieldMap fieldMap) {
            // Just skipping mapping field if not set on Thrift source model and the field's value is primitive
            if (isSourceUnsetThriftField(source, fieldMap) && sourceFieldValue != null && ClassUtils.isPrimitiveOrWrapper(sourceFieldValue.getClass())) {
                logger.debug("Skipping field " + fieldMap.getSrcFieldName() + " since it is unset thrift field and is primitive");
                return true;
            }
            return false;
        }

        private boolean isSourceUnsetThriftField(Object source, FieldMap fieldMap) {
            if (source instanceof TBase) {
                TBase thriftSource = (TBase) source;
                try {
                    Class<?> thriftFieldsEnum = Class.forName(thriftSource.getClass().getName() + "$_Fields");
                    TFieldIdEnum srcField = (TFieldIdEnum) thriftFieldsEnum.getMethod(
                            "findByName", String.class).invoke(null, fieldMap.getSrcFieldName());
                    // FIXME: Dozer can handle case insensitive field matching, for example, "gatewayID" maps to
                    // "gatewayId" but this method of looking up field by name is case sensitive. For example,
                    // it fails to find "gatewayID" on GatewayResourceProfile.
                    if (srcField != null && !thriftSource.isSet(srcField)) {
                        return true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Thrift model class has no _Fields enum", e);
                }
            }
            return false;
        }
    }
}
