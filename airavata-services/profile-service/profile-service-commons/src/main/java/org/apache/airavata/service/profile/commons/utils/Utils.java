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
package org.apache.airavata.service.profile.commons.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


public class Utils {
    private final static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getJDBCURL(){
    	try {
            return ServerSettings.getSetting(JPAConstants.KEY_JDBC_URL);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getHost(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getHost();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static int getPort(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getPort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public static int getJPACacheSize (){
        try {
            String cache = ServerSettings.getSetting(JPAConstants.JPA_CACHE_SIZE, "5000");
            return Integer.parseInt(cache);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public static String isCachingEnabled (){
        try {
            return ServerSettings.getSetting(JPAConstants.ENABLE_CACHING, "true");
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            return "true";
        }
    }

    public static String getJDBCUser(){
    	try {
		    return ServerSettings.getSetting(JPAConstants.KEY_JDBC_USER);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    public static String getValidationQuery(){
    	try {
            return ServerSettings.getSetting(JPAConstants.VALIDATION_QUERY);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    public static String getJDBCPassword(){
    	try {
            return ServerSettings.getSetting(JPAConstants.KEY_JDBC_PASSWORD);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}

    }

    public static String getJDBCDriver(){
    	try {
            return ServerSettings.getSetting(JPAConstants.KEY_JDBC_DRIVER);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }
}
