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
package org.apache.airavata.common.model;

/**
 * Enum representing the type of library path modification in an application deployment.
 *
 * <p>This consolidates the separate library path tables:
 * <ul>
 *   <li>LIBRARY_PREPAND_PATH - Paths prepended to the library search path</li>
 *   <li>LIBRARY_APEND_PATH - Paths appended to the library search path</li>
 * </ul>
 *
 * @see org.apache.airavata.registry.entities.appcatalog.LibraryPathEntity
 */
public enum LibraryPathType {
    /**
     * Paths that are prepended (added at the beginning) to the library search path.
     * These paths take precedence over system library paths.
     */
    PREPEND,

    /**
     * Paths that are appended (added at the end) to the library search path.
     * These paths are searched after system library paths.
     */
    APPEND
}
