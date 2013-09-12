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

package org.apache.airavata.rest.utils;

import com.sun.jersey.api.client.WebResource;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;

public class BasicAuthHeaderUtil {

    protected static Logger log = LoggerFactory.getLogger(BasicAuthHeaderUtil.class);


    /**
     * A method to use by clients in the case of Basic Access authentication.
     * Creates Basic Auth header structure.
     * Reference - http://en.wikipedia.org/wiki/Basic_access_authentication
     * @param userName The user name.
     * @param password Password as credentials.
     * @return  Base64 encoded authorisation header.
     */
    public static String getBasicAuthHeader(String userName, String password) {

        String credentials = userName + ":" + password;
        String encodedString = null;
        try {
            encodedString = new String(Base64.encodeBase64(credentials.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            // TODO we need to do proper exception handling
            log.error("Error encoding credentials. ", e);
            throw new RuntimeException("Error encoding credentials. ", e);
        }
        return "Basic " + encodedString;
    }

    public static WebResource.Builder getBuilder(WebResource webResource,
                                                 MultivaluedMap queryParams,
                                                 String userName,
                                                 String password,
                                                 Cookie cookie,
                                                 String gateway) {
        if (queryParams != null){
            webResource = webResource.queryParams(queryParams);
        }
        WebResource.Builder builder = webResource.header("Authorization",
                BasicAuthHeaderUtil.getBasicAuthHeader(userName, password));
        builder.header("gateway_id", gateway);
        if (cookie != null){
            builder.cookie(cookie);
        }

        return builder;
    }
}
