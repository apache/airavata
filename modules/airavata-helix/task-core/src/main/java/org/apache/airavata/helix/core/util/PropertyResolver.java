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
 */
package org.apache.airavata.helix.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class PropertyResolver {
    private Properties properties = new Properties();

    public void loadFromFile(File propertyFile) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
    }

    public void loadInputStream(InputStream inputStream) throws IOException {
        properties = new Properties();
        properties.load(inputStream);
    }

    public String get(String key) {
        if (properties.containsKey(key)) {
            if (System.getenv(key.replace(".", "_")) != null) {
                return System.getenv(key.replace(".", "_"));
            } else {
                return properties.getProperty(key);
            }
        } else {
            return null;
        }
    }

    public String get(String key, String defaultValue) {
        return Optional.ofNullable(get(key)).orElse(defaultValue);
    }
}
