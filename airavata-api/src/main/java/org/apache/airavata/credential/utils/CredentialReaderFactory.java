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
package org.apache.airavata.credential.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.impl.store.CredentialReaderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory class to create credential store readers.
 * @deprecated Use Spring dependency injection instead. This factory is kept for backward compatibility.
 */
@Component
@Deprecated
public class CredentialReaderFactory {

    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        CredentialReaderFactory.applicationContext = applicationContext;
    }

    /**
     * Creates a credential reader using supplied database configurations.
     * @deprecated Use Spring dependency injection instead.
     * @param dbUti The database configurations.
     * @return CredentialReader object.
     */
    @Deprecated
    public static CredentialReader createCredentialStoreReader(DBUtil dbUti) throws ApplicationSettingsException {
        if (applicationContext != null) {
            return applicationContext.getBean(CredentialReaderImpl.class);
        }
        throw new IllegalStateException(
                "CredentialReaderFactory not initialized. Use Spring dependency injection instead.");
    }

    /**
     * Creates credential reader using default configurations for credential store database.
     * @deprecated Use Spring dependency injection instead.
     * @return The credential reader.
     * @throws ClassNotFoundException If an error occurred while instantiating jdbc driver
     * @throws ApplicationSettingsException If an error occurred while reading database configurations.
     * @throws InstantiationException If an error occurred while instantiating jdbc driver
     * @throws IllegalAccessException A security exception accessing jdbc driver.
     */
    @Deprecated
    public static CredentialReader createCredentialStoreReader()
            throws ClassNotFoundException, ApplicationSettingsException, InstantiationException,
                    IllegalAccessException {
        if (applicationContext != null) {
            return applicationContext.getBean(CredentialReaderImpl.class);
        }
        throw new IllegalStateException(
                "CredentialReaderFactory not initialized. Use Spring dependency injection instead.");
    }
}
