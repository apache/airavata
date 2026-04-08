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
package org.apache.airavata.credential.mapper;

import org.mapstruct.Mapper;

/**
 * MapStruct mapper skeleton for credential entities.
 *
 * <p>Proto-to-entity mapping for credentials involves encryption/decryption of the
 * credential blob, so the actual conversion logic lives in
 * {@link org.apache.airavata.credential.util.CredentialEncryptionUtil}.
 * This mapper can be extended later for any non-encrypted field mappings.
 */
@Mapper(componentModel = "spring")
public interface CredentialMapper {
    // Intentionally empty — credential blob mapping requires encryption logic
    // that cannot be expressed as simple field mappings.
}
