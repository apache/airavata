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
package org.apache.airavata.security.configurations;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract implementation to read configurations.
 */
public abstract class AbstractConfigurationReader {

    public void init(String fileName) throws IOException, SAXException, ParserConfigurationException {

        File configurationFile = new File(fileName);

        if (!configurationFile.canRead()) {
            throw new IOException("Error reading configuration file " + configurationFile.getAbsolutePath());
        }

        FileInputStream streamIn = new FileInputStream(configurationFile);

        try {
            init(streamIn);
        } finally {
            streamIn.close();
        }
    }

    public abstract void init(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException;
}
