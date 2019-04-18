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
package org.apache.airavata.service.security.authzcache;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.security.AiravataSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This initializes the AuthzCacheManager implementation to be used as defined by the configuration.
 */
public class AuthzCacheManagerFactory {
    private final static Logger logger = LoggerFactory.getLogger(AuthzCacheManagerFactory.class);

    public static AuthzCacheManager getAuthzCacheManager() throws AiravataSecurityException {
        try {
            Class authzCacheManagerImpl = Class.forName(ServerSettings.getAuthzCacheManagerClassName());
            AuthzCacheManager authzCacheManager  = (AuthzCacheManager) authzCacheManagerImpl.newInstance();
            return  authzCacheManager;
        } catch (ClassNotFoundException e) {
            String error = "Authorization Cache Manager class could not be found.";
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException(error);
        } catch (ApplicationSettingsException e) {
            String error = "Error in reading the configuration related to Authorization Cache Manager class.";
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException(error);
        } catch (InstantiationException e) {
            String error = "Error in instantiating the Authorization Cache Manager class.";
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException(error);
        } catch (IllegalAccessException e) {
            String error = "Error in instantiating the Authorization Cache Manager class.";
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException(error);

        }
    }

}
