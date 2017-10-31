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
package org.apache.airavata.k8s.api.server.model.experiment;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "EXPERIMENT_OUTPUT_OBJECT")
public class ExperimentOutputData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String value;
    private DataType type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public static enum DataType {
        STRING(0),
        INTEGER(1),
        FLOAT(2),
        URI(3),
        URI_COLLECTION(4),
        STDOUT(5),
        STDERR(6);

        private final int value;
        private static Map<Integer, DataType> map = new HashMap<>();

        static {
            for (DataType dataType : DataType.values()) {
                map.put(dataType.value, dataType);
            }
        }

        public static DataType valueOf(int dataType) {
            return map.get(dataType);
        }

        private DataType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

    }
}
