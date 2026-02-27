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
package org.apache.airavata.core.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Consolidates the repeated limit/offset-to-{@link Pageable} conversion pattern
 * used across services that support paginated listing.
 */
public final class PaginationUtil {

    private PaginationUtil() {}

    /**
     * Converts a limit/offset pair to a Spring Data {@link Pageable}.
     *
     * <p>Guards against zero or negative limits to prevent division by zero.
     *
     * @param limit  the maximum number of results per page (clamped to at least 1)
     * @param offset the zero-based item offset
     * @return a PageRequest suitable for Spring Data repository methods
     */
    public static Pageable toPageRequest(int limit, int offset) {
        int safeLimit = Math.max(limit, 1);
        return PageRequest.of(offset / safeLimit, safeLimit);
    }
}
