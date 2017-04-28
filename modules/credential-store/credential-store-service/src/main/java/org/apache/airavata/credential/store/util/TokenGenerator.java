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
package org.apache.airavata.credential.store.util;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 5/21/13
 * Time: 3:07 PM
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Generates tokens for users.
 */
public class TokenGenerator {

    protected static Logger log = LoggerFactory.getLogger(TokenGenerator.class);


    public TokenGenerator() {

    }

    public static String generateToken(String gatewayId, String metadata) {

        return UUID.randomUUID().toString();
    }

    public String encryptToken(String token) {
        return null;
    }

}
