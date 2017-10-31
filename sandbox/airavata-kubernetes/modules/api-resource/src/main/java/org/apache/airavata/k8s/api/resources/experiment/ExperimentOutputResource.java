/**
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
 */
package org.apache.airavata.k8s.api.resources.experiment;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExperimentOutputResource {

    private long id;
    private String name;
    private String value;
    private int type;

    public long getId() {
        return id;
    }

    public ExperimentOutputResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ExperimentOutputResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExperimentOutputResource setValue(String value) {
        this.value = value;
        return this;
    }

    public int getType() {
        return type;
    }

    public ExperimentOutputResource setType(int type) {
        this.type = type;
        return this;
    }

    public static final class Types {
        public static final int STRING = 0;
        public static final int INTEGER = 1;
        public static final int FLOAT = 2;
        public static final int URI = 3;
        public static final int URI_COLLECTION = 4;
        public static final int STDOUT = 5;
        public static final int STDERR = 6;
    }
}
