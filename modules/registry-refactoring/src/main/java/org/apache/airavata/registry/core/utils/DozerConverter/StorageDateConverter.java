/*
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

package org.apache.airavata.registry.core.utils.DozerConverter;

import org.dozer.DozerConverter;

import java.sql.Timestamp;

/**
 * Created by skariyat on 4/11/18.
 */
public class StorageDateConverter extends DozerConverter {

    public StorageDateConverter(Class prototypeA, Class prototypeB) {
        super(prototypeA, prototypeB);
    }

    @Override
    public Object convertTo(Object source, Object dest) {

        if (source != null) {
            if (source instanceof Long) {
                return new Timestamp((long) source);
            } else if (source instanceof Timestamp) {
                return ((Timestamp)source).getTime();
            }
        }
        return null;
    }

    @Override
    public Object convertFrom(Object source, Object dest) {
        return convertTo(source, dest);
    }

}
